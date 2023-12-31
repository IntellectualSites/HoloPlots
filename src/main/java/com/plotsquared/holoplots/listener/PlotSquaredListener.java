package com.plotsquared.holoplots.listener;

import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.events.PlayerClaimPlotEvent;
import com.plotsquared.core.events.PlotChangeOwnerEvent;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.PlotUnlinkEvent;
import com.plotsquared.core.events.post.PostPlayerAutoPlotEvent;
import com.plotsquared.core.events.post.PostPlotDeleteEvent;
import com.plotsquared.core.events.post.PostPlotMergeEvent;
import com.plotsquared.core.events.post.PostPlotUnlinkEvent;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.implementations.ServerPlotFlag;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.plotsquared.holoplots.HoloPlots;
import it.unimi.dsi.fastutil.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlotSquaredListener {

    private static final long PENDING_THRESHOLD = 1000 * 60; // the server has 60 seconds to clear and unlink the plots

    private final HoloPlots holoPlots;
    private final Map<Plot, Pair<Long, Set<Plot>>> pendingUnlinks = Collections.synchronizedMap(new HashMap<>());

    public PlotSquaredListener(HoloPlots holoPlots) {
        this.holoPlots = holoPlots;
        // clean up pending unlink operations
        TaskManager.runTaskRepeat(() -> {
            synchronized (pendingUnlinks) {
                pendingUnlinks.entrySet().removeIf(longPairEntry -> longPairEntry
                        .getValue().left() + PENDING_THRESHOLD > System.currentTimeMillis());
            }
        }, TaskTime.seconds(5));
    }

    @Subscribe
    public void onPlotClaim(PlayerClaimPlotEvent event) {
        TaskManager.runTaskLater(() -> this.holoPlots.updatePlot(event.getPlot()), TaskTime.ticks(20));
    }

    @Subscribe
    public void onPostPlotAuto(PostPlayerAutoPlotEvent event) {
        this.holoPlots.updatePlot(event.getPlot());
    }

    @Subscribe
    public void onPlotChangeOwner(PlotChangeOwnerEvent event) {
        TaskManager.runTaskLater(() -> this.holoPlots.updatePlot(event.getPlot()), TaskTime.ticks(20));
    }

    @Subscribe
    public void onPlotDelete(PostPlotDeleteEvent event) {
        if (!this.holoPlots.provider().removeHologram(event.getPlot())) {
            this.holoPlots.logger().warning("Failed to remove hologram for " + event.getPlot().getId() + " (Already removed?)");
        }
    }

    @Subscribe
    public void onPostPlotMerge(PostPlotMergeEvent event) {
        for (final Plot plot : event.getPlot().getConnectedPlots()) {
            this.holoPlots.updatePlot(plot);
        }
    }

    @Subscribe
    public void onPlotUnlink(PlotUnlinkEvent event) {
        Set<Plot> plots = new HashSet<>(event.getPlot().getConnectedPlots());
        synchronized (pendingUnlinks) {
            pendingUnlinks.put(event.getPlot(), Pair.of(System.currentTimeMillis(), plots));
        }
    }

    @Subscribe
    public void onPostPlotUnlink(PostPlotUnlinkEvent event) {
        this.holoPlots.updatePlot(event.getPlot());
        synchronized (pendingUnlinks) {
            final Pair<Long, Set<Plot>> result = pendingUnlinks.remove(event.getPlot());
            if (result == null) {
                return;
            }
            result.right().forEach(this.holoPlots::updatePlot);
        }
    }

    @Subscribe
    public void onPlotFlagAdd(PlotFlagAddEvent event) {
        if (event.getFlag() instanceof ServerPlotFlag) {
            TaskManager.runTaskLater(() -> this.holoPlots.updatePlot(event.getPlot()), TaskTime.ticks(20));
        }
    }


    @Subscribe
    public void onPlotFlagRemove(PlotFlagRemoveEvent event) {
        if (event.getFlag() instanceof ServerPlotFlag) {
            TaskManager.runTaskLater(() -> this.holoPlots.updatePlot(event.getPlot()), TaskTime.ticks(20));
        }
    }
}
