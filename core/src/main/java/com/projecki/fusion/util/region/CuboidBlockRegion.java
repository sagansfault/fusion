package com.projecki.fusion.util.region;

import com.projecki.fusion.util.BlockVector3;

/**
 * A basic area that encapsulates a region of blocks.
 *
 * @since February 25, 2022
 * @author Andavin
 */
public interface CuboidBlockRegion {

    /**
     * The minimum point of this region.
     *
     * @return The minimum point.
     */
    BlockVector3 min();

    /**
     * The maximum point of this region.
     *
     * @return The maximum point.
     */
    BlockVector3 max();

    /**
     * The minimum X block coordinate of this region.
     *
     * @return The minimum X block coordinate.
     */
    default int minX() {
        return this.min().x();
    }

    /**
     * The minimum Y block coordinate of this region.
     *
     * @return The minimum Y block coordinate.
     */
    default int minY() {
        return this.min().y();
    }

    /**
     * The minimum Z block coordinate of this region.
     *
     * @return The minimum Z block coordinate.
     */
    default int minZ() {
        return this.min().z();
    }

    /**
     * The maximum X block coordinate of this region.
     *
     * @return The maximum X block coordinate.
     */
    default int maxX() {
        return this.max().x();
    }

    /**
     * The maximum Y block coordinate of this region.
     *
     * @return The maximum Y block coordinate.
     */
    default int maxY() {
        return this.max().y();
    }

    /**
     * The maximum Z block coordinate of this region.
     *
     * @return The maximum Z block coordinate.
     */
    default int maxZ() {
        return this.max().z();
    }

    /**
     * Check whether the specified position is contained
     * with this {@link CuboidBlockRegion}.
     *
     * @param pos The {@link BlockVector3 block position}.
     * @return If the coordinates are contained with this {@link CuboidBlockRegion}.
     */
    default boolean contains(BlockVector3 pos) {
        return this.contains(pos.x(), pos.y(), pos.z());
    }

    /**
     * Check whether the specified coordinates are contained
     * with this {@link CuboidBlockRegion}.
     *
     * @param x The X block coordinate.
     * @param y The Y block coordinate.
     * @param z The Z block coordinate.
     * @return If the coordinates are contained with this {@link CuboidBlockRegion}.
     */
    default boolean contains(int x, int y, int z) {
        return this.minX() <= x && x <= this.maxX() &&
                this.minY() <= y && y <= this.maxY() &&
                this.minZ() <= z && z <= this.maxZ();
    }

    /**
     * Check whether this {@link CuboidBlockRegion} intersects the specified
     * {@link CuboidBlockRegion} at any point.
     *
     * @param o The {@link CuboidBlockRegion} to check intersection with.
     * @return If this intersects the given {@link CuboidBlockRegion}.
     */
    default boolean intersects(CuboidBlockRegion o) {
        return this.minX() < o.maxX() && this.maxX() > o.minX() &&
                this.minY() < o.maxY() && this.maxY() > o.minY() &&
                this.minZ() < o.maxZ() && this.maxZ() > o.minZ();
    }

    /**
     * Create a new {@link CuboidBlockRegion} that represents the intersection
     * of this and the specified {@link CuboidBlockRegion}.
     *
     * @param o The {@link CuboidBlockRegion} to find the intersection with.
     * @return The intersection {@link CuboidBlockRegion}.
     * @see CuboidBlockRegion#of(int, int, int, int, int, int)
     */
    default CuboidBlockRegion intersection(CuboidBlockRegion o) {
        return this.intersection(o, CuboidBlockRegion::of);
    }

    /**
     * Create a new {@link CuboidBlockRegion} that represents the intersection
     * of this and the specified {@link CuboidBlockRegion}.
     *
     * @param o The {@link CuboidBlockRegion} to find the intersection with.
     * @param factory The {@link BlockRegionFunction} to use to
     *                create the new {@link CuboidBlockRegion}.
     * @return The intersection {@link CuboidBlockRegion}.
     * @see BlockRegionFunction
     */
    default CuboidBlockRegion intersection(CuboidBlockRegion o, BlockRegionFunction factory) {
        return factory.apply(
                Math.min(this.minX(), o.minX()),
                Math.min(this.minY(), o.minY()),
                Math.min(this.minZ(), o.minZ()),
                Math.max(this.maxX(), o.maxX()),
                Math.max(this.maxY(), o.maxY()),
                Math.max(this.maxZ(), o.maxZ())
        );
    }

