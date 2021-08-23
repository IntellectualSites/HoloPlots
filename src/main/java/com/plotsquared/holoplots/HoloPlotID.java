package com.plotsquared.holoplots;

import com.plotsquared.core.plot.PlotId;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;
import java.util.UUID;

public class HoloPlotID {

    private final PlotId id;
    private final UUID owner;
    private final int hash;

    public HoloPlotID(PlotId id, UUID owner) {
        this.id = id;
        this.owner = owner;
        this.hash = Objects.hash(this.getId(), this.getOwner());
    }

    public UUID getOwner() {
        return owner;
    }

    public PlotId getId() {
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.hashCode() != obj.hashCode()) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HoloPlotID other = (HoloPlotID) obj;
        return this.getId() == other.getId() && this.getOwner() == other.getOwner();
    }

    @Override
    public @NonNull String toString() {
        return this.getId().toString() + ":" + this.getOwner().toString();
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

}
