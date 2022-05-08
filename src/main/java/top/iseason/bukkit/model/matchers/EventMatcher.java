package top.iseason.bukkit.model.matchers;

import org.bukkit.configuration.ConfigurationSection;

import java.awt.*;
import java.util.HashSet;

public class EventMatcher extends BaseMatcher {
    private final HashSet<Class<?>> events = new HashSet<>();
    private final boolean matchAll;

    public EventMatcher(boolean matchAll) {
        this.matchAll = matchAll;
    }

    //todo: 完成反序列化
    public static BaseMatcher fromConfig(ConfigurationSection section) {
        return null;
    }

    public boolean addEvent(String event) {
        if (matchAll) return true;
        try {
            Class<?> aClass = Class.forName(event);
            if (!Event.class.isAssignableFrom(aClass)) {
                return false;
            }
            events.add(aClass);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean match(Object obj) {
        if (matchAll) return checkIfReverse(true);
        if (!(obj instanceof Event)) return checkIfReverse(false);
        Event event = (Event) obj;
        return checkIfReverse(events.contains(event.getClass()));
    }
}
