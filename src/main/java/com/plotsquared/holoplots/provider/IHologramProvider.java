package com.plotsquared.holoplots.provider;

import com.plotsquared.core.plot.Plot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * This class is highly unstable in terms of eventual future modifications and therefore not recommended to be used (yet)
 */
@ApiStatus.Internal
public interface IHologramProvider {

    LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacySection();

    void createHologram(Location location, Plot plot, List<Component> lines, boolean floatingSkull);

    boolean updateHologram(Plot plot, List<Component> lines, boolean floatingSkull);

    boolean removeHologram(Plot plot);

    String getName();

    default void createOrUpdateHologram(Location location, Plot plot, List<Component> lines, boolean floatingSkull) {
        if (!updateHologram(plot, lines, floatingSkull)) {
            createHologram(location, plot, lines, floatingSkull);
        }
    }

}
