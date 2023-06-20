package com.projecki.fusion.util;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.unmodifiableMap;

/**
 * A utility class for converting {@link BlockFace BlockFaces}
 * to and from degrees.
 */
public final class DirectionUtil {

    private static final Map<BlockFace, Float> ROTATIONS;
    private static final List<BlockFace> LEFT_ROTATION, RIGHT_ROTATION;

    /**
     * A list containing the cardinal {@link BlockFace directions}:
     * <ul>
     *     <li>{@link BlockFace#SOUTH}</li>
     *     <li>{@link BlockFace#WEST}</li>
     *     <li>{@link BlockFace#NORTH}</li>
     *     <li>{@link BlockFace#EAST}</li>
     * </ul>
     * Note that the order of the above list is the order in which the
     * directions appear in the list when iterating forward.
     */
    public static final List<BlockFace> CARDINAL_DIRECTIONS = List.of(
            BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST
    );

    /**
     * A list containing the cardinal directions and their in-between,
     * diagonal directions:
     * <ul>
     *     <li>{@link BlockFace#SOUTH}</li>
     *     <li>{@link BlockFace#SOUTH_WEST}</li>
     *     <li>{@link BlockFace#WEST}</li>
     *     <li>{@link BlockFace#NORTH_WEST}</li>
     *     <li>{@link BlockFace#NORTH}</li>
     *     <li>{@link BlockFace#NORTH_EAST}</li>
     *     <li>{@link BlockFace#EAST}</li>
     *     <li>{@link BlockFace#SOUTH_EAST}</li>
     * </ul>
     * Note that the order of the above list is the order in which the
     * directions appear in the list when iterating forward.
     */
    public static final List<BlockFace> DIAGONAL_DIRECTIONS = List.of(
            BlockFace.SOUTH, BlockFace.SOUTH_WEST,
            BlockFace.WEST, BlockFace.NORTH_WEST,
            BlockFace.NORTH, BlockFace.NORTH_EAST,
            BlockFace.EAST, BlockFace.SOUTH_EAST
    );

