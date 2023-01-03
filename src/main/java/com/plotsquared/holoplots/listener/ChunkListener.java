package com.plotsquared.holoplots.listener;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.generator.GridPlotWorld;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.holoplots.HoloPlotsPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.function.Consumer;

public class ChunkListener implements org.bukkit.event.Listener {

    private final HoloPlotsPlugin plugin;

    public ChunkListener(final HoloPlotsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent event) {
        processAffectedPlots(event.getChunk(), plugin.holoUtil()::updatePlot);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkUnload(ChunkUnloadEvent event) {
        processAffectedPlots(event.getChunk(), plugin.provider()::removeHologram);
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
            PlotId top = gridPlotWorld.getPlotManager().getPlotIdAbs(bx + 15, 0, bz + 15);
            if (top == null) { //Top corner of plot, or assume entirely road
                continue;
            }
            Plot plot = gridPlotWorld.getPlotAbs(top);
            if (plot == null) {
                continue;
            }
            consumer.accept(plot);
        }
    }

}
