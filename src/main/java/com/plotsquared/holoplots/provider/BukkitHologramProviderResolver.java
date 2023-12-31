package com.plotsquared.holoplots.provider;

import com.plotsquared.holoplots.HoloPlots;
import com.plotsquared.holoplots.provider.impl.DecentHologramsProvider;
import com.plotsquared.holoplots.provider.impl.HolographicDisplaysProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class BukkitHologramProviderResolver implements HologramProviderResolver {

    private static final Map<String, BiFunction<Plugin, HoloPlots, HologramProvider>> SUPPORTED_PROVIDERS = Map.of(
            HolographicDisplaysProvider.PLUGIN_NAME, HolographicDisplaysProvider::new,
            DecentHologramsProvider.PLUGIN_NAME, (pl, holoPlots) -> new DecentHologramsProvider()
    );

    private final Plugin plugin;

    public BukkitHologramProviderResolver(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<@NonNull HologramProvider> findProvider(@NonNull final HoloPlots holoPlots) {
        return SUPPORTED_PROVIDERS.entrySet()
                .stream().filter(entry -> Bukkit.getPluginManager().isPluginEnabled(entry.getKey()))
                .findFirst().map(entry -> entry.getValue().apply(this.plugin, holoPlots));
    }

}
