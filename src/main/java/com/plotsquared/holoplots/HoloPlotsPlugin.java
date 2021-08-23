package com.plotsquared.holoplots;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.events.PlotDeleteEvent;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

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
        new PlotAPI().registerListener(HOLO);
    }

}
