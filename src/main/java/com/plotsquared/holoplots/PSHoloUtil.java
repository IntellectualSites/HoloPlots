package com.plotsquared.holoplots;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.eventbus.Subscribe;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.LocaleHolder;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlayerClaimPlotEvent;
import com.plotsquared.core.events.PlotChangeOwnerEvent;
import com.plotsquared.core.events.PlotDeleteEvent;
import com.plotsquared.core.events.PlotMergeEvent;
import com.plotsquared.core.generator.GridPlotWorld;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class PSHoloUtil implements IHoloUtil {

    public static HashMap<HoloPlotID, Hologram> holograms = new HashMap<>();

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
        areas:
        for (PlotArea area : areas) {
            if (!(area instanceof GridPlotWorld gridPlotWorld)) {
                continue;
            }
            PlotId bot = gridPlotWorld.getPlotManager().getPlotIdAbs(bx, 0, bz);
            PlotId top = gridPlotWorld.getPlotManager().getPlotIdAbs(bx + 15, 0, bz + 15);
            if (top == null) { //Top corner of plot, or assume entirely road
                continue;
            } else if (top.equals(bot)) {
                PlotId id = gridPlotWorld.getPlotManager().getPlotIdAbs(bx - 2, 0, bz - 2); //account for wall
                if (id != null && id.equals(bot)) { //Plot entirely contains chunk
                    continue;
                }
            }
            Plot plot = gridPlotWorld.getPlotAbs(top);
            if (plot == null) {
                continue;
            }
            Location sign = area.getPlotManager().getSignLoc(plot);

            int x = sign.getX();
            int z = sign.getZ();

            org.bukkit.Location loc;
            if (x > pos1.getX() && x < pos2.getX() && z > pos1.getZ() && z < pos2.getZ()) {
                if (!plot.hasOwner() || !plot.isBasePlot()) {
                    Iterator<Map.Entry<HoloPlotID, Hologram>> it = holograms.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<HoloPlotID, Hologram> entry = it.next();
                        if (entry.getKey().getId().equals(top)) {
                            entry.getValue().delete();
                            it.remove();
                            break areas;
                        }
                    }
                    continue;
                }

                loc = new org.bukkit.Location(player.getWorld(), x + 0.5, sign.getY() + 3, z + 0.5);

                HoloPlotID id = new HoloPlotID(plot.getId(), plot.getOwnerAbs());

                final Hologram hologram;
                if (!holograms.containsKey(id)) {
                    hologram = HologramsAPI.createHologram(HoloPlotsPlugin.THIS, loc);
                    holograms.put(id, hologram);
                } else {
                    Hologram holo = holograms.get(id);
                    if (holo.isDeleted()) {
                        hologram = HologramsAPI.createHologram(HoloPlotsPlugin.THIS, loc);
                        holograms.replace(id, hologram);
                    } else {
                        hologram = holo;
                    }
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

    @Subscribe
    public void onPlotClaim(PlayerClaimPlotEvent e) {
        final Plot plot = e.getPlot();
        final UUID uuid = e.getPlotPlayer().getUUID();
        TaskManager.runTaskLater(() -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) {
                return;
            }
            Location loc = plot.getArea().getPlotManager().getSignLoc(plot);
            updatePlayer(p, new ChunkWrapper(loc.getX() >> 4, loc.getZ() >> 4, loc.getWorldName()));
        }, TaskTime.ticks(20));
    }

    @Subscribe
    public void onPlotChangeOwner(PlotChangeOwnerEvent e) {
        final Plot plot = e.getPlot();
        final UUID uuid = e.getInitiator().getUUID();
        TaskManager.runTaskLater(() -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) {
                return;
            }
            Location loc = plot.getArea().getPlotManager().getSignLoc(plot);
            updatePlayer(p, new ChunkWrapper(loc.getX() >> 4, loc.getZ() >> 4, loc.getWorldName()));
        }, TaskTime.ticks(20));
    }

    @Subscribe
    public void onPlotDelete(PlotDeleteEvent e) {
        UUID owner = e.getPlot().getOwnerAbs();
        if (owner == null) {
            return;
        }
        HoloPlotID id = new HoloPlotID(e.getPlotId(), owner);
        Hologram hologram = PSHoloUtil.holograms.remove(id);
        if (hologram != null) {
            hologram.delete();
        }
    }

    // Go overboard with removing holograms when plots merge as it's likely more will be removed than will need to be created
    // again (by not testing for base plot etc)
    @Subscribe
    public void onPlotMerge(PlotMergeEvent e) {
        UUID owner = e.getPlot().getOwnerAbs();
        if (owner == null) {
            return;
        }
        HoloPlotID id = new HoloPlotID(e.getPlot().getId(), owner);
        Hologram hologram = PSHoloUtil.holograms.remove(id);
        if (hologram != null) {
            hologram.delete();
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
