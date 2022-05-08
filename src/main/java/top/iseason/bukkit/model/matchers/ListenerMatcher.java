package top.iseason.bukkit.model.matchers;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import top.iseason.bukkit.PluginLimiter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Level;

public class ListenerMatcher extends BaseMatcher {
    private final HashMap<Class<?>, ListenerMethodMatcher> listenerClass = new HashMap<>();
    @Getter
    private final boolean matchAll;

    public ListenerMatcher(boolean matchAll) {
        this.matchAll = matchAll;
    }

    //todo: 完成反序列化
    public static BaseMatcher fromConfig(ConfigurationSection section) {
        return null;
    }

    public boolean addListener(String name) {
        if (matchAll) return true;
        try {
            Class<?> aClass = Class.forName(name);
            if (!Listener.class.isAssignableFrom(aClass)) {
                PluginLimiter.log(Level.WARNING, name + " is not a Listener");
                return false;
            }
            listenerClass.put(aClass, new ListenerMethodMatcher(aClass, false));
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public boolean match(Object obj, Method method) {
        if (matchAll) return checkIfReverse(true);
        ListenerMethodMatcher listenerMethodMatcher = listenerClass.get(obj.getClass());
        if (listenerMethodMatcher == null) return checkIfReverse(false);
        return checkIfReverse(listenerMethodMatcher.match(method));
    }

    @Override
    public boolean match(Object obj) {
        if (matchAll) return true;
        return listenerClass.containsKey(obj.getClass());
    }
}
