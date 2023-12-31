package com.plotsquared.holoplots;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.holoplots.provider.HologramProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public interface HoloPlots {

    /**
     * Logger to use for logging purposes across {@link HologramProvider HologramProviders} etc.
     *
     * @return The logger
     */
    @NonNull Logger logger();

    /**
     * The currently used provider based on the other available plugins
     *
     * @return {@link HologramProvider} implementation
     */
    @NonNull HologramProvider provider();

    /**
     * Packs a {@link Plot} kinda-unique into a long by putting the {@link com.plotsquared.core.plot.PlotArea
     * PlotAreas} hash code into the first 4 bytes of the long and the {@link com.plotsquared.core.plot.PlotId PlotIds} hash
     * code into the following / last 4 bytes.
     *
     * @param plot The plot to hash "uniquely" across multiple worlds / {@link com.plotsquared.core.plot.PlotArea PlotAreas}
     * @return The hash of the plot
     */
    long hashPlot(@NonNull Plot plot);

    /**
     * Translates all lines as defined in {@link com.plotsquared.holoplots.config.Configuration#LINES}.
     * <p>
     * If one of the lines does not exist in the {@link com.plotsquared.core.configuration.caption.CaptionMap} it'll be treated
     * as static text to not be translated and simply parsed by MiniMessage. This allows for a more flexible configuration.
     * <p>
     * If one of the lines contains a
     * <a href="https://docs.advntr.dev/minimessage/dynamic-replacements.html#placeholders">Placeholder</a> the {@code username}
     * argument must be {@code non-null}. Otherwise, a {@link NullPointerException} is thrown.
     *
     * @param plot     The plot which the lines represent
     * @param username The plot owner's username (or fallbacks for e.g. server-plots)
     * @return An ordered list containing the translated lines as configured
     * @throws NullPointerException If one lines requires the username due to a placeholder, while no username caption was
     *                              provided
     */
    @NonNull List<@NonNull Component> translateLines(@NonNull Plot plot, @Nullable Caption username) throws NullPointerException;

    /**
     * Creates a textured head for the plots owner based on the passed parameters.
     *
     * @param ownerUuid The plot owners uuid or the reserved uuid for server / everyone-owned plots
     * @param username  The potential username, if the owner is indeed a player
     * @return The skull as an ItemStack, {@code null} if no skull should be spawned
     * @throws NullPointerException If the username is required, but passed as {@code null}
     */
    @Nullable ItemStack createOwnerSkull(@NonNull UUID ownerUuid, @Nullable String username) throws NullPointerException;

    /**
     * Schedule a new plot update, which basically spawns / delete / updates the hologram.
     *
     * @param plot The plot to update
     */
    void updatePlot(@NonNull Plot plot);

}
