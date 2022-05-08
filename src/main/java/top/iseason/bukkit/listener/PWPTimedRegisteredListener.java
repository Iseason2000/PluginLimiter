/**
 * Created on 17 May 2014 by _MylesC
 * Copyright 2014
 */
package top.iseason.bukkit.listener;

import top.iseason.bukkit.PluginLimiter;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.TimedRegisteredListener;
import org.jetbrains.annotations.NotNull;

public class PWPTimedRegisteredListener extends TimedRegisteredListener {

    public PWPTimedRegisteredListener(Listener pluginListener, EventExecutor eventExecutor,
                                      EventPriority eventPriority, Plugin registeredPlugin, boolean listenCancelled) {
        super(pluginListener, eventExecutor, eventPriority, registeredPlugin, listenCancelled);
    }

    public void callEvent(@NotNull Event event) throws EventException {
        PluginLimiter instance = PluginLimiter.getInstance();
        if (instance == null) {
            super.callEvent(event);
            return;
        }
        if (instance.checkWorld(getPlugin(), event))
            return;
        super.callEvent(event);
    }

}
