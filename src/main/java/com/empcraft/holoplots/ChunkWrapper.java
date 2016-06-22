package com.empcraft.holoplots;

public class ChunkWrapper {
    public final int    x;
    public final int    y;
    public final String world;

    /**
     * A representation of a chunk (x = chunkX y = chunkY, world = world name)<br>
     * <br>
     * to convert standard location to chunk:<br>
     * x = locationX >> 4<br>
     * y = locationZ >> 4
     */
    public ChunkWrapper(final int x, final int y, final String world) {
        this.x = x;
        this.y = y;
        this.world = world;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChunkWrapper other = (ChunkWrapper) obj;
        return ((this.x == (other.x)) && (this.y == (other.y)) && (this.world.equals(other.world)));
    }

    @Override
    public int hashCode() {
        return (this.y + this.world + this.x).hashCode();
    }
}
