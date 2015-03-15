package com.empcraft.holoplots;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public static Main THIS;

    @Override
    public void onEnable() {
        Main.THIS = this;
        new PacketListener();
    }
}
