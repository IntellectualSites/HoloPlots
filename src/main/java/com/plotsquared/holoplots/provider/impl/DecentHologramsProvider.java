package com.plotsquared.holoplots.provider.impl;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.holoplots.provider.HologramProvider;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;

public class DecentHologramsProvider implements HologramProvider {

    public static final String PLUGIN_NAME = "DecentHolograms";
    private static final String HOLOGRAM_NAME_FORMAT = "HoloPlots__%s_%s_%s";

    public DecentHologramsProvider() {
    }

    @Override
    public void createHologram(
            final Location location,
            final Plot plot,
            final List<Component> lines,
            @Nullable final ItemStack skull
    ) throws IllegalArgumentException {
        final Hologram hologram = DHAPI.createHologram(formatHologramName(plot), location, false);
        if (skull != null) {
            DHAPI.addHologramLine(hologram, skull);
        }
        for (final Component line : lines) {
            DHAPI.addHologramLine(hologram, LEGACY_COMPONENT_SERIALIZER.serialize(line));
        }
        hologram.realignLines();
    }

    @Override
    public boolean updateHologram(final Plot plot, final List<Component> lines, @Nullable final ItemStack skull) {
        final Hologram hologram = DHAPI.getHologram(formatHologramName(plot));
        if (hologram == null) {
            return false;
        }
        DHAPI.setHologramLines(hologram, lines.stream().map(LEGACY_COMPONENT_SERIALIZER::serialize).toList());
        if (skull != null) {
            DHAPI.insertHologramLine(hologram, 0, skull);
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

    @Override
    public String validateVersion(final String version) {
        final String[] segments = version.split("\\.");
        // If the plugin uses semver versioning (as it should)
        if (segments.length >= 3) {
            try {
                // Make sure it's at least 2.0.3 (introduction of DHAPI class), if running 2.1.x or higher we don't need to
                // validate the patch version - substring to mitigate possible SNAPSHOT suffix
                if (Integer.parseInt(segments[0]) >= 2 &&
                        (Integer.parseInt(segments[1]) != 0 || Integer.parseInt(segments[2].substring(0, 1)) >= 3)) {
                    return null;
                }
            } catch (NumberFormatException ignored) {
                // fallthrough
            }
        }
        // We have to guess and bet, that the user isn't running 3 patch versions behind
        return version.charAt(0) > 2 ? null : ">=2.0.3";
    }

    private String formatHologramName(final Plot plot) {
        return HOLOGRAM_NAME_FORMAT.formatted(
                Objects.requireNonNull(plot.getArea(), "PlotArea is null").getWorldName(),
                plot.getArea().getId(),
                plot.getId().toDashSeparatedString()
        );
    }

}