    /**
     * Create a new {@link CuboidBlockRegion} that encompasses this and
     * the specified {@link CuboidBlockRegion}.
     *
     * @param o The {@link CuboidBlockRegion} to encompass.
     * @return The encompassing {@link CuboidBlockRegion}.
     * @see CuboidBlockRegion#of(int, int, int, int, int, int)
     */
    default CuboidBlockRegion union(CuboidBlockRegion o) {
        return this.union(o, CuboidBlockRegion::of);
    }

    /**
     * Create a new {@link CuboidBlockRegion} that encompasses this and
     * the specified {@link CuboidBlockRegion}.
     *
     * @param o The {@link CuboidBlockRegion} to encompass.
     * @param factory The {@link BlockRegionFunction} to use to
     *                create the new {@link CuboidBlockRegion}.
     * @return The encompassing {@link CuboidBlockRegion}.
     * @see BlockRegionFunction
     */
    default CuboidBlockRegion union(CuboidBlockRegion o, BlockRegionFunction factory) {
        return factory.apply(
                Math.min(this.minX(), o.minX()),
                Math.min(this.minY(), o.minY()),
                Math.min(this.minZ(), o.minZ()),
                Math.max(this.maxX(), o.maxX()),
                Math.max(this.maxY(), o.maxY()),
                Math.max(this.maxZ(), o.maxZ())
        );
    }

    /**
     * Create a new {@link CuboidBlockRegion} that is expanded on
     * all sides by the specified amount.
     *
     * @param amount The amount to expand on all sides.
     * @return The expanded {@link CuboidBlockRegion}.
     */
    default CuboidBlockRegion expand(int amount) {
        return this.expand(amount, amount, amount, CuboidBlockRegion::of);
    }

    /**
     * Create a new {@link CuboidBlockRegion} that is expanded on
     * all sides by the specified amount.
     *
     * @param amount The amount to expand on all sides.
     * @param factory The {@link BlockRegionFunction} to use to
     *                create the new {@link CuboidBlockRegion}.
     * @return The expanded {@link CuboidBlockRegion}.
     */
    default CuboidBlockRegion expand(int amount, BlockRegionFunction factory) {
        return this.expand(amount, amount, amount, factory);
    }

    /**
     * Create a new {@link CuboidBlockRegion} that is expanded on
     * each side by the specified amounts.
     *
     * @param x The amount to expand on the X-axis.
     * @param y The amount to expand on the Y-axis.
     * @param z The amount to expand on the Z-axis.
     * @return The expanded {@link CuboidBlockRegion}.
     */
    default CuboidBlockRegion expand(int x, int y, int z) {
        return this.expand(x, y, z, CuboidBlockRegion::of);
    }

    /**
     * Create a new {@link CuboidBlockRegion} that is expanded on
     * each side by the specified amounts.
     *
     * @param x The amount to expand on the X-axis.
     * @param y The amount to expand on the Y-axis.
     * @param z The amount to expand on the Z-axis.
     * @param factory The {@link BlockRegionFunction} to use to
     *                create the new {@link CuboidBlockRegion}.
     * @return The expanded {@link CuboidBlockRegion}.
     */
    default CuboidBlockRegion expand(int x, int y, int z, BlockRegionFunction factory) {
        return factory.apply(this.min().subtract(x, y, z), this.max().add(x, y, z));
    }

    /**
     * Create a new {@link CuboidBlockRegion} that is contracted on
     * all sides by the specified amount.
     *
     * @param amount The amount to contract on all sides.
     * @return The contracted {@link CuboidBlockRegion}.
     */
    default CuboidBlockRegion contract(int amount) {
        return this.contract(amount, amount, amount, CuboidBlockRegion::of);
    }

    /**
     * Create a new {@link CuboidBlockRegion} that is contracted on
     * all sides by the specified amount.
     *
     * @param amount The amount to contract on all sides.
     * @param factory The {@link BlockRegionFunction} to use to
     *                create the new {@link CuboidBlockRegion}.
     * @return The contracted {@link CuboidBlockRegion}.
     */
    default CuboidBlockRegion contract(int amount, BlockRegionFunction factory) {
        return this.contract(amount, amount, amount, factory);
    }

