package com.plotsquared.holoplots;

/**
 * A representation of a chunk (x = chunkX y = chunkY, world = world name)
 * <p>
 * to convert standard location to chunk:
 * <ul>
 * <li>x = locationX >> 4</li>
 * <li>y = locationZ >> 4</li>
 * </ul>
 */
public record ChunkWrapper(int x, int y, String world) {

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        final ChunkWrapper other = (ChunkWrapper) object;
        return ((this.x == (other.x)) && (this.y == (other.y)) && (this.world.equals(other.world)));
    }

    @Override
    public int hashCode() {
        return (this.y + this.world + this.x).hashCode();
    }

}
