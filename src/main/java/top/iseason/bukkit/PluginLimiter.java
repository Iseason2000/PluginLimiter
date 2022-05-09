package top.iseason.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import top.iseason.bukkit.command.Commands;
import top.iseason.bukkit.model.PLoader;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class PluginLimiter extends JavaPlugin implements Listener {
    private static PluginLimiter INSTANCE;
    private static PLoader pLoader;
    private SimpleCommandMap commandMap = null;

    public static void log(Level level, String s) {
        Bukkit.getServer().getLogger().log(level, "[PluginLimiter] " + s);
    }


    public static PluginLimiter getInstance() {
        return INSTANCE;
    }

    public void onLoad() {
        INSTANCE = this;
        log(Level.INFO, "开始替换 Loader...");
        pLoader = new PLoader(Bukkit.getServer());
        pLoader.setLoader((JavaPluginLoader) getPluginLoader());
        try {
            Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (SimpleCommandMap) f.get(Bukkit.getServer());
            f.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log(Level.SEVERE, "Command Map not Found");
        }
        injectExistingPlugins(pLoader);
        cleanJavaPluginLoaders(pLoader);
        log(Level.INFO, "替换 Loader 完成");
    }

    public void onEnable() {
        if (commandMap != null)
            Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("PluginLimiter")).setExecutor(new Commands());
        ConfigManager.reload();
    }


    private void injectExistingPlugins(PLoader pwpLoader) {
        for (org.bukkit.plugin.Plugin p : Bukkit.getPluginManager().getPlugins()) {
            if (p instanceof JavaPlugin) {
                //只修改JavaPluginLoader的
                if (!p.getPluginLoader().getClass().equals(JavaPluginLoader.class)) continue;
                if (p instanceof PluginLimiter) continue;
                JavaPlugin jp = (JavaPlugin) p;
                try {
                    Field f = JavaPlugin.class.getDeclaredField("loader");
                    f.setAccessible(true);
                    f.set(jp, pwpLoader);
                    f.setAccessible(false);
                } catch (Exception e) {
                    log(Level.SEVERE, "PluginLimiter failed injecting " + jp.getDescription().getFullName()
                            + " with the new PluginLoader, contact the developers on BukkitDev!" + e);
                }
            }
        }
    }

    private void cleanJavaPluginLoaders(PLoader pwpLoader) {
        PluginManager spm = Bukkit.getPluginManager();
        try {
            Field field = spm.getClass().getDeclaredField("fileAssociations");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Pattern, PluginLoader> map = (Map<Pattern, PluginLoader>) field.get(spm);
            map.entrySet().stream()
                    .filter(entry -> entry.getValue().getClass().equals(JavaPluginLoader.class))
                    .forEach(entry -> map.replace(entry.getKey(), pwpLoader));
        } catch (Exception e) {
            Bukkit.getServer().getLogger().log(Level.SEVERE, "PluginLimiter failed replacing the existing PluginLoader, contact the developers on BukkitDev!", e);
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        String[] s = message.split(" ");
        if (s.length < 1) return;
        String s1 = s[0].substring(1);
        Command command = commandMap.getCommand(s1);
        if (!(command instanceof PluginCommand)) return;
        PluginCommand pc = (PluginCommand) command;
        if (ConfigManager.matchCommand(pc.getPlugin(), message, event.getPlayer().getLocation())) {
            event.setCancelled(true);
        }
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
