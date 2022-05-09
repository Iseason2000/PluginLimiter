/**
 * Created on 17 May 2014 by _MylesC
 * Copyright 2014
 */
package top.iseason.bukkit.listener;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import top.iseason.bukkit.ConfigManager;

public class PLRegisteredListener extends RegisteredListener {
    public PLRegisteredListener(Listener listener, EventExecutor executor, EventPriority priority, Plugin plugin, boolean ignoreCancelled) {
        super(listener, executor, priority, plugin, ignoreCancelled);
    }

    public void callEvent(@NotNull Event event) throws EventException {
        if (ConfigManager.matchEvent(getPlugin(), event)) {
            return;
        }
        super.callEvent(event);
    }

}
