package com.projecki.fusion.util.region;

import com.projecki.fusion.util.BlockVector3;
import com.projecki.fusion.util.Vector3;

/**
 * A basic area that encapsulates a region of space.
 *
 * @since February 25, 2022
 * @author Andavin
 */
public interface CuboidRegion {

    /**
     * The minimum point of this region.
     *
     * @return The minimum point.
     */
    Vector3 min();

    /**
     * The maximum point of this region.
     *
     * @return The maximum point.
     */
    Vector3 max();

    /**
     * The minimum X coordinate of this region.
     *
     * @return The minimum X coordinate.
     */
    default double minX() {
        return this.min().x();
    }

    /**
     * The minimum Y coordinate of this region.
     *
     * @return The minimum Y coordinate.
     */
    default double minY() {
        return this.min().y();
    }

    /**
     * The minimum Z coordinate of this region.
     *
     * @return The minimum Z coordinate.
     */
    default double minZ() {
        return this.min().z();
    }

    /**
     * The maximum X coordinate of this region.
     *
     * @return The maximum X coordinate.
     */
    default double maxX() {
        return this.max().x();
    }

    /**
     * The maximum Y coordinate of this region.
     *
     * @return The maximum Y coordinate.
     */
    default double maxY() {
        return this.max().y();
    }

    /**
     * The maximum Z coordinate of this region.
     *
     * @return The maximum Z coordinate.
     */
    default double maxZ() {
        return this.max().z();
    }

    /**
     * Check whether the specified position is contained
     * with this {@link CuboidRegion}.
     *
     * @param pos The {@link Vector3 position}.
     * @return If the coordinates are contained with this {@link CuboidRegion}.
     */
    default boolean contains(Vector3 pos) {
        return this.contains(pos.x(), pos.y(), pos.z());
    }

    /**
     * Check whether the specified position is contained
     * with this {@link CuboidRegion}.
     *
     * @param pos The {@link BlockVector3 block position}.
     * @return If the coordinates are contained with this {@link CuboidRegion}.
     */
    default boolean contains(BlockVector3 pos) {
        return this.contains(pos.x(), pos.y(), pos.z());
    }

    /**
     * Check whether the specified coordinates are contained
     * with this {@link CuboidRegion}.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @param z The Z coordinate.
     * @return If the coordinates are contained with this {@link CuboidRegion}.
     */
    default boolean contains(double x, double y, double z) {
        return this.minX() <= x && x <= this.maxX() &&
                this.minY() <= y && y <= this.maxY() &&
                this.minZ() <= z && z <= this.maxZ();
    }

    /**
     * Check whether this {@link CuboidRegion} intersects the specified
     * {@link CuboidRegion} at any point.
     *
     * @param o The {@link CuboidRegion} to check intersection with.
     * @return If this intersects the given {@link CuboidRegion}.
     */
    default boolean intersects(CuboidRegion o) {
        return this.minX() < o.maxX() && this.maxX() > o.minX() &&
                this.minY() < o.maxY() && this.maxY() > o.minY() &&
                this.minZ() < o.maxZ() && this.maxZ() > o.minZ();
    }

    /**
     * Create a new {@link CuboidRegion} that represents the intersection
     * of this and the specified {@link CuboidRegion}.
     *
     * @param o The {@link CuboidRegion} to find the intersection with.
     * @return The intersection {@link CuboidRegion}.
     * @see CuboidRegion#of(double, double, double, double, double, double)
     */
    default CuboidRegion intersection(CuboidRegion o) {
        return this.intersection(o, CuboidRegion::of);
    }

