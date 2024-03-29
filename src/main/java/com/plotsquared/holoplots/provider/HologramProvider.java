package com.plotsquared.holoplots.provider;

import com.plotsquared.core.plot.Plot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * API for implementing multiple hologram providers and using them with generalized logic.
 */
public interface HologramProvider {

    LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacySection();

    /**
     * Creates a new hologram for the given plot, at the given location with the passed content
     *
     * @param location The location where the hologram should be spawned
     * @param plot     The plot for which the hologram should be created
     * @param lines    The lines to display on the hologram
     * @param skull    The floating head, if it should spawn - otherwise {@code null}
     */
    void createHologram(Location location, Plot plot, List<Component> lines, @Nullable ItemStack skull) throws IllegalArgumentException;

    /**
     * Update an already existing hologram (identified by the plot)
     *
     * @param plot  The plot for which the hologram should be updated
     * @param lines The lines to display on the hologram
     * @param skull The floating head, if it should spawn - otherwise {@code null}
     * @return {@code true} if the already existing hologram was updated, {@code false} if no hologram exists for the plot
     */
    boolean updateHologram(Plot plot, List<Component> lines, @Nullable ItemStack skull);

    /**
     * Remove an existing hologram (identified by the plot)
     *
     * @param plot The plot of which hologram should be removed
     * @return {@code true} if the hologram was removed, {@code false} if there was no hologram to remove
     */
    boolean removeHologram(Plot plot);

    /**
     * @return The name of this provider implementation for formatting reasons
     */
    String getName();

    /**
     * Check if the installed provider has the correct and required version
     * @return {@code null} if the installed version is supported, otherwise a string representing the version requirements (e
     * .g. {@code 1.x})
     */
    @Nullable String validateVersion(String version);

    /**
     * Attempts to update the hologram for the passed plot - if no hologram exists for that plot a new one is created instead.
     *
     * @param location The location where the hologram should be spawned
     * @param plot     The plot for which the hologram should be created
     * @param lines    The lines to display on the hologram
     * @param skull    The floating head, if it should spawn - otherwise {@code null}
     */
    default void createOrUpdateHologram(Location location, Plot plot, List<Component> lines, @Nullable ItemStack skull) throws IllegalArgumentException {
        if (!updateHologram(plot, lines, skull)) {
            createHologram(location, plot, lines, skull);
        }
    }

}
