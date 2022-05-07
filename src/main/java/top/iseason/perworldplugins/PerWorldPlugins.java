package top.iseason.perworldplugins;

import top.iseason.perworldplugins.command.Commands;
import top.iseason.perworldplugins.loader.PWPLoader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class PerWorldPlugins extends JavaPlugin implements Listener {
    private static PerWorldPlugins INSTANCE;
    private static PWPLoader pwpLoader;
    public List<Class<?>> exemptEvents = Arrays.asList(new Class<?>[]{AsyncPlayerPreLoginEvent.class, PlayerJoinEvent.class, PlayerKickEvent.class, PlayerLoginEvent.class, PlayerPreLoginEvent.class, PlayerQuitEvent.class});
    private boolean isExemptEnabled = true;

    private final Map<String, Set<String>> pluginNameToWorlds = new HashMap<>();

    public void onLoad() {
        INSTANCE = this;
        log("Registering event interceptor...");
        pwpLoader = new PWPLoader(Bukkit.getServer());
        pwpLoader.setLoader((JavaPluginLoader) getPluginLoader());
        injectExistingPlugins(pwpLoader);
        cleanJavaPluginLoaders(pwpLoader);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        String[] s = message.split(" ");
        if (s.length < 1) return;
        String s1 = s[0].replace("/", "");
        Command command = Bukkit.getCommandMap().getCommand(s1);
        if (!(command instanceof PluginCommand)) return;
        PluginCommand pc = (PluginCommand) command;
        if (checkWorld(pc.getPlugin(), event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }


    private void injectExistingPlugins(PWPLoader pwpLoader) {
        for (org.bukkit.plugin.Plugin p : Bukkit.getPluginManager().getPlugins()) {
            if (p instanceof JavaPlugin) {
                //只修改JavaPluginLoader的
                if (!p.getPluginLoader().getClass().equals(JavaPluginLoader.class)) continue;
                if (p instanceof PerWorldPlugins) continue;
                JavaPlugin jp = (JavaPlugin) p;
                try {
                    Field f = JavaPlugin.class.getDeclaredField("loader");
                    f.setAccessible(true);
                    f.set(jp, pwpLoader);
                } catch (Exception e) {
                    log("PerWorldPlugins failed injecting " + jp.getDescription().getFullName()
                            + " with the new PluginLoader, contact the developers on BukkitDev!" + e);
                }
            }
        }
    }

    private void cleanJavaPluginLoaders(PWPLoader pwpLoader) {
        PluginManager spm = Bukkit.getPluginManager();
        try {
            Field field = spm.getClass().getDeclaredField("fileAssociations");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Pattern, PluginLoader> map = (Map<Pattern, PluginLoader>) field.get(spm);
            map.entrySet().stream()
                    .filter(entry -> entry.getValue().getClass().equals(JavaPluginLoader.class))
                    .forEach(entry -> {
                        map.replace(entry.getKey(), pwpLoader);
                    });
        } catch (Exception e) {
            Bukkit.getServer().getLogger().log(Level.SEVERE, "PerWorldPlugins failed replacing the existing PluginLoader, contact the developers on BukkitDev!", e);
        }
    }

    public void onEnable() {

        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("pwp")).setExecutor(new Commands());
        this.reload();
    }

    public static PerWorldPlugins getInstance() {
        return INSTANCE;
    }

    public void log(String s) {
        Bukkit.getServer().getLogger().log(Level.SEVERE, "[PerWorldPlugins] " + s);
    }

    public void loadConfig() {
        this.saveDefaultConfig();

        FileConfiguration c = getConfig();

        if (!c.isBoolean("exempt-login-events") || !c.contains("exempt-login-events")
                || !c.isSet("exempt-login-events")) {
            c.set("exempt-login-events", true);
        }

        isExemptEnabled = c.getBoolean("exempt-login-events", true);
        ConfigurationSection ul = c.getConfigurationSection("limit");
        if (ul == null) {
            ul = c.createSection("limit");
        }
        for (org.bukkit.plugin.Plugin plug : Bukkit.getPluginManager().getPlugins()) {
            if (plug.equals(this))
                continue;
            if (!ul.isList(plug.getDescription().getName())) {
                ul.set(plug.getDescription().getName(), new ArrayList<String>());
            }
        }
        saveConfig();
    }

    public void reload() {
        this.reloadConfig();
        this.loadConfig();

        pluginNameToWorlds.clear();
        ConfigurationSection limit = getConfig().getConfigurationSection("limit");

        assert limit != null;

        for (String pluginName : limit.getKeys(false)) {
            if (limit.isList(pluginName)) {
                List<String> worldNames = limit.getStringList(pluginName);
                if (worldNames.size() == 0)
                    continue;
                pluginNameToWorlds.put(pluginName, new HashSet<>());
                worldNames.stream().map(String::toLowerCase).forEach(pluginNameToWorlds.get(pluginName)::add);
            }
        }
    }

    public boolean checkWorld(org.bukkit.plugin.Plugin plugin, World w) {
        if (plugin == null) return false;

        if (w == null) return false;

        String pluginName = plugin.getDescription().getName();
        Set<String> restrictedWorlds = pluginNameToWorlds.get(pluginName);

        if (restrictedWorlds == null)
            return false;
        return !restrictedWorlds.contains(w.getName().toLowerCase());
    }

    public boolean checkWorld(org.bukkit.plugin.Plugin plugin, Event e) {
        if ((e instanceof PlayerEvent)) {
            PlayerEvent e1 = (PlayerEvent) e;
            if ((exemptEvents.contains(e.getClass())) && (PerWorldPlugins.getInstance().isExemptEnabled())) {
                return false;
            }
            return checkWorld(plugin, e1.getPlayer().getWorld());
        }

        if ((e instanceof BlockEvent)) {
            BlockEvent e1 = (BlockEvent) e;
            e1.getBlock().getWorld();
            return checkWorld(plugin, e1.getBlock().getWorld());
        }

        if ((e instanceof InventoryEvent)) {
            InventoryEvent e1 = (InventoryEvent) e;
            e1.getView().getPlayer();
            e1.getView().getPlayer().getWorld();
            return checkWorld(plugin, e1.getView().getPlayer().getWorld());
        }

        if ((e instanceof EntityEvent)) {
            EntityEvent e1 = (EntityEvent) e;
            e1.getEntity();
            e1.getEntity().getWorld();
            return checkWorld(plugin, e1.getEntity().getWorld());
        }

        if ((e instanceof HangingEvent)) {
            HangingEvent e1 = (HangingEvent) e;
            e1.getEntity();
            e1.getEntity().getWorld();
            return checkWorld(plugin, e1.getEntity().getWorld());
        }

        if ((e instanceof VehicleEvent)) {
            VehicleEvent e1 = (VehicleEvent) e;
            e1.getVehicle().getWorld();
            return checkWorld(plugin, e1.getVehicle().getWorld());
        }

        if ((e instanceof WeatherEvent)) {
            WeatherEvent e1 = (WeatherEvent) e;
            return checkWorld(plugin, e1.getWorld());
        }

        if ((e instanceof WorldEvent)) {
            WorldEvent e1 = (WorldEvent) e;
            e1.getWorld();
            return checkWorld(plugin, e1.getWorld());
        }
        return false;
    }

    public boolean isExemptEnabled() {
        return this.isExemptEnabled;
    }

    // Just for making string coloring less tedious.
    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
