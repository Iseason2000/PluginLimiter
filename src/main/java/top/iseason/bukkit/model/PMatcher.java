package top.iseason.bukkit.model;

import lombok.Data;
import top.iseason.bukkit.model.matchers.CommandMatcher;
import top.iseason.bukkit.model.matchers.EventMatcher;
import top.iseason.bukkit.model.matchers.ListenerMatcher;
import top.iseason.bukkit.model.matchers.LocationMatcher;


@Data
public class PMatcher {
    private CommandMatcher commandMatcher = null;
    private EventMatcher eventMatcher = null;
    private ListenerMatcher listenerMatcher = null;
    private LocationMatcher worldMatcher = null;

}