    static {

        BlockFace[] values = BlockFace.values();
        BlockFace[] left = new BlockFace[values.length];
        left[BlockFace.SELF.ordinal()] = BlockFace.SELF;
        left[BlockFace.UP.ordinal()] = BlockFace.UP;
        left[BlockFace.DOWN.ordinal()] = BlockFace.DOWN;
        left[BlockFace.NORTH.ordinal()] = BlockFace.NORTH_NORTH_WEST;
        left[BlockFace.NORTH_NORTH_WEST.ordinal()] = BlockFace.NORTH_WEST;
        left[BlockFace.NORTH_WEST.ordinal()] = BlockFace.WEST_NORTH_WEST;
        left[BlockFace.WEST_NORTH_WEST.ordinal()] = BlockFace.WEST;
        left[BlockFace.WEST.ordinal()] = BlockFace.WEST_SOUTH_WEST;
        left[BlockFace.WEST_SOUTH_WEST.ordinal()] = BlockFace.SOUTH_WEST;
        left[BlockFace.SOUTH_WEST.ordinal()] = BlockFace.SOUTH_SOUTH_WEST;
        left[BlockFace.SOUTH_SOUTH_WEST.ordinal()] = BlockFace.SOUTH;
        left[BlockFace.SOUTH.ordinal()] = BlockFace.SOUTH_SOUTH_EAST;
        left[BlockFace.SOUTH_SOUTH_EAST.ordinal()] = BlockFace.SOUTH_EAST;
        left[BlockFace.SOUTH_EAST.ordinal()] = BlockFace.EAST_SOUTH_EAST;
        left[BlockFace.EAST_SOUTH_EAST.ordinal()] = BlockFace.EAST;
        left[BlockFace.EAST.ordinal()] = BlockFace.EAST_NORTH_EAST;
        left[BlockFace.EAST_NORTH_EAST.ordinal()] = BlockFace.NORTH_EAST;
        left[BlockFace.NORTH_EAST.ordinal()] = BlockFace.NORTH_NORTH_EAST;
        left[BlockFace.NORTH_NORTH_EAST.ordinal()] = BlockFace.NORTH;

        BlockFace[] right = new BlockFace[values.length];
        right[BlockFace.SELF.ordinal()] = BlockFace.SELF;
        right[BlockFace.UP.ordinal()] = BlockFace.UP;
        right[BlockFace.DOWN.ordinal()] = BlockFace.DOWN;
        right[BlockFace.NORTH.ordinal()] = BlockFace.NORTH_NORTH_EAST;
        right[BlockFace.NORTH_NORTH_EAST.ordinal()] = BlockFace.NORTH_EAST;
        right[BlockFace.NORTH_EAST.ordinal()] = BlockFace.EAST_NORTH_EAST;
        right[BlockFace.EAST_NORTH_EAST.ordinal()] = BlockFace.EAST;
        right[BlockFace.EAST.ordinal()] = BlockFace.EAST_SOUTH_EAST;
        right[BlockFace.EAST_SOUTH_EAST.ordinal()] = BlockFace.SOUTH_EAST;
        right[BlockFace.SOUTH_EAST.ordinal()] = BlockFace.SOUTH_SOUTH_EAST;
        right[BlockFace.SOUTH_SOUTH_EAST.ordinal()] = BlockFace.SOUTH;
        right[BlockFace.SOUTH.ordinal()] = BlockFace.SOUTH_SOUTH_WEST;
        right[BlockFace.SOUTH_SOUTH_WEST.ordinal()] = BlockFace.SOUTH_WEST;
        right[BlockFace.SOUTH_WEST.ordinal()] = BlockFace.WEST_SOUTH_WEST;
        right[BlockFace.WEST_SOUTH_WEST.ordinal()] = BlockFace.WEST;
        right[BlockFace.WEST.ordinal()] = BlockFace.WEST_NORTH_WEST;
        right[BlockFace.WEST_NORTH_WEST.ordinal()] = BlockFace.NORTH_WEST;
        right[BlockFace.NORTH_WEST.ordinal()] = BlockFace.NORTH_NORTH_WEST;
        right[BlockFace.NORTH_NORTH_WEST.ordinal()] = BlockFace.NORTH;

        LEFT_ROTATION = List.of(left);
        RIGHT_ROTATION = List.of(right);

        Map<BlockFace, Float> rotations = new EnumMap<>(BlockFace.class);
        rotations.put(BlockFace.SOUTH, 0F);
        rotations.put(BlockFace.SOUTH_SOUTH_WEST, 22.5F);
        rotations.put(BlockFace.SOUTH_WEST, 45F);
        rotations.put(BlockFace.WEST_SOUTH_WEST, 67.5F);
        rotations.put(BlockFace.WEST, 90F);
        rotations.put(BlockFace.WEST_NORTH_WEST, 112.5F);
        rotations.put(BlockFace.NORTH_WEST, 135F);
        rotations.put(BlockFace.NORTH_NORTH_WEST, 157.5F);
        rotations.put(BlockFace.NORTH, 180F);
        rotations.put(BlockFace.NORTH_NORTH_EAST, 202.5F);
        rotations.put(BlockFace.NORTH_EAST, 225F);
        rotations.put(BlockFace.EAST_NORTH_EAST, 247.5F);
        rotations.put(BlockFace.EAST, 270F);
        rotations.put(BlockFace.EAST_SOUTH_EAST, 292.5F);
        rotations.put(BlockFace.SOUTH_EAST, 315F);
        rotations.put(BlockFace.SOUTH_SOUTH_EAST, 337.5F);
        ROTATIONS = unmodifiableMap(rotations);
    }

