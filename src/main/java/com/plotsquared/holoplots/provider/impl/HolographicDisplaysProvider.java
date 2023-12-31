package com.plotsquared.holoplots.provider.impl;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.holoplots.HoloPlots;
import com.plotsquared.holoplots.provider.HologramProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class HolographicDisplaysProvider implements HologramProvider {

    public static final String PLUGIN_NAME = "HolographicDisplays";

    private final HoloPlots holoPlots;
    private final Object2ObjectMap<Plot, Hologram> holograms = new Object2ObjectOpenHashMap<>();

    private final HolographicDisplaysAPI api;

    public HolographicDisplaysProvider(final Plugin plugin, final HoloPlots holoPlots) {
        this.holoPlots = holoPlots;
        this.api = HolographicDisplaysAPI.get(plugin);
    }

    @Override
    public void createHologram(
            final Location location, final Plot plot, final List<Component> lines,
            @Nullable final ItemStack skull
    ) {
        final Hologram hologram = this.api.createHologram(location);
        if (skull != null) {
            hologram.getLines().appendItem(skull);
        }
        for (final Component line : lines) {
            hologram.getLines().appendText(LEGACY_COMPONENT_SERIALIZER.serialize(line));
        }
        holograms.put(plot, hologram);
    }

    @Override
    public boolean updateHologram(final Plot plot, final List<Component> lines, @Nullable final ItemStack skull) {
        final Hologram hologram = holograms.get(plot);
        if (hologram == null) {
            return false;
        }
        if (hologram.isDeleted()) {
            holograms.remove(plot);
            return false;
        }
        // The easiest way is to clear the lines and create new ones
        hologram.getLines().clear();
        if (skull != null) {
            hologram.getLines().appendItem(skull);
        }
        for (final Component line : lines) {
            hologram.getLines().appendText(LEGACY_COMPONENT_SERIALIZER.serialize(line));
        }
        return true;
    }

    @Override
    public boolean removeHologram(final Plot plot) {
        final Hologram hologram = holograms.remove(plot);
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

    @Override
    public @Nullable String validateVersion(final String version) {
        return version.startsWith("3") ? null : "3.x";
    }

}
