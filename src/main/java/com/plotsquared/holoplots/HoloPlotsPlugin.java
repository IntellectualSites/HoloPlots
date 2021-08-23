package com.plotsquared.holoplots;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class HoloPlotsPlugin extends JavaPlugin {

    private static final int BSTATS_ID = 6402;
    public static HoloPlotsPlugin THIS;
    public static IHoloUtil HOLO = null;

    @Override
    public void onEnable() {
        HoloPlotsPlugin.THIS = this;
        new PacketListener();
        HOLO = new PSHoloUtil();
        // Enable metrics
        new Metrics(this, BSTATS_ID);
    }

}
