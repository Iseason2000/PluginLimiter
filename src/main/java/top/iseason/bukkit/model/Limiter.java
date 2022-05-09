package top.iseason.bukkit.model;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;
import top.iseason.bukkit.model.matchers.PMatcher;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * 用于检测是否触发限制条件
 */

public class Limiter {


    private final boolean enabled;
    private final ArrayList<PMatcher> matchers = new ArrayList<>();
    private final boolean isReverse;

    public Limiter(ConfigurationSection section) {
        enabled = section.getBoolean("enable");
        isReverse = section.getBoolean("reverse");
        if (!enabled) return;
        for (String key : section.getKeys(false)) {
            if ("enable".equals(key) || "reverse".equals(key)) continue;
            ConfigurationSection cs = section.getConfigurationSection(key);
            if (cs == null) continue;
            matchers.add(new PMatcher(cs));
        }
    }

    public static Location getLocation(Event event) {
        if ((event instanceof PlayerEvent)) {
            return ((PlayerEvent) event).getPlayer().getLocation();
        }
        if ((event instanceof BlockEvent)) {
            return ((BlockEvent) event).getBlock().getLocation();
        }
        if ((event instanceof InventoryEvent)) {
            return ((InventoryEvent) event).getView().getPlayer().getLocation();
        }
        if ((event instanceof EntityEvent)) {
            return ((EntityEvent) event).getEntity().getLocation();
        }
        if ((event instanceof HangingEvent)) {
            return ((HangingEvent) event).getEntity().getLocation();
        }
        if ((event instanceof VehicleEvent)) {
            return ((VehicleEvent) event).getVehicle().getLocation();
        }
        if ((event instanceof WeatherEvent)) {
            return ((WeatherEvent) event).getWorld().getSpawnLocation();
        }
        if ((event instanceof WorldEvent)) {
            return ((WorldEvent) event).getWorld().getSpawnLocation();
        }
        return null;
    }

    boolean checkIfReverse(boolean result) {
        if (!enabled) return false;
        if (isReverse) return !result;
        return result;
    }

    public boolean matchCommand(String command, Location loc) {
        if (!enabled) return false;
        for (PMatcher matcher : matchers) {
            if (matcher.getLocationMatcher() == null && matcher.matchCommand(command)) {
                return checkIfReverse(true);
            }
            if (matcher.matchCommand(command) && matcher.matchLocation(loc)) {
                return checkIfReverse(true);
            }
        }
        return checkIfReverse(false);
    }

    public boolean matchEvent(Event event) {
        if (!enabled) return false;
        for (PMatcher matcher : matchers) {
            if (matcher.getLocationMatcher() == null && matcher.matchEvent(event)) {
                return checkIfReverse(true);
            }
            if (matcher.matchEvent(event) && matcher.matchLocation(getLocation(event))) {
                return checkIfReverse(true);
            }
        }
        return checkIfReverse(false);
    }

    public boolean matchListener(Listener listener, Method method, Event event) {
        if (!enabled) return false;
        for (PMatcher matcher : matchers) {
            if (matcher.getLocationMatcher() == null && matcher.matchListener(listener, method)) {
                return checkIfReverse(true);
            }
            if (matcher.matchListener(listener, method) && matcher.matchLocation(getLocation(event))) {
                return checkIfReverse(true);
            }
        }
        return checkIfReverse(false);
    }

    public boolean isEnabled() {
        return enabled;
    }

}

