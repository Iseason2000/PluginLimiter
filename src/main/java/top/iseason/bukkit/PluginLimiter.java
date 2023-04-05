package top.iseason.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import top.iseason.bukkit.bstat.Metrics;
import top.iseason.bukkit.command.Commands;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

public class PluginLimiter extends JavaPlugin implements Listener {
    private static PluginLimiter INSTANCE;
    //    private static PLoader pLoader = null;
    private SimpleCommandMap commandMap = null;

    public static void log(Level level, String s) {
        Bukkit.getServer().getLogger().log(level, "[PluginLimiter] " + s);
    }

    public static PluginLimiter getInstance() {
        return INSTANCE;
    }

    public PluginLimiter() {
//        injectExistingPlugins();
    }

    public void onLoad() {
        INSTANCE = this;
        try {
            Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (SimpleCommandMap) f.get(Bukkit.getServer());
            f.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log(Level.SEVERE, "Command Map not Found");
        }
        new Metrics(this, 15162);
    }

    public void onEnable() {
        if (commandMap != null)
            Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("PluginLimiter")).setExecutor(new Commands());
        ConfigManager.reload();

//        replaceJavaPluginLoaders();
        Bukkit.getScheduler().runTaskAsynchronously(this, this::proxyListeners);
    }

    private void proxyListeners() {
        log(Level.INFO, "开始替换监听器");
        //储存所有插件的所有监听器
        HashMap<Plugin, HashSet<Listener>> map = new HashMap<>();
        //所有事件的监听器
        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            //单个事件的所有监听器
            for (RegisteredListener registeredListener : handlerList.getRegisteredListeners()) {
                map.computeIfAbsent(registeredListener.getPlugin(), it -> new HashSet<>()).add(registeredListener.getListener());
            }
        }
        //重新注册监听器
        HandlerList.unregisterAll();
        map.forEach((p, l) -> {
            for (Listener listener : l) {
                Set<Method> methods;
                try {
                    Method[] publicMethods = listener.getClass().getMethods();
                    Method[] privateMethods = listener.getClass().getDeclaredMethods();
                    methods = new HashSet<>(publicMethods.length + privateMethods.length, 1.0f);
                    Collections.addAll(methods, publicMethods);
                    Collections.addAll(methods, privateMethods);
                } catch (Exception ignored) {
                    continue;
                }
                for (Method method : methods) {
                    final EventHandler eh = method.getAnnotation(EventHandler.class);
                    if (eh == null) continue;
                    if (method.isBridge() || method.isSynthetic()) {
                        continue;
                    }
                    final Class<?> checkClass;
                    if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0])) {
                        continue;
                    }
                    final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
                    method.setAccessible(true);
                    if (ConfigManager.matchEvent(p, null) || ConfigManager.matchListener(p, listener, method, null)) {
                        continue;
                    }
                    EventExecutor executor = (listener1, event) -> {
                        try {
                            if (!eventClass.isAssignableFrom(event.getClass())) {
                                return;
                            }
                            if (ConfigManager.matchEvent(p, event)) {
                                return;
                            }
                            //此处可修改是否触发
                            if (ConfigManager.matchListener(p, listener1, method, event)) {
                                return;
                            }
                            method.invoke(listener1, event);
                        } catch (Throwable t) {
                            throw new EventException(t);
                        }
                    };
                    Bukkit.getPluginManager().registerEvent(eventClass, listener, eh.priority(), executor, p, eh.ignoreCancelled());
                }
            }
        });
        HandlerList.bakeAll();
        log(Level.INFO, "监听器替换完成");
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
        Player player = event.getPlayer();
        if (ConfigManager.matchCommand(pc.getPlugin(), message, player.getLocation())) {
            String commandMessage = ConfigManager.getCommandMessage();
            if (!commandMessage.isEmpty()) {
                player.sendMessage(color(commandMessage.replace("%player%", player.getDisplayName())));
            }
            event.setCancelled(true);
        }
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
