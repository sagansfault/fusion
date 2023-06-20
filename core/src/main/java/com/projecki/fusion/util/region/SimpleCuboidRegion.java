package com.projecki.fusion.util.region;

import com.projecki.fusion.util.Vector3;

/**
 * A simple implementation of {@link CuboidRegion}.
 *
 * @param min The minimum coordinate.
 * @param max The maximum coordinate.
 * @since February 25, 2022
 * @author Andavin
 */
public record SimpleCuboidRegion(Vector3 min, Vector3 max) implements CuboidRegion {

    public SimpleCuboidRegion(Vector3 min, Vector3 max) {
        this.min = min.getMinimum(max);
        this.max = max.getMaximum(min);
    }

    /**
     * Create a new {@link CuboidRegion} with the given coordinates.
     *
     * @param minX The minimum X coordinate.
     * @param minY The minimum Y coordinate.
     * @param minZ The minimum Z coordinate.
     * @param maxX The maximum X coordinate.
     * @param maxY The maximum Y coordinate.
     * @param maxZ The maximum Z coordinate.
     */
    public SimpleCuboidRegion(double minX, double minY, double minZ,
                              double maxX, double maxY, double maxZ) {
        this(
                Vector3.at(
                        Math.min(minX, maxX),
                        Math.min(minY, maxY),
                        Math.min(minZ, maxZ)
                ),
                Vector3.at(
                        Math.max(minX, maxX),
                        Math.max(minY, maxY),
                        Math.max(minZ, maxZ)
                )
        );
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof CuboidRegion r && this.equals(r);
    }
}
