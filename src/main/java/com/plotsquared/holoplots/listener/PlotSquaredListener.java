package com.plotsquared.holoplots.listener;

import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.events.PlayerClaimPlotEvent;
import com.plotsquared.core.events.PlotChangeOwnerEvent;
import com.plotsquared.core.events.PlotUnlinkEvent;
import com.plotsquared.core.events.post.PostPlayerAutoPlotEvent;
import com.plotsquared.core.events.post.PostPlotDeleteEvent;
import com.plotsquared.core.events.post.PostPlotMergeEvent;
import com.plotsquared.core.events.post.PostPlotUnlinkEvent;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.plotsquared.holoplots.HoloPlotsPlugin;
import it.unimi.dsi.fastutil.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlotSquaredListener {

    private static final long PENDING_THRESHOLD = 1000 * 30; // the server has 30 seconds to clear and unlink the plots

    private final HoloPlotsPlugin plugin;
    private final Map<Long, Pair<Long, Set<Plot>>> pendingUnlinks = Collections.synchronizedMap(new HashMap<>());


    public PlotSquaredListener(HoloPlotsPlugin plugin) {
        this.plugin = plugin;
        // clean up pending unlink operations
        TaskManager.getPlatformImplementation().taskRepeat(() -> {
            synchronized (pendingUnlinks) {
                pendingUnlinks.entrySet().removeIf(longPairEntry -> longPairEntry
                        .getValue().left() + PENDING_THRESHOLD > System.currentTimeMillis());
            }
        }, TaskTime.seconds(1));
    }

    @Subscribe
    public void onPlotClaim(PlayerClaimPlotEvent event) {
        TaskManager.runTaskLater(() -> plugin.holoUtil().updatePlot(event.getPlot()), TaskTime.ticks(20));
    }

    @Subscribe
    public void onPostPlotAuto(PostPlayerAutoPlotEvent event) {
        plugin.holoUtil().updatePlot(event.getPlot());
    }

    @Subscribe
    public void onPlotChangeOwner(PlotChangeOwnerEvent event) {
        TaskManager.runTaskLater(() -> plugin.holoUtil().updatePlot(event.getPlot()), TaskTime.ticks(20));
    }

    @Subscribe
    public void onPlotDelete(PostPlotDeleteEvent event) {
        if (!plugin.provider().removeHologram(event.getPlot())) {
            plugin.getLogger().warning("Failed to remove hologram for " + event.getPlot().getId() + " (Already removed?)");
        }
    }

    @Subscribe
    public void onPostPlotMerge(PostPlotMergeEvent event) {
        for (final Plot plot : event.getPlot().getConnectedPlots()) {
            plugin.holoUtil().updatePlot(plot);
        }
    }

    @Subscribe
    public void onPlotUnlink(PlotUnlinkEvent event) {
        Set<Plot> plots = new HashSet<>(event.getPlot().getConnectedPlots());
        synchronized (pendingUnlinks) {
            pendingUnlinks.put(plugin.hashPlot(event.getPlot()), Pair.of(System.currentTimeMillis(), plots));
        }
    }

    @Subscribe
    public void onPostPlotUnlink(PostPlotUnlinkEvent event) {
        plugin.holoUtil().updatePlot(event.getPlot());
        synchronized (pendingUnlinks) {
            final Pair<Long, Set<Plot>> result = pendingUnlinks.remove(plugin.hashPlot(event.getPlot()));
            if (result == null) {
                return;
            }
            result.right().forEach(plugin.holoUtil()::updatePlot);
        }
    }

}
