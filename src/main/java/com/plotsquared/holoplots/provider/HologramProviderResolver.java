package com.plotsquared.holoplots.provider;

import com.plotsquared.holoplots.HoloPlots;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

public interface HologramProviderResolver {

    Optional<@NonNull HologramProvider> findProvider(@NonNull HoloPlots holoPlots);

}
