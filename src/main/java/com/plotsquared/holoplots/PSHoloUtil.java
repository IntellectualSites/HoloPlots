package com.plotsquared.holoplots;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.holoplots.config.Configuration;
import org.bukkit.Bukkit;

public class PSHoloUtil {

    private final HoloPlotsPlugin plugin;

    public PSHoloUtil(HoloPlotsPlugin plugin) {
        this.plugin = plugin;
    }

    public void updatePlot(final Plot plot) {
        if (!plot.isBasePlot() || !plot.hasOwner()) {
            plugin.provider().removeHologram(plot);
            return;
        }
        final Location signLoc = plot.getArea().getPlotManager().getSignLoc(plot);
        org.bukkit.Location loc = new org.bukkit.Location(
                Bukkit.getWorld(plot.getWorldName()), signLoc.getX() + 0.5, signLoc.getY() + 3, signLoc.getZ() + 0.5
        );
        plugin.translateAll(plot, HoloPlotsPlugin.HOLOGRAM_CAPTIONS).thenAcceptAsync(components -> {
            plugin.provider().createOrUpdateHologram(loc, plot, components, Configuration.SPAWN_PLAYER_HEAD);
        }, TaskManager.getPlatformImplementation()::task);
    }

}
