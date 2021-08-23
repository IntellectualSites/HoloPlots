package com.plotsquared.holoplots;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.LocaleHolder;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.generator.GridPlotWorld;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PSHoloUtil implements IHoloUtil {

    public static HashMap<Plot, Hologram> holograms = new HashMap<>();

    @Override
    public void updatePlayer(Player player, ChunkWrapper chunkWrapper) {
        String world = chunkWrapper.world;
        int bx = chunkWrapper.x << 4;
        int bz = chunkWrapper.y << 4;
        BlockVector3 pos1 = BlockVector3.at(bx - 1, 0, bz - 1);
        BlockVector3 pos2 = BlockVector3.at(bx + 16, 255, bz + 16);
        CuboidRegion region = new CuboidRegion(pos1, pos2);
        PlotArea[] areas = PlotSquared.get().getPlotAreaManager().getPlotAreas(world, region);
        if (areas.length == 0) {
            return;
        }
        for (PlotArea area : areas) {
            if (!(area instanceof GridPlotWorld gridPlotWorld)) {
                continue;
            }
            Plot plot = gridPlotWorld.getOwnedPlotAbs(Location.at(area.getWorldName(), BlockVector3.at(bx, 0, bz + 1), 0, 0));
            if (plot == null) {
                plot = gridPlotWorld.getOwnedPlotAbs(Location.at(
                        area.getWorldName(),
                        BlockVector3.at(pos2.getX(), 0, pos2.getZ() + 1),
                        0,
                        0
                ));
            }
            if (plot == null || !plot.isBasePlot()) {
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
                    hologram = HologramsAPI.createHologram(HoloPlotsPlugin.THIS, loc);
                    holograms.put(plot, hologram);
                } else {
                    hologram = holograms.get(plot);
                }

                // Call translate async as it might do HTTP requests
                final Plot finalPlot = plot;
                TaskManager.getPlatformImplementation().taskAsync(() -> {
                    String line1 = translate(finalPlot, TranslatableCaption.of("signs.owner_sign_line_1"));
                    String line2 = translate(finalPlot, TranslatableCaption.of("signs.owner_sign_line_2"));
                    String line3 = translate(finalPlot, TranslatableCaption.of("signs.owner_sign_line_3"));
                    String line4 = translate(finalPlot, TranslatableCaption.of("signs.owner_sign_line_4"));
                    TaskManager.getPlatformImplementation().task(() -> {
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

    private String translate(Plot plot, Caption caption) {
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
        return BukkitUtil.LEGACY_COMPONENT_SERIALIZER
                .serialize(BukkitUtil.MINI_MESSAGE.parse(caption.getComponent(LocaleHolder.console()), Template.of("id", id),
                        Template.of("owner", name)
                ))
                .replace("Claimed", plot.getOwnerAbs() == null ? "" : "Claimed");
    }

}
