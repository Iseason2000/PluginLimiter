package top.iseason.bukkit.model.matchers;

import org.bukkit.event.Event;
import top.iseason.bukkit.PluginLimiter;

import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

public class EventMatcher extends BaseMatcher {
    private final HashSet<Class<?>> events = new HashSet<>();
    private final boolean matchAll;

    public EventMatcher(boolean matchAll) {
        this.matchAll = matchAll;
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

    public static EventMatcher fromConfig(List<String> stringList) {
        EventMatcher eventMatcher = new EventMatcher(stringList.isEmpty());
        for (String s : stringList) {
            if (!eventMatcher.addEvent(s)) {
                PluginLimiter.log(Level.WARNING, "Event not found " + s);
            }
        }
        return eventMatcher;
    }
}