    /**
     * Create a new {@link CuboidBlockRegion} that is contracted on
     * each side by the specified amounts.
     *
     * @param x The amount to contract on the X-axis.
     * @param y The amount to contract on the Y-axis.
     * @param z The amount to contract on the Z-axis.
     * @return The contracted {@link CuboidBlockRegion}.
     */
    default CuboidBlockRegion contract(int x, int y, int z) {
        return this.contract(x, y, z, CuboidBlockRegion::of);
    }

    /**
     * Create a new {@link CuboidBlockRegion} that is contracted on
     * each side by the specified amounts.
     *
     * @param x The amount to contract on the X-axis.
     * @param y The amount to contract on the Y-axis.
     * @param z The amount to contract on the Z-axis.
     * @param factory The {@link BlockRegionFunction} to use to
     *                create the new {@link CuboidBlockRegion}.
     * @return The contracted {@link CuboidBlockRegion}.
     */
    default CuboidBlockRegion contract(int x, int y, int z, BlockRegionFunction factory) {
        return factory.apply(this.min().add(x, y, z), this.max().subtract(x, y, z));
    }

    /**
     * Create a new {@link CuboidBlockRegion} that is shifted on
     * each side by the specified amounts.
     *
     * @param x The amount to shift on the X-axis.
     * @param y The amount to shift on the Y-axis.
     * @param z The amount to shift on the Z-axis.
     * @return The shifted {@link CuboidBlockRegion}.
     */
    default CuboidBlockRegion shift(int x, int y, int z) {
        return this.shift(x, y, z, CuboidBlockRegion::of);
    }

    /**
     * Create a new {@link CuboidBlockRegion} that is shifted on
     * each side by the specified amounts.
     *
     * @param x The amount to shift on the X-axis.
     * @param y The amount to shift on the Y-axis.
     * @param z The amount to shift on the Z-axis.
     * @param factory The {@link BlockRegionFunction} to use to
     *                create the new {@link CuboidBlockRegion}.
     * @return The shifted {@link CuboidBlockRegion}.
     */
    default CuboidBlockRegion shift(int x, int y, int z, BlockRegionFunction factory) {
        return factory.apply(this.min().add(x, y, z), this.max().add(x, y, z));
    }

    /**
     * Determine whether the specified {@link CuboidBlockRegion}
     * is equivalent to this.
     *
     * @param o The {@link CuboidBlockRegion} to compare to.
     * @return If the specified {@link CuboidBlockRegion} is equal.
     */
    default boolean equals(CuboidBlockRegion o) {
        return this.min().equals(o.min()) && this.max().equals(o.max());
    }

    /**
     * Create a new {@link CuboidBlockRegion} with the given coordinates.
     *
     * @param min The minimum block coordinate.
     * @param max The maximum block coordinate.
     * @return The newly created {@link CuboidBlockRegion}.
     */
    static CuboidBlockRegion of(BlockVector3 min, BlockVector3 max) {
        return new SimpleCuboidBlockRegion(min, max);
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
     * @return The newly created {@link CuboidBlockRegion}.
     */
    static CuboidBlockRegion of(int minX, int minY, int minZ,
                                int maxX, int maxY, int maxZ) {
        return new SimpleCuboidBlockRegion(minX, minY, minZ, maxX, maxY, maxZ);
    }

    interface BlockRegionFunction {

        /**
         * Create a new {@link CuboidBlockRegion}.
         *
         * @param min The minimum block coordinate.
         * @param max The maximum block coordinate.
         * @return The newly created {@link CuboidBlockRegion}.
         */
        CuboidBlockRegion apply(BlockVector3 min, BlockVector3 max);

        /**
         * Create a new {@link CuboidBlockRegion}.
         *
         * @param minX The minimum X block coordinate.
         * @param minY The minimum Y block coordinate.
         * @param minZ The minimum Z block coordinate.
         * @param maxX The maximum X block coordinate.
         * @param maxY The maximum Y block coordinate.
         * @param maxZ The maximum Z block coordinate.
         * @return The newly created {@link CuboidBlockRegion}.
         */
        default CuboidBlockRegion apply(int minX, int minY, int minZ,
                                        int maxX, int maxY, int maxZ) {
            return this.apply(BlockVector3.at(minX, minY, minZ), BlockVector3.at(maxX, maxY, maxZ));
        }
    }
}
