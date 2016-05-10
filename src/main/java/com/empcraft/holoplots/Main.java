package com.empcraft.holoplots;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public static Main THIS;
    public static IHoloUtil HOLO = null;

    @Override
    public void onEnable() {
        Main.THIS = this;
		if(Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
			getLogger().log(Level.SEVERE, "ProtocolLib required. Disabling.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		if(Bukkit.getPluginManager().getPlugin("HolographicDisplays") == null) {
			getLogger().log(Level.SEVERE, "HolographicDisplays required. Disabling.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
        new PacketListener();
        if (Bukkit.getPluginManager().getPlugin("PlotSquared") != null) {
            HOLO = new PSHoloUtil();
        }
        
    }
}