    /**
     * Create a new {@link CuboidRegion} that represents the intersection
     * of this and the specified {@link CuboidRegion}.
     *
     * @param o The {@link CuboidRegion} to find the intersection with.
     * @param factory The {@link RegionFunction} to use to
     *                create the new {@link CuboidRegion}.
     * @return The intersection {@link CuboidRegion}.
     * @see RegionFunction
     */
    default CuboidRegion intersection(CuboidRegion o, RegionFunction factory) {
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
     * Create a new {@link CuboidRegion} that encompasses this and
     * the specified {@link CuboidRegion}.
     *
     * @param o The {@link CuboidRegion} to encompass.
     * @return The encompassing {@link CuboidRegion}.
     * @see CuboidRegion#of(double, double, double, double, double, double)
     */
    default CuboidRegion union(CuboidRegion o) {
        return this.union(o, CuboidRegion::of);
    }

    /**
     * Create a new {@link CuboidRegion} that encompasses this and
     * the specified {@link CuboidRegion}.
     *
     * @param o The {@link CuboidRegion} to encompass.
     * @param factory The {@link RegionFunction} to use to
     *                create the new {@link CuboidRegion}.
     * @return The encompassing {@link CuboidRegion}.
     * @see RegionFunction
     */
    default CuboidRegion union(CuboidRegion o, RegionFunction factory) {
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
     * Create a new {@link CuboidRegion} that is expanded on
     * all sides by the specified amount.
     *
     * @param amount The amount to expand on all sides.
     * @return The expanded {@link CuboidRegion}.
     */
    default CuboidRegion expand(double amount) {
        return this.expand(amount, amount, amount, CuboidRegion::of);
    }

    /**
     * Create a new {@link CuboidRegion} that is expanded on
     * all sides by the specified amount.
     *
     * @param amount The amount to expand on all sides.
     * @param factory The {@link RegionFunction} to use to
     *                create the new {@link CuboidRegion}.
     * @return The expanded {@link CuboidRegion}.
     */
    default CuboidRegion expand(double amount, RegionFunction factory) {
        return this.expand(amount, amount, amount, factory);
    }

    /**
     * Create a new {@link CuboidRegion} that is expanded on
     * each side by the specified amounts.
     *
     * @param x The amount to expand on the X-axis.
     * @param y The amount to expand on the Y-axis.
     * @param z The amount to expand on the Z-axis.
     * @return The expanded {@link CuboidRegion}.
     */
    default CuboidRegion expand(double x, double y, double z) {
        return this.expand(x, y, z, CuboidRegion::of);
    }

    /**
     * Create a new {@link CuboidRegion} that is expanded on
     * each side by the specified amounts.
     *
     * @param x The amount to expand on the X-axis.
     * @param y The amount to expand on the Y-axis.
     * @param z The amount to expand on the Z-axis.
     * @param factory The {@link RegionFunction} to use to
     *                create the new {@link CuboidRegion}.
     * @return The expanded {@link CuboidRegion}.
     */
    default CuboidRegion expand(double x, double y, double z, RegionFunction factory) {
        return factory.apply(this.min().subtract(x, y, z), this.max().add(x, y, z));
    }

    /**
     * Create a new {@link CuboidRegion} that is contracted on
     * all sides by the specified amount.
     *
     * @param amount The amount to contract on all sides.
     * @return The contracted {@link CuboidRegion}.
     */
    default CuboidRegion contract(double amount) {
        return this.contract(amount, amount, amount, CuboidRegion::of);
    }

    /**
     * Create a new {@link CuboidRegion} that is contracted on
     * all sides by the specified amount.
     *
     * @param amount The amount to contract on all sides.
     * @param factory The {@link RegionFunction} to use to
     *                create the new {@link CuboidRegion}.
     * @return The contracted {@link CuboidRegion}.
     */
    default CuboidRegion contract(double amount, RegionFunction factory) {
        return this.contract(amount, amount, amount, factory);
    }

    /**
     * Create a new {@link CuboidRegion} that is contracted on
     * each side by the specified amounts.
     *
     * @param x The amount to contract on the X-axis.
     * @param y The amount to contract on the Y-axis.
     * @param z The amount to contract on the Z-axis.
     * @return The contracted {@link CuboidRegion}.
     */
    default CuboidRegion contract(double x, double y, double z) {
        return this.contract(x, y, z, CuboidRegion::of);
    }

    /**
     * Create a new {@link CuboidRegion} that is contracted on
     * each side by the specified amounts.
     *
     * @param x The amount to contract on the X-axis.
     * @param y The amount to contract on the Y-axis.
     * @param z The amount to contract on the Z-axis.
     * @param factory The {@link RegionFunction} to use to
     *                create the new {@link CuboidRegion}.
     * @return The contracted {@link CuboidRegion}.
     */
    default CuboidRegion contract(double x, double y, double z, RegionFunction factory) {
        return factory.apply(this.min().add(x, y, z), this.max().subtract(x, y, z));
    }

    /**
     * Create a new {@link CuboidRegion} that is shifted on
     * each side by the specified amounts.
     *
     * @param x The amount to shift on the X-axis.
     * @param y The amount to shift on the Y-axis.
     * @param z The amount to shift on the Z-axis.
     * @return The shifted {@link CuboidRegion}.
     */
    default CuboidRegion shift(double x, double y, double z) {
        return this.shift(x, y, z, CuboidRegion::of);
    }

    /**
     * Create a new {@link CuboidRegion} that is shifted on
     * each side by the specified amounts.
     *
     * @param x The amount to shift on the X-axis.
     * @param y The amount to shift on the Y-axis.
     * @param z The amount to shift on the Z-axis.
     * @param factory The {@link RegionFunction} to use to
     *                create the new {@link CuboidRegion}.
     * @return The shifted {@link CuboidRegion}.
     */
    default CuboidRegion shift(double x, double y, double z, RegionFunction factory) {
        return factory.apply(this.min().add(x, y, z), this.max().add(x, y, z));
    }

    /**
     * Determine whether the specified {@link CuboidRegion}
     * is equivalent to this.
     *
     * @param o The {@link CuboidRegion} to compare to.
     * @return If the specified {@link CuboidRegion} is equal.
     */
    default boolean equals(CuboidRegion o) {
        return this.min().equals(o.min()) && this.max().equals(o.max());
    }

    /**
     * Create a new {@link CuboidRegion} with the given coordinates.
     *
     * @param min The minimum coordinate.
     * @param max The maximum coordinate.
     * @return The newly created {@link CuboidRegion}.
     */
    static CuboidRegion of(Vector3 min, Vector3 max) {
        return new SimpleCuboidRegion(min, max);
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
     * @return The newly created {@link CuboidRegion}.
     */
    static CuboidRegion of(double minX, double minY, double minZ,
                           double maxX, double maxY, double maxZ) {
        return new SimpleCuboidRegion(minX, minY, minZ, maxX, maxY, maxZ);
    }

    interface RegionFunction {

        /**
         * Create a new {@link CuboidRegion}.
         *
         * @param min The minimum coordinate.
         * @param max The maximum coordinate.
         * @return The newly created {@link CuboidRegion}.
         */
        CuboidRegion apply(Vector3 min, Vector3 max);

        /**
         * Create a new {@link CuboidRegion}.
         *
         * @param minX The minimum X coordinate.
         * @param minY The minimum Y coordinate.
         * @param minZ The minimum Z coordinate.
         * @param maxX The maximum X coordinate.
         * @param maxY The maximum Y coordinate.
         * @param maxZ The maximum Z coordinate.
         * @return The newly created {@link CuboidRegion}.
         */
        default CuboidRegion apply(double minX, double minY, double minZ,
                                   double maxX, double maxY, double maxZ) {
            return this.apply(Vector3.at(minX, minY, minZ), Vector3.at(maxX, maxY, maxZ));
        }
    }
}
