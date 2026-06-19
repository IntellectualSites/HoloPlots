package com.plotsquared.holoplots.listener;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.generator.GridPlotWorld;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.holoplots.HoloPlots;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.function.Consumer;

public class ChunkListener implements org.bukkit.event.Listener {

    private final HoloPlots holoPlots;

    public ChunkListener(final HoloPlots holoPlots) {
        this.holoPlots = holoPlots;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent event) {
        processAffectedPlots(event.getChunk(), this.holoPlots::updatePlot);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkUnload(ChunkUnloadEvent event) {
        processAffectedPlots(event.getChunk(), this.holoPlots.provider()::removeHologram);
    }

    private void processAffectedPlots(Chunk chunk, Consumer<Plot> consumer) {
        int bx = chunk.getX() << 4;
        int bz = chunk.getZ() << 4;
        BlockVector3 pos1 = BlockVector3.at(bx - 1, 0, bz - 1);
        BlockVector3 pos2 = BlockVector3.at(bx + 16, 255, bz + 16);
        CuboidRegion region = new CuboidRegion(pos1, pos2);
        for (final PlotArea area : PlotSquared.get().getPlotAreaManager().getPlotAreas(chunk.getWorld().getName(), region)) {
            if (!(area instanceof GridPlotWorld gridPlotWorld)) {
                continue;
            }

            Set<Plot> seen = new HashSet<>();
            int[][] corners = {{bx, bz}, {bx + 15, bz}, {bx, bz + 15}, {bx + 15, bz + 15}};
            for (int[] corner : corners) {
                PlotId plotId = gridPlotWorld.getPlotManager().getPlotIdAbs(corner[0], 0, corner[1]);
                if (plotId == null) {
                    continue;
                }
                Plot plot = gridPlotWorld.getPlotAbs(plotId);
                // Because we check for 4 corners, the same plot may be returned multiple times, so we keep track of which ones we've already processed
                if (plot == null || !seen.add(plot)) {
                    continue;
                }

                // Only process if the plot's sign location falls within this chunk,
                // preventing the same plot from being processed by multiple chunk events
                Location signLoc = gridPlotWorld.getPlotManager().getSignLoc(plot);
                if (signLoc.getX() >> 4 != chunk.getX() || signLoc.getZ() >> 4 != chunk.getZ()) {
                    continue;
                }
                consumer.accept(plot);
            }
        }
    }

}
