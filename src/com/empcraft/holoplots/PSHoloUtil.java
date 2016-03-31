package com.empcraft.holoplots;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.generator.GridPlotWorld;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.UUIDHandler;

public class PSHoloUtil implements IHoloUtil {

    public static HashMap<Plot, Hologram> holograms = new HashMap<Plot, Hologram>();
    
    @Override
    public void updatePlayer(Player player, ChunkWrapper chunk) {
        String world = chunk.world;
        int bx = chunk.x << 4;
        int bz = chunk.y << 4;
        RegionWrapper region = new RegionWrapper(bx - 1, bx + 16, bz - 1, bz + 16);
        Set<PlotArea> areas = PS.get().getPlotAreas(world, region);
        if (areas.size() == 0) {
            return;
        }
        for (PlotArea area : areas) {
            if (!(area instanceof GridPlotWorld)) {
                continue;
            }
            GridPlotWorld gpw = (GridPlotWorld) area;
            Plot plot = gpw.getOwnedPlotAbs(new Location(area.worldname, bx, 0, bz + 1));
            if (plot == null) {
                plot = gpw.getOwnedPlotAbs(new Location(area.worldname, region.maxX, 0, region.maxZ + 1));
            }
            if (plot == null || !plot.isBasePlot()) {
                continue;
            }
            Location sign = area.getPlotManager().getSignLoc(gpw, plot);
            
            int x = sign.getX();
            int z = sign.getZ();
            
            org.bukkit.Location loc;
            if (x > region.minX && x < region.maxX && z > region.minZ && z < region.maxZ) {
                loc = new org.bukkit.Location(player.getWorld(), x + 0.5, sign.getY() + 3, z + 0.5);
                Hologram hologram = holograms.get(plot);
                if (hologram == null) {
                    hologram = HologramsAPI.createHologram(Main.THIS, loc);
                    holograms.put(plot, hologram);
                }
                hologram.clearLines();
                hologram.appendTextLine(translate(plot, C.OWNER_SIGN_LINE_1.s()));
                hologram.appendTextLine(translate(plot, C.OWNER_SIGN_LINE_2.s()));
                hologram.appendTextLine(translate(plot, C.OWNER_SIGN_LINE_3.s()));
                hologram.appendTextLine(translate(plot, C.OWNER_SIGN_LINE_4.s()));
                VisibilityManager visiblityManager = hologram.getVisibilityManager();
                visiblityManager.showTo(player);
            }
        }
    }
    
    private String translate(Plot plot, String string)  {
        String id = plot.getId().toString();
        String name;
        if (plot.owner == null) {
            name = "unowned";
        }
        else {
            name = UUIDHandler.getName(plot.owner);
        }
        if (name == null) {
            name = "unknown";
        }
        return ChatColor.translateAlternateColorCodes('&', string.replaceAll("%id%", id).replaceAll("%plr%", name).replace("Claimed", plot.owner == null ? "" : "Claimed"));
    }
}