    /**
     * Get the yaw for a location so that an entity at the location would be
     * facing the same direction as the blockface
     *
     * @param blockFace block face to get yaw of
     * @return yaw that will make an entity face the same as the block face
     * @throws IllegalArgumentException for blockfaces that can't be converted to yaws
     * @deprecated Use {@link #getRotation(BlockFace)}
     */
    @Deprecated
    public static float getYaw(BlockFace blockFace) {
        return getRotation(blockFace);
    }

    /**
     * Get the {@link BlockFace direction} that the location is facing.
     * This will include the main 4 directions and their diagonal
     * counterparts as well:
     * <ul>
     *     <li>{@link BlockFace#NORTH}</li>
     *     <li>{@link BlockFace#NORTH_EAST}</li>
     *     <li>{@link BlockFace#EAST}</li>
     *     <li>{@link BlockFace#SOUTH_EAST}</li>
     *     <li>{@link BlockFace#SOUTH}</li>
     *     <li>{@link BlockFace#SOUTH_WEST}</li>
     *     <li>{@link BlockFace#WEST}</li>
     *     <li>{@link BlockFace#NORTH_WEST}</li>
     * </ul>
     * It does not include {@link BlockFace#UP} or {@link BlockFace#DOWN}
     * or any diagonal direction.
     * <p>
     * This is equivalent to:
     * <pre>
     *     LocationUtil.getDirection(loc, true, false);</pre>
     *
     * @param loc The location to get the direction from.
     * @return The direction that the location is facing.
     * @see #getDirection(Location, boolean, boolean)
     */
    public static BlockFace getDirection(Location loc) {
        return getDirection(loc, true, false);
    }

    /**
     * Get the cardinal {@link BlockFace direction} that the location
     * is facing. Cardinal direction includes the 4 normal directions:
     * <ul>
     *     <li>{@link BlockFace#NORTH}</li>
     *     <li>{@link BlockFace#SOUTH}</li>
     *     <li>{@link BlockFace#EAST}</li>
     *     <li>{@link BlockFace#WEST}</li>
     * </ul>
     * It does not include {@link BlockFace#UP} or {@link BlockFace#DOWN}
     * or any diagonal direction.
     * <p>
     * This is equivalent to:
     * <pre>
     *     LocationUtil.getDirection(loc, false, false);</pre>
     *
     * @param loc The location to get the direction from.
     * @return The direction that the location is facing.
     * @see #getDirection(Location, boolean, boolean)
     */
    public static BlockFace getCardinalDirection(Location loc) {
        return getDirection(loc, false, false);
    }

    /**
     * Get the {@link BlockFace direction} that the location is facing
     * including vertical and cardinal directions directions:
     * <ul>
     *     <li>{@link BlockFace#NORTH}</li>
     *     <li>{@link BlockFace#SOUTH}</li>
     *     <li>{@link BlockFace#EAST}</li>
     *     <li>{@link BlockFace#WEST}</li>
     *     <li>{@link BlockFace#UP}</li>
     *     <li>{@link BlockFace#DOWN}</li>
     * </ul>
     * This is equivalent to:
     * <pre>
     *     LocationUtil.getDirection(loc, false, true);</pre>
     *
     * @param loc The location to get the direction from.
     * @return The direction that the location is facing.
     * @see #getDirection(Location, boolean, boolean)
     */
    public static BlockFace getVerticalDirection(Location loc) {
        return getDirection(loc, false, true);
    }

