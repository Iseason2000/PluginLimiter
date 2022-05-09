package top.iseason.bukkit.model.matchers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import top.iseason.bukkit.PluginLimiter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class ListenerMatcher extends BaseMatcher {
    private final HashMap<Class<?>, ListenerMethodMatcher> listenerClass = new HashMap<>();

    private final boolean matchAll;

    public ListenerMatcher(boolean matchAll) {
        this.matchAll = matchAll;
    }

    public static ListenerMatcher fromConfig(ConfigurationSection section) {
        Set<String> keys = section.getKeys(false);
        ListenerMatcher listenerMatcher = new ListenerMatcher(keys.isEmpty());
        for (String key : keys) {
            ListenerMethodMatcher listenerMethodMatcher = listenerMatcher.addListener(key);
            if (listenerMethodMatcher == null) {
                PluginLimiter.log(Level.WARNING, "Listener not found " + key);
                continue;
            }
            List<String> key1 = section.getStringList(key);
            if (key1.isEmpty()) listenerMethodMatcher.setMatchAll(true);
            for (String s : key1) {
                if (!listenerMethodMatcher.addMethod(s)) {
                    PluginLimiter.log(Level.WARNING, "Method not found in " + key + " : " + s);
                }
            }
        }
        return listenerMatcher;
    }

    public ListenerMethodMatcher addListener(String name) {
        if (matchAll) return null;
        try {
            Class<?> aClass = Class.forName(name);
            if (!Listener.class.isAssignableFrom(aClass)) {
                PluginLimiter.log(Level.WARNING, name + " is not a Listener");
                return null;
            }
            ListenerMethodMatcher listenerMethodMatcher = new ListenerMethodMatcher(aClass, false);
            listenerClass.put(aClass, listenerMethodMatcher);
            return listenerMethodMatcher;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public boolean match(Object obj, Method method) {
        if (matchAll) return checkIfReverse(true);
        ListenerMethodMatcher listenerMethodMatcher = listenerClass.get(obj.getClass());
        if (listenerMethodMatcher == null) return checkIfReverse(false);
        if (method == null) return checkIfReverse(true);
        return checkIfReverse(listenerMethodMatcher.match(method));
    }

    @Override
    public boolean match(Object obj) {
        if (matchAll) return checkIfReverse(true);
        return listenerClass.containsKey(obj.getClass());
    }
}
