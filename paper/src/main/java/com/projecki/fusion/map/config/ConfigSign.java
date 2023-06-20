package com.projecki.fusion.map.config;

import com.projecki.fusion.util.DirectionUtil;
import com.projecki.fusion.util.ImmutableLocation;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;

import java.util.Arrays;

/**
 * Data that was extracted from a physical sign in the world
 * that is used for configuring.
 *
 * Constructor is private, generate a new {@link ConfigSign} from
 * a block that is a sign using the {@code fromBlock} method.
 */
public final class ConfigSign {

    /** The contents of the sign. The brackets on the first line will still be present. */
    private final String[] contents;

    /** The direction the sign was facing */
    private final BlockFace facingDirection;

    /** If the sign was a sign post, as opposed to a wall-sign */
    private final boolean post;

    /** The location the sign was in */
    private final ImmutableLocation location;

    private ConfigSign(String[] contents, BlockFace facingDirection, boolean post, ImmutableLocation location) {
        this.contents = contents;
        this.facingDirection = facingDirection;
        this.post = post;
        this.location = location;
    }

    static ConfigSign fromBlock(Block block) {
        if (block == null || !isSign(block))
            throw new IllegalArgumentException("Passed block is null or not a sign.");

        // get the direction the sign is facing
        var data = block.getBlockData();
        BlockFace facing = null;

        if (data instanceof org.bukkit.block.data.type.Sign signData) {
            facing = signData.getRotation();
        } else if (data instanceof WallSign signData) {
            facing = signData.getFacing();
        }

        Validate.notNull(facing, "Unable to get sign direction from sign");

        // set the location yaw from the sign direction
        var location = block.getLocation();
        location.setYaw(DirectionUtil.getRotation(facing));

        return new ConfigSign(((Sign) block.getState()).getLines(), facing,
                !block.getType().name().contains("WALL"), ImmutableLocation.from(location));
    }

    private static boolean isSign(Block block) {
        return block.getState() instanceof Sign;
    }

    /**
     * Get the contents of the sign.
     * The brackets on the first line will still be present.
     */
    public String[] getContents() {
        return contents;
    }

    /**
     * Get the direction the sign was facing.
     */
    public BlockFace getFacingDirection() {
        return facingDirection;
    }

    /**
     * Get if the sign was a sign post, as opposed to a wall sign.
     */
    public boolean isPost() {
        return post;
    }

    /**
     * Get the location the sign was in.
     * Returned location is immutable.
     */
    public ImmutableLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "ConfigSign[" +
                "contents=" + Arrays.toString(contents) + ", " +
                "facingDirection=" + facingDirection + ", " +
                "post=" + post + ", " +
                "location=" + location + ']';
    }

}
