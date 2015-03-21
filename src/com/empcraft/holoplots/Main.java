package com.empcraft.holoplots;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public static Main THIS;
    public static IHoloUtil HOLO = null;

    @Override
    public void onEnable() {
        Main.THIS = this;
        new PacketListener();
        if (Bukkit.getPluginManager().getPlugin("PlotSquared") != null) {
            HOLO = new PSHoloUtil();
        }
        
    }
}