    /**
     * Get the {@link BlockFace direction} that the location is facing.
     * Optionally include directions such as {@link BlockFace#NORTH_EAST}
     * or {@link BlockFace#SOUTH_WEST} as well as {@link BlockFace#UP} or
     * {@link BlockFace#DOWN}.
     *
     * @param loc The location to get the direction off of.
     * @param diagonal If directions such as {@link BlockFace#NORTH_EAST} or
     *                 {@link BlockFace#NORTH_WEST} etc. should be included.
     * @param vertical If the {@link BlockFace#UP} and {@link BlockFace#DOWN}
     *                 directions should be included in the calculation.
     * @return The direction that the location is facing.
     */
    public static BlockFace getDirection(Location loc, boolean diagonal, boolean vertical) {

        if (vertical) {

            float pitch = loc.getPitch();
            if (pitch > 70) {
                return BlockFace.UP;
            }

            if (pitch < -70) {
                return BlockFace.DOWN;
            }
        }

        List<BlockFace> directions = diagonal ? DIAGONAL_DIRECTIONS : CARDINAL_DIRECTIONS;
        //noinspection IntegerDivisionInFloatingPointContext
        return directions.get(Math.round(loc.getYaw() / (360 / directions.size())) & directions.size() - 1);
    }

    /**
     * Get the rotation (i.e. yaw) of the given {@link BlockFace}.
     *
     * @param face The {@link BlockFace} to get the rotation for.
     * @return The rotation for the {@link BlockFace}.
     * @throws NullPointerException If the {@link BlockFace} cannot
     *                              be converted to a rotation.
     */
    public static float getRotation(BlockFace face) {
        return ROTATIONS.get(face);
    }

    /**
     * Get the difference, in degrees, between the first {@link BlockFace}
     * given and the second.
     *
     * @param from The BlockFace to measure from (0º).
     * @param to The BlockFace to measure to (degrees from 0º).
     * @return The amount of degrees that separate the two BlockFaces.
     */
    public static float getDifference(BlockFace from, BlockFace to) {

        if (from == to) {
            return 0F;
        }

        if (from.getOppositeFace() == to) {
            return 180F;
        }

        float degrees = 22.5F;
        while ((from = rotateRight(from)) != to) {
            degrees += 22.5F;
        }

        return degrees;
    }

    /**
     * Rotate the given {@link BlockFace} by the given degrees to the
     * closest 16th of a circle on the y-axis.
     * <p>
     * If the degrees are positive, the face will be rotated clockwise
     * and if they are negative it will be rotated counterclockwise.
     *
     * @param face The BlockFace to rotate.
     * @param degrees The amount of degrees to rotate.
     * @return The block face that is rotated.
     */
    public static BlockFace rotate(BlockFace face, float degrees) {
        return rotate(face, degrees, false, false);
    }

    /**
     * Rotate the given {@link BlockFace} by the given degrees to the
     * closest 16th of a circle on the y-axis and to the closest 4th
     * of a circle on the x or z axes.
     * <p>
     * If the degrees are positive, the face will be rotated clockwise
     * and if they are negative it will be rotated counterclockwise.
     * <p>
     * Note that the rotation cannot take place on both X and Z axes.
     * Therefore, both {@code xAxis} and {@code zAxis} cannot both be
     * {@code true}. If they are, then it will be rotated on the X-axis.
     *
     * @param face The BlockFace to rotate.
     * @param degrees The amount of degrees to rotate.
     * @param xAxis If the rotation is taking place on the x-axis.
     * @param zAxis If the rotation is taking place on the z-axis.
     * @return The block face that is rotated.
     */
    public static BlockFace rotate(BlockFace face, float degrees, boolean xAxis, boolean zAxis) {

        if (face == BlockFace.SELF || degrees == 0) {
            return face;
        }

        float dRotations = degrees / 22.5F;
        int rotations = (int) dRotations;
        if (dRotations > rotations) { // If the degrees were positive
            rotations++;
        } else if (dRotations < rotations) { // If the degrees were negative
            rotations--;
        }

        rotations %= 16;
        if (xAxis || zAxis) {

            rotations /= 4;
            if (rotations == 2) {
                return face.getOppositeFace();
            }

            if (rotations == 3 || rotations == -3) {
                rotations /= -3;
            }

            boolean positive = rotations == 1;
            switch (face) {
                case UP:
                    return xAxis ? positive ? BlockFace.NORTH : BlockFace.SOUTH :
                            positive ? BlockFace.EAST : BlockFace.WEST;
                case NORTH:
                case EAST:
                    return positive ? BlockFace.DOWN : BlockFace.UP;
                case DOWN:
                    return xAxis ? positive ? BlockFace.SOUTH : BlockFace.NORTH :
                            positive ? BlockFace.WEST : BlockFace.EAST;
                case SOUTH:
                case WEST:
                    return positive ? BlockFace.UP : BlockFace.DOWN;
            }
        }

        if (degrees > 0) {
            return rotateRight(face, rotations);
        }

        if (degrees < 0) {
            return rotateLeft(face, rotations * -1);
        }

        return face;
    }

