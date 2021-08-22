package com.plotsquared.holoplots;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public static Main THIS;
    public static IHoloUtil HOLO = null;
    private static final int BSTATS_ID = 6402;

    @Override
    public void onEnable() {
        Main.THIS = this;
        new PacketListener();
        HOLO = new PSHoloUtil();
        // Enable metrics
        new Metrics(this, BSTATS_ID);
    }
}
