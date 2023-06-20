package com.projecki.fusion.util.region;

import com.projecki.fusion.util.BlockVector3;

/**
 * A simple implementation of {@link CuboidBlockRegion}.
 *
 * @param min The minimum block coordinate.
 * @param max The maximum block coordinate.
 * @since February 25, 2022
 * @author Andavin
 */
public record SimpleCuboidBlockRegion(BlockVector3 min, BlockVector3 max) implements CuboidBlockRegion {

    public SimpleCuboidBlockRegion(BlockVector3 min, BlockVector3 max) {
        this.min = min.getMinimum(max);
        this.max = max.getMaximum(min);
    }

    /**
     * Create a new {@link CuboidBlockRegion} with the given coordinates.
     *
     * @param minX The minimum X block coordinate.
     * @param minY The minimum Y block coordinate.
     * @param minZ The minimum Z block coordinate.
     * @param maxX The maximum X block coordinate.
     * @param maxY The maximum Y block coordinate.
     * @param maxZ The maximum Z block coordinate.
     */
    public SimpleCuboidBlockRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this(
                BlockVector3.at(
                        Math.min(minX, maxX),
                        Math.min(minY, maxY),
                        Math.min(minZ, maxZ)
                ),
                BlockVector3.at(
                        Math.max(minX, maxX),
                        Math.max(minY, maxY),
                        Math.max(minZ, maxZ)
                )
        );
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof CuboidBlockRegion r && this.equals(r);
    }
}