    /**
     * Rotate the given {@link BlockFace} 1/16th of a 360º rotation
     * clockwise on the y-axis. If the given BlockFace is either
     * {@link BlockFace#UP} or {@link BlockFace#DOWN}, then itself
     * will be returned as those faces cannot be rotated on the y-axis.
     *
     * @param face The face to rotate on the y-axis.
     * @return The BlockFace that has been rotated 22.5º on the y-axis.
     */
    public static BlockFace rotateRight(BlockFace face) {
        return RIGHT_ROTATION.get(face.ordinal());
    }

    /**
     * Rotate the given {@link BlockFace} 1/16th of a 360º rotation
     * clockwise on the y-axis. If the given BlockFace is either
     * {@link BlockFace#UP} or {@link BlockFace#DOWN}, then itself
     * will be returned as those faces cannot be rotated on the y-axis.
     *
     * @param face The face to rotate on the y-axis.
     * @param amount The amount of 22.5º rotations to rotate the face.
     *               So {@code 4} would result in a 90º rotation.
     * @return The BlockFace that has been rotated 22.5º on the y-axis.
     */
    public static BlockFace rotateRight(BlockFace face, int amount) {
        return rotate(RIGHT_ROTATION, face, amount);
    }

    /**
     * Rotate the given {@link BlockFace} 1/16th of a 360º rotation
     * counterclockwise on the y-axis. If the given BlockFace is either
     * {@link BlockFace#UP} or {@link BlockFace#DOWN}, then itself will
     * be returned as those faces cannot be rotated on the y-axis.
     *
     * @param face The face to rotate on the y-axis.
     * @return The BlockFace that has been rotated 22.5º on the y-axis.
     */
    public static BlockFace rotateLeft(BlockFace face) {
        return LEFT_ROTATION.get(face.ordinal());
    }

    /**
     * Rotate the given {@link BlockFace} 1/16th of a 360º rotation
     * counterclockwise on the y-axis. If the given BlockFace is either
     * {@link BlockFace#UP} or {@link BlockFace#DOWN}, then itself will
     * be returned as those faces cannot be rotated on the y-axis.
     *
     * @param face The face to rotate on the y-axis.
     * @param amount The amount of 22.5º rotations to rotate the face.
     *               So {@code 4} would result in a 90º rotation.
     * @return The BlockFace that has been rotated 22.5º on the y-axis.
     */
    public static BlockFace rotateLeft(BlockFace face, int amount) {
        return rotate(LEFT_ROTATION, face, amount);
    }

    private static BlockFace rotate(List<BlockFace> rotations, BlockFace face, int amount) {

        amount %= 16; // Anything over 16 is over a full rotation
        if (amount == 0 || face == BlockFace.SELF || face == BlockFace.UP || face == BlockFace.DOWN) { // No rotation
            return face;
        }

        if (amount == 1) { // Single rotation
            return rotations.get(face.ordinal());
        }

        if (amount == 8) { // Faster than iterating
            return face.getOppositeFace();
        }

        checkArgument(amount > 1, "cannot have negative rotation: %s", amount);
        BlockFace rotated = face;
        for (int i = 0; i < amount; i++) {
            rotated = rotations.get(rotated.ordinal());
        }

        return rotated;
    }

}