package top.iseason.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import top.iseason.bukkit.model.Limiter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Level;


public class ConfigManager {

    private static final PluginLimiter plugin = PluginLimiter.getInstance();
    private static final HashMap<Plugin, Limiter> pluginLimiters = new HashMap<>();
    private static Limiter whiteList = null;
    private static Limiter blackList = null;
    private static Class<?> LOOT_GENERATE_EVENT = null;

    private static String commandMessage = "";

    public static void loadConfig() throws IOException, InvalidConfigurationException {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            plugin.saveDefaultConfig();
        }
        plugin.reloadConfig();
        YamlConfiguration config = new YamlConfiguration();
        config.options().pathSeparator('\\');
        config.load(file);
        //设置全局
        //白名单
        ConfigurationSection whitelist = config.getConfigurationSection("whitelist");
        if (whitelist != null) {
            whiteList = new Limiter(whitelist);
        }
        //黑名单
        ConfigurationSection blacklist = config.getConfigurationSection("blacklist");
        if (blacklist != null) {
            blackList = new Limiter(blacklist);
        }

        //设置插件
        ConfigurationSection pl = config.getConfigurationSection("limits");
        if (pl == null) {
            pl = config.createSection("limits");
        }
        for (org.bukkit.plugin.Plugin plug : Bukkit.getPluginManager().getPlugins()) {
            if (plug.equals(PluginLimiter.getInstance()))
                continue;
            String name = plug.getDescription().getName();
            //不存在
            ConfigurationSection cs = pl.getConfigurationSection(name);
            if (cs == null) {
                pl.set(name, "NONE");
                continue;
            }
            pluginLimiters.put(plug, new Limiter(cs));
        }
        commandMessage = config.getString("block-command-message", "");
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException ex) {
            PluginLimiter.log(Level.SEVERE, "Could not save config to ");
        }
    }

    public static boolean matchCommand(Plugin plugin, String command, Location loc) {
        if (whiteList != null && whiteList.isEnabled() && whiteList.matchCommand(command, loc)) {
            return false;
        }
        if (blackList != null && blackList.isEnabled() && blackList.matchCommand(command, loc)) {
            return true;
        }
        Limiter limiter = pluginLimiters.get(plugin);
        if (limiter != null && limiter.isEnabled()) {
            return limiter.matchCommand(command, loc);
        }
        return false;
    }

    public static boolean matchEvent(Plugin plugin, Event event) {
        if (whiteList != null && whiteList.isEnabled() && whiteList.matchEvent(event)) {
            return false;
        }
        if (blackList != null && blackList.isEnabled() && blackList.matchEvent(event)) {
            return true;
        }
        Limiter limiter = pluginLimiters.get(plugin);
        if (limiter != null) {
            return limiter.matchEvent(event);
        }
        return false;
    }

    public static boolean matchListener(Plugin plugin, Listener listener, Method method, Event event) {
        Limiter limiter = pluginLimiters.get(plugin);
        if (limiter != null) {
            return limiter.matchListener(listener, method, event);
        }
        return false;
    }

    public static void reload() {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            whiteList = null;
            pluginLimiters.clear();
            LOOT_GENERATE_EVENT = getLootGenerateEventReflection();
            try {
                loadConfig();
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        });
    }

    public static Class<?> getLootGenerateEventReflection() {
        try {
            return Class.forName("org.bukkit.event.world.LootGenerateEvent");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static boolean hasLootGenerateEvent() {
        return LOOT_GENERATE_EVENT != null;
    }

    public static String getCommandMessage() {
        return commandMessage;
    }
}
