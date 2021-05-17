package com.empcraft.holoplots;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.generator.GridPlotWorld;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PSHoloUtil implements IHoloUtil {

    public static HashMap<Plot, Hologram> holograms = new HashMap<>();

    @Override
    public void updatePlayer(Player player, ChunkWrapper chunk) {
        String world = chunk.world;
        int bx = chunk.x << 4;
        int bz = chunk.y << 4;
        BlockVector3 pos1 = BlockVector3.at(bx - 1, 0, bz - 1);
        BlockVector3 pos2 = BlockVector3.at(bx + 16, 255, bz + 16);
        CuboidRegion region = new CuboidRegion(pos1, pos2);
        Set<PlotArea> areas = PlotSquared.get().getPlotAreas(world, region);
        if (areas.size() == 0) {
            return;
        }
        for (PlotArea area : areas) {
            if (!(area instanceof GridPlotWorld)) {
                continue;
            }
            GridPlotWorld gpw = (GridPlotWorld) area;
            Plot plot = gpw.getOwnedPlotAbs(new Location(area.getWorldName(), bx, 0, bz + 1));
            if (plot == null) {
                plot = gpw.getOwnedPlotAbs(new Location(area.getWorldName(), pos2.getX(), 0, pos2.getZ() + 1));
            }
            if (plot == null || !plot.isBasePlot()) {
                if (plot == null) {
                    for (Map.Entry<Plot, Hologram> e: holograms.entrySet()) {
                        if (e.getKey().getArea().equals(area)) {
                            e.getValue().delete();
                            holograms.remove(e.getKey());
                            break;
                        }
                    }
                }
                continue;
            }
            Location sign = area.getPlotManager().getSignLoc(plot);

            int x = sign.getX();
            int z = sign.getZ();

            org.bukkit.Location loc;
            if (x > pos1.getX() && x < pos2.getX() && z > pos1.getZ() && z < pos2.getZ()) {
                loc = new org.bukkit.Location(player.getWorld(), x + 0.5, sign.getY() + 3, z + 0.5);

                final Hologram hologram;
                if (!holograms.containsKey(plot)) {
                    hologram = HologramsAPI.createHologram(Main.THIS, loc);
                    holograms.put(plot, hologram);
                } else {
                    hologram = holograms.get(plot);
                }

                // Call translate async as it might do HTTP requests
                final Plot finalPlot = plot;
                TaskManager.IMP.taskAsync(() -> {
                    String line1 = translate(finalPlot, Captions.OWNER_SIGN_LINE_1.getTranslated());
                    String line2 = translate(finalPlot, Captions.OWNER_SIGN_LINE_2.getTranslated());
                    String line3 = translate(finalPlot, Captions.OWNER_SIGN_LINE_3.getTranslated());
                    String line4 = translate(finalPlot, Captions.OWNER_SIGN_LINE_4.getTranslated());
                    TaskManager.IMP.task(() -> {
                        hologram.clearLines();
                        hologram.appendTextLine(line1);
                        hologram.appendTextLine(line2);
                        hologram.appendTextLine(line3);
                        hologram.appendTextLine(line4);
                        hologram.getVisibilityManager().showTo(player);
                    });
                });
            }
        }
    }

    private String translate(Plot plot, String string) {
        String id = plot.getId().toString();
        String name;
        if (plot.getOwnerAbs() == null) {
            name = "unowned";
        } else {
            name = Bukkit.getOfflinePlayer(plot.getOwnerAbs()).getName();
        }
        if (name == null) {
            name = "unknown";
        }
        return ChatColor.translateAlternateColorCodes('&', string.replaceAll("%id%", id)
                .replaceAll("%plr%", name).replace("Claimed", plot.getOwners() == null ? "" : "Claimed"));
    }
}
