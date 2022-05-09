package top.iseason.bukkit.model.matchers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;
import top.iseason.bukkit.PluginLimiter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class LocationMatcher extends BaseMatcher {
    private final ArrayList<Area> areas = new ArrayList<>();
    private final boolean matchAll;

    public LocationMatcher(boolean matchAll) {
        this.matchAll = matchAll;
    }

    public static Location toLocation(World world, String loc) {
        String[] split = loc.split(",");
        if (split.length != 3) return null;
        try {
            return new Location(world, NumberConversions.toDouble(split[0]), NumberConversions.toDouble(split[1]), NumberConversions.toDouble(split[2]));
        } catch (Exception e) {
            return null;
        }
    }

    public static LocationMatcher fromConfig(List<String> stringList) {
        LocationMatcher locationMatcher = new LocationMatcher(stringList.isEmpty());
        for (String s : stringList) {
            if (!locationMatcher.addArea(s)) {
                PluginLimiter.log(Level.WARNING, "Location not found " + s);
            }
        }
        return locationMatcher;
    }

    //World:x1,y1,z1:x2,y2,z2
    //World
    public boolean addArea(String locationStr) {
        if (matchAll) checkIfReverse(true);
        String[] split = locationStr.split(":");
        if (split.length == 3) {
            World world = Bukkit.getWorld(split[0]);
            if (world == null) return false;
            Location loc1 = toLocation(world, split[1]);
            if (loc1 == null) return false;
            Location loc2 = toLocation(world, split[2]);
            if (loc2 == null) return false;
            areas.add(new Area(world, loc1, loc2));
            return true;
        }
        World world = Bukkit.getWorld(locationStr);
        if (world == null) return false;
        areas.add(new Area(world));
        return true;
    }

    @Override
    public boolean match(Object obj) {
        if (matchAll) return checkIfReverse(true);
        if (!(obj instanceof Location)) return checkIfReverse(false);
        Location location = (Location) obj;
        for (Area area : areas) {
            if (area.checkLocationIn(location)) {
                return checkIfReverse(true);
            }
        }
        return checkIfReverse(false);
    }

    private class Area {
        World world;
        Location loc1 = null;
        Location loc2 = null;
        boolean isWholeWorld;

        Area(World world, Location loc1, Location loc2) {
            this.world = world;
            this.loc1 = loc1;
            this.loc2 = loc2;
            isWholeWorld = false;
        }

        Area(World world) {
            this.world = world;
            isWholeWorld = true;
        }

        public boolean checkLocationIn(Location loc) {
            if (!world.equals(loc.getWorld())) {
                return false;
            }
            if (isWholeWorld) return true;
            Location midLoc = loc1.clone().add(loc2).multiply(0.5);
            if (Math.abs(loc.getX() - midLoc.getX()) > Math.abs(loc2.getX() - loc1.getX()) * 0.5D) {
                return false;
            }
            if (Math.abs(loc.getY() - midLoc.getY()) > Math.abs(loc2.getY() - loc1.getY()) * 0.5D) {
                return false;
            }
            return !(Math.abs(loc.getZ() - midLoc.getZ()) > Math.abs(loc2.getZ() - loc1.getZ()) * 0.5D);
        }
    }
}
