package com.plotsquared.holoplots;

import com.plotsquared.core.PlotAPI;
import com.plotsquared.holoplots.config.Configuration;
import com.plotsquared.holoplots.listener.ChunkListener;
import com.plotsquared.holoplots.listener.PlotSquaredListener;
import com.plotsquared.holoplots.provider.BukkitHologramProviderResolver;
import com.plotsquared.holoplots.provider.impl.DecentHologramsProvider;
import com.plotsquared.holoplots.provider.impl.HolographicDisplaysProvider;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class HoloPlotsPlugin extends JavaPlugin {

    private static final int BSTATS_ID = 6402;

    @Override
    public void onEnable() {
        final String psVersion = Bukkit.getPluginManager().getPlugin("PlotSquared").getDescription().getVersion();
        if (!psVersion.startsWith("7") || psVersion.split("\\.")[1].charAt(0) < 1) {
            getLogger().severe("PlotSquared 7.1.0 or higher is required for HoloPlots to work!");
            getLogger().severe("Please update PlotSquared: https://www.spigotmc.org/resources/77506/");
            getLogger().severe("Disabling HoloPlots...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Configuration.load(new File(getDataFolder(), "settings.yml"), Configuration.class);
        Configuration.save(new File(getDataFolder(), "settings.yml"), Configuration.class);
        Configuration.deserializeSkullTextures(this.getLogger());

        final HoloPlots holoPlots = new DefaultHoloPlots(this, new BukkitHologramProviderResolver(this));

        getLogger().info("Using " + holoPlots.provider().getName() + " as the hologram provider");

        new PlotAPI().registerListener(new PlotSquaredListener(holoPlots));
        getServer().getPluginManager().registerEvents(new ChunkListener(holoPlots), this);

        // Enable metrics
        new Metrics(this, BSTATS_ID).addCustomChart(new SimplePie(
                "hologram_provider",
                () -> {
                    if (holoPlots.provider() instanceof DecentHologramsProvider || holoPlots.provider() instanceof HolographicDisplaysProvider) {
                        return holoPlots.provider().getName();
                    }
                    return null;
                }
        ));
    }

}
