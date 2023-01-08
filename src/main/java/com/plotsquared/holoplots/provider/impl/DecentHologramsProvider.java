package com.plotsquared.holoplots.provider.impl;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.holoplots.HoloPlotsPlugin;
import com.plotsquared.holoplots.provider.IHologramProvider;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

import java.util.List;

public class DecentHologramsProvider implements IHologramProvider {

    public static final String PLUGIN_NAME = "DecentHolograms";
    private static final String HOLOGRAM_NAME_FORMAT = "HoloPlots__%s_%s_%s";

    private final HoloPlotsPlugin plugin;

    public DecentHologramsProvider(final HoloPlotsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void createHologram(
            final Location location,
            final Plot plot,
            final List<Component> lines,
            final boolean floatingSkull
    ) {
        final Hologram hologram = DHAPI.createHologram(formatHologramName(plot), location, false);
        if (floatingSkull && plot.hasOwner()) {
            DHAPI.addHologramLine(hologram, plugin.getPlayerSkull(plot.getOwnerAbs()));
        }
        for (final Component line : lines) {
            DHAPI.addHologramLine(hologram, LEGACY_COMPONENT_SERIALIZER.serialize(line));
        }
        hologram.realignLines();
    }

    @Override
    public boolean updateHologram(final Plot plot, final List<Component> lines, final boolean floatingSkull) {
        final Hologram hologram = DHAPI.getHologram(formatHologramName(plot));
        if (hologram == null) {
            return false;
        }
        DHAPI.setHologramLines(hologram, lines.stream().map(LEGACY_COMPONENT_SERIALIZER::serialize).toList());
        if (floatingSkull && plot.hasOwner()) {
            DHAPI.insertHologramLine(hologram, 0, plugin.getPlayerSkull(plot.getOwnerAbs()));
        }
        hologram.realignLines();
        return true;
    }

    @Override
    public boolean removeHologram(final Plot plot) {
        final Hologram hologram = DHAPI.getHologram(formatHologramName(plot));
        if (hologram == null) {
            return false;
        }
        hologram.delete();
        return true;
    }

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    private String formatHologramName(final Plot plot) {
        return HOLOGRAM_NAME_FORMAT.formatted(
                plot.getArea().getWorldName(),
                plot.getArea().getId(),
                plot.getId()
        );
    }

}
