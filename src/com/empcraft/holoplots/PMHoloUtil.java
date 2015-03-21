package com.empcraft.holoplots;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.gmail.filoghost.holographicdisplays.api.Hologram;



import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;


import com.worldcretornica.plotme_core.Plot;
import com.worldcretornica.plotme_core.PlotId;
import com.worldcretornica.plotme_core.PlotMeCoreManager;
import com.worldcretornica.plotme_core.PlotMe_Core;
import com.worldcretornica.plotme_core.api.IPlotMe_GeneratorManager;
import com.worldcretornica.plotme_core.bukkit.PlotMe_CorePlugin;
import com.worldcretornica.plotme_core.bukkit.api.IBukkitPlotMe_GeneratorManager;

public class PMHoloUtil implements IHoloUtil {

    public static HashMap<Plot, Hologram> holograms = new HashMap<Plot, Hologram>();
    private PlotMe_Core core;
    private PlotId pos1;
    private PlotId pos2;
    private PlotMeCoreManager instance;
    
    public PMHoloUtil() {
        Plugin plotme = Bukkit.getPluginManager().getPlugin("PlotMe-Core");
        this.core = ((PlotMe_CorePlugin) plotme).getAPI();
        this.instance = PlotMeCoreManager.getInstance();
        this.pos1 = new PlotId(0,0);
        this.pos2 = new PlotId(1,1);
    }
    
    public void updatePlayer(Player player, ChunkWrapper chunk) {
        String world = chunk.world;
        IBukkitPlotMe_GeneratorManager manager = (IBukkitPlotMe_GeneratorManager) core.getGenManager(world);
        Location bot1 = manager.getBottom(player.getWorld(), pos1);
        Location bot2 = manager.getBottom(player.getWorld(), pos2);
        int size = (bot2.getBlockX() - bot1.getBlockX());
        int bx = (chunk.x << 4) - 1;
        int bz = (chunk.y << 4) - 1;
        
        int tx = bx + 16;
        int tz = bz + 16;
        
        PlotId id = getId(size, bx - 16, bz - 16);
        Plot plot = instance.getPlotById(id, world);
        
        Location bot = manager.getBottom(player.getWorld(), id);
        Location top = manager.getTop(player.getWorld(), id);
        
        int x = bot.getBlockX();
        int z = bot.getBlockZ();
        
        org.bukkit.Location loc;
        if (x > bx && x <= tx && z > bz && z <= tz) {
            loc = new org.bukkit.Location(player.getWorld(), x + 0.5, 66 + 2, z + 0.5);
            Hologram hologram = holograms.get(plot);
            if (hologram == null) {
                hologram = HologramsAPI.createHologram(Main.THIS, loc);
                holograms.put(plot, hologram);
            }
            String owner = plot.getOwner();
            if (owner == null || owner.length() == 0) {
                owner = "unowned";
            }
            hologram.clearLines();
            hologram.appendTextLine("ID: " + id);
            hologram.appendTextLine(owner);
            VisibilityManager visiblityManager = hologram.getVisibilityManager();
            visiblityManager.showTo(player);
        }
    }
    
    private PlotId getId(int size, int x, int z) {
        int idx;
        int idz;
        if (x < 0) {
            idx = (x/size);
            x = size + (x % size);
        }
        else {
            idx = (x/size) + 1;
            x = (x % size);
        }
        if (z < 0) {
            idz = (z/size);
            z = size + (z % size);
        }
        else {
            idz = (z/size) + 1;
            z = (z % size);
        }
        return new PlotId(idx + 1, idz + 1);
    }
    
}
