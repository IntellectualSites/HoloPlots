package com.empcraft.holoplots;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class Main extends JavaPlugin {
    public static Main THIS;
    public static IHoloUtil HOLO = null;

    @Override
    public void onEnable() {
        Main.THIS = this;
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().log(Level.SEVERE, "ProtocolLib required. Disabling HoloPlots.");
            getLogger().log(Level.SEVERE, "https://www.spigotmc.org/resources/protocollib.1997/");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("HolographicDisplays") == null) {
            getLogger().log(Level.SEVERE, "HolographicDisplays required. Disabling HoloPlots.");
            getLogger().log(Level.SEVERE, "https://dev.bukkit.org/projects/holographic-displays/files/2652670");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        new PacketListener();
        if (Bukkit.getPluginManager().getPlugin("PlotSquared") != null) {
            HOLO = new PSHoloUtil();
        }

    }
}
