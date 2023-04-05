package top.iseason.bukkit.model.matchers;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;
import java.util.Objects;


public class PMatcher {
    private final boolean isReverse;
    private final boolean enabled;
    private CommandMatcher commandMatcher = null;
    private EventMatcher eventMatcher = null;
    private ListenerMatcher listenerMatcher = null;
    private LocationMatcher locationMatcher = null;

    public PMatcher(ConfigurationSection section) {
        enabled = section.getBoolean("enable");
        isReverse = section.getBoolean("reverse");
        if (!enabled) return;

        if (section.contains("commands")) {
            commandMatcher = CommandMatcher.fromConfig(section.getStringList("commands"));
        }
        if (section.contains("commands-R")) {
            commandMatcher = CommandMatcher.fromConfig(section.getStringList("commands-R"));
            commandMatcher.setReverse(true);
        }
        if (section.contains("events")) {
            eventMatcher = EventMatcher.fromConfig(section.getStringList("events"));
        }
        if (section.contains("events-R")) {
            eventMatcher = EventMatcher.fromConfig(section.getStringList("events-R"));
            eventMatcher.setReverse(true);
        }
        if (section.contains("listeners")) {
            ConfigurationSection commands = section.getConfigurationSection("listeners");
            listenerMatcher = ListenerMatcher.fromConfig(Objects.requireNonNull(commands));
        }
        if (section.contains("listeners-R")) {
            ConfigurationSection commands = section.getConfigurationSection("listeners-R");
            listenerMatcher = ListenerMatcher.fromConfig(Objects.requireNonNull(commands));
            listenerMatcher.setReverse(true);
        }
        if (section.contains("locations")) {
            locationMatcher = LocationMatcher.fromConfig(section.getStringList("locations"));
        }
        if (section.contains("locations-R")) {
            locationMatcher = LocationMatcher.fromConfig(section.getStringList("locations-R"));
            locationMatcher.setReverse(true);
        }
    }

    public boolean matchCommand(String obj) {
        if (commandMatcher == null) return false;
        return checkIfReverse(commandMatcher.match(obj));
    }

    public boolean matchEvent(Event event) {
        if (eventMatcher == null) return false;
        return checkIfReverse(eventMatcher.match(event));
    }

    public boolean matchListener(Listener obj, Method method) {
        if (listenerMatcher == null) return false;
        return checkIfReverse(listenerMatcher.match(obj, method));
    }

    public boolean matchLocation(Location obj) {
        if (locationMatcher == null) return false;
        if (obj == null) return false;
        return checkIfReverse(locationMatcher.match(obj));
    }

    boolean checkIfReverse(boolean result) {
        if (!enabled) return false;
        if (isReverse) return !result;
        return result;
    }

    public CommandMatcher getCommandMatcher() {
        return commandMatcher;
    }

    public EventMatcher getEventMatcher() {
        return eventMatcher;
    }

    public ListenerMatcher getListenerMatcher() {
        return listenerMatcher;
    }

    public LocationMatcher getLocationMatcher() {
        return locationMatcher;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
