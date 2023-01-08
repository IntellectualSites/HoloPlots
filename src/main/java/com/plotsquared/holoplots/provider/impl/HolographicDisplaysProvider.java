package com.plotsquared.holoplots.provider.impl;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.holoplots.HoloPlotsPlugin;
import com.plotsquared.holoplots.provider.IHologramProvider;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

import java.util.List;

public class HolographicDisplaysProvider implements IHologramProvider {

    public static final String PLUGIN_NAME = "HolographicDisplays";

    private final HoloPlotsPlugin plugin;
    private final Long2ObjectMap<Hologram> holograms = new Long2ObjectLinkedOpenHashMap<>();

    public HolographicDisplaysProvider(final HoloPlotsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void createHologram(
            final Location location, final Plot plot, final List<Component> lines,
            final boolean floatingSkull
    ) {
        final Hologram hologram = HologramsAPI.createHologram(plugin, location);
        if (floatingSkull && plot.hasOwner()) {
            hologram.appendItemLine(plugin.getPlayerSkull(plot.getOwnerAbs()));
        }
        for (final Component line : lines) {
            hologram.appendTextLine(LEGACY_COMPONENT_SERIALIZER.serialize(line));
        }
        holograms.put(plugin.hashPlot(plot), hologram);
    }

    @Override
    public boolean updateHologram(final Plot plot, final List<Component> lines, final boolean floatingSkull) {
        final Hologram hologram = holograms.get(plugin.hashPlot(plot));
        if (hologram == null) {
            return false;
        }
        if (hologram.isDeleted()) {
            holograms.remove(plugin.hashPlot(plot));
            return false;
        }
        // The easiest way is to clear the lines and create new ones
        hologram.clearLines();
        if (floatingSkull && plot.hasOwner()) {
            hologram.appendItemLine(plugin.getPlayerSkull(plot.getOwnerAbs()));
        }
        for (final Component line : lines) {
            hologram.appendTextLine(LEGACY_COMPONENT_SERIALIZER.serialize(line));
        }
        return true;
    }

    @Override
    public boolean removeHologram(final Plot plot) {
        final Hologram hologram = holograms.remove(plugin.hashPlot(plot));
        if (hologram == null) {
            return false;
        }
        hologram.delete();
        return hologram.isDeleted();
    }

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

}
