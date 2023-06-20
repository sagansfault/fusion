package com.projecki.fusion.button;

import com.projecki.fusion.customfloatingtexture.AbstractCustomFloatingTexture;
import com.projecki.fusion.util.ImmutableLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents the super type of a button in a world. Simply common math and logic for buttons.
 */
public abstract class AbstractButton implements Listener {

    private static final Vector Y = new Vector(0, 1, 0);
    private static final Vector X = new Vector(1, 0, 0);

    protected final JavaPlugin plugin;

    protected final AbstractCustomFloatingTexture floatingTexture;
    protected final float textureWidth;
    protected final float textureHeight;
    protected final double range;

    protected final ImmutableLocation max;
    protected final ImmutableLocation min;
    protected final Vector maxVec;
    protected final Vector minVec;

    protected Consumer<Player> onStartHover = p -> {};
    protected Consumer<Player> onEndHover = p -> {};
    protected BiConsumer<Player, Action> onClick = (p, a) -> {};

    /**
     * Constructs a new button
     *
     * @param plugin The main plugin
     * @param floatingTexture The texture to display as this button
     * @param textureWidth The horizontal bounds of this button; used for determining successful clicks/hovers of the button
     * @param textureHeight The vertical bounds of this button; used for determining successful clicks/hovers of the button
     * @param range The range at which a button should be allowed to be interacted with
     */
    public AbstractButton(JavaPlugin plugin,
                          AbstractCustomFloatingTexture floatingTexture,
                          float textureWidth,
                          float textureHeight,
                          double range) {
        this.plugin = plugin;
        this.floatingTexture = floatingTexture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.range = range;

        Location origin = floatingTexture.getOrigin();
        // determine corner locations
        float halfWidth = textureWidth / 2f;
        int rotation = floatingTexture.getRotation();
        Location loc1 = getSymmetricRotatedLocation(origin, halfWidth, rotation).add(0, 0.8, 0);
        Location loc2 = getSymmetricRotatedLocation(origin, -halfWidth, rotation).add(0, 0.8, 0).add(0, textureHeight, 0);

        // determine vectors
        double maxX, maxY, maxZ, minX, minY, minZ;
        maxX = Math.max(loc1.getX(), loc2.getX());
        maxY = Math.max(loc1.getY(), loc2.getY());
        maxZ = Math.max(loc1.getZ(), loc2.getZ());
        minX = Math.min(loc1.getX(), loc2.getX());
        minY = Math.min(loc1.getY(), loc2.getY());
        minZ = Math.min(loc1.getZ(), loc2.getZ());

        Location maxLoc = new Location(loc1.getWorld(), maxX, maxY, maxZ);
        Location minLoc = new Location(loc2.getWorld(), minX, minY, minZ);
        this.max = ImmutableLocation.from(maxLoc);
        this.min = ImmutableLocation.from(minLoc);
        this.maxVec = max.toVector();
        this.minVec = min.toVector();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public AbstractCustomFloatingTexture getFloatingTexture() {
        return floatingTexture;
    }

    private Location getSymmetricRotatedLocation(Location origin, float offset, int angle) {
        return this.angleToVec(angle).normalize().multiply(offset).add(origin.toVector()).toLocation(origin.getWorld());
    }

    /**
     * Determines whether a given player is looking at this button within its range and bounds
     *
     * @param player The player
     * @return Whether they are looking
     */
    public boolean isLooking(Player player) {
        return this.isLooking(player.getEyeLocation());
    }

    /**
     * Determines whether a given eye location is looking at the button. This is the getEyeLocation() function of
     * many entities. This is the actual location of the entity's eyes, or more specifically, the camera of the client.
     *
     * @param entityEyeLocation The entity eye location
     * @return Whether they are looking
     */
    public boolean isLooking(Location entityEyeLocation) {

        // out of range
        if (entityEyeLocation.distanceSquared(this.min) > range * range) {
            return false;
        }

        Vector eyeLocVec = entityEyeLocation.toVector();
        Vector lookingDir = entityEyeLocation.getDirection();

        System.out.println(eyeLocVec);
        System.out.println(lookingDir);

        Vector playerEyeToMax = this.maxVec.clone().subtract(eyeLocVec);
        Vector playerEyeToMin = this.minVec.clone().subtract(eyeLocVec);

        float maxToVert = Math.abs(playerEyeToMax.angle(Y));
        float minToVert = Math.abs(playerEyeToMin.angle(Y));
        float lookingToVert = Math.abs(lookingDir.angle(Y));

        float maxToSide = Math.abs(playerEyeToMax.angle(X));
        float minToSide = Math.abs(playerEyeToMin.angle(X));
        float lookingToSide = Math.abs(lookingDir.angle(X));

        return between(maxToVert, minToVert, lookingToVert) && between(maxToSide, minToSide, lookingToSide);
    }

    /**
     * Sets the function that should run when a user first begins to hover over this button
     *
     * @param onStartHover The function to run
     */
    public void onStartHover(Consumer<Player> onStartHover) {
        this.onStartHover = onStartHover;
    }

    /**
     * Sets the function that should run when a user ends their hovering over a button. For this to fire, the user must
     * have already been hovering over the button
     *
     * @param onEndHover The function to run
     */
    public void onEndHover(Consumer<Player> onEndHover) {
        this.onEndHover = onEndHover;
    }

    /**
     * Sets the function that should run when a user interacts while hovering over button.
     *
     * @param onClick The function to run
     */
    public void onClick(BiConsumer<Player, Action> onClick) {
        this.onClick = onClick;
    }

    /**
     * Removes this button
     */
    public void delete() {
        this.floatingTexture.delete();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    private void onLookEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location eyeLocation = player.getEyeLocation();
        Location fromRaw = event.getFrom();
        Location toRaw = event.getTo();

        /*
        The direction of a location is gained from its pitch and yaw so that's the only part we really care about.
        The x, y, z components can be the same since this event fires so often, so we only use the pitch and yaw
        from the previous and next/current location.
         */

        Location from = new Location(eyeLocation.getWorld(), eyeLocation.getX(), eyeLocation.getY(),
                eyeLocation.getZ(), fromRaw.getYaw(), fromRaw.getPitch());
        Location to = new Location(eyeLocation.getWorld(), eyeLocation.getX(), eyeLocation.getY(),
                eyeLocation.getZ(), toRaw.getYaw(), toRaw.getPitch());

        if (this.isLooking(from) && !this.isLooking(to)) {
            this.onEndHover.accept(player);
        } else if (!this.isLooking(from) && this.isLooking(to)) {
            this.onStartHover.accept(player);
        }
    }

    @EventHandler
    private void onClickEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (this.isLooking(player)) {
            this.onClick.accept(player, event.getAction());
        }
    }

    private Vector angleToVec(int angle) {
        int clamped = angle >= 360 ? angle - 360 : Math.max(0, angle);
        double x = Math.cos(clamped); // * 1
        double z = Math.sin(clamped); // * 1
        return new Vector(x, 0, z);
    }

    private boolean between(float bound1, float bound2, float check) {
        float max = Math.max(bound1, bound2);
        float min = Math.min(bound1, bound2);
        return max > check && check > min;
    }
}
