package com.projecki.fusion.util.math;

import com.projecki.fusion.util.Vector3;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * An object that assists in forward kinematic calculations.
 * <p>
 *     This can be used for a wide variety of use-cases such
 *     as creating different moving parts of a single object.
 *     <br>
 *     Each part being relative to the others or all based around
 *     a single base transform that is moved in space.
 * </p>
 *
 * @since May 27, 2022
 * @author Andavin
 * @see <a href="https://www.rosroboticslearning.com/forward-kinematics">Forward Kinematics</a>
 */
public class Transform {

    /**
     * Create a new transform.
     *
     * @return The created transform.
     */
    public static Transform of() {
        return new Transform(Vector3.ZERO, Vector3.ZERO, Vector3.ONE);
    }

    /**
     * Create a new transform.
     *
     * @param pos The {@link Vector3 position}.
     * @return The created transform.
     */
    public static Transform of(Vector3 pos) {
        return new Transform(pos, Vector3.ZERO, Vector3.ONE);
    }

    /**
     * Create a new transform.
     *
     * @param pos The {@link Vector3 position}.
     * @param rot The {@link Vector3 rotation} in the form of
     *            {@code (yaw, pitch roll)}. This is <b>not</b>
     *            a direction vector.
     * @return The created transform.
     */
    public static Transform of(Vector3 pos, Vector3 rot) {
        return new Transform(pos, rot, Vector3.ONE);
    }

    /**
     * Create a new transform.
     *
     * @param pos The {@link Vector3 position}.
     * @param rot The {@link Vector3 rotation} in the form of
     *            {@code (yaw, pitch roll)}. This is <b>not</b>
     *            a direction vector.
     * @param scale The {@link Vector3 scale}.
     * @return The created transform.
     */
    public static Transform of(Vector3 pos, Vector3 rot, Vector3 scale) {
        return new Transform(pos, rot, scale);
    }

    /**
     * Create a new transform.
     *
     * @param posX The X coordinate of the position.
     * @param posY The Y coordinate of the position.
     * @param posZ The Z coordinate of the position.
     * @return The created transform.
     */
    public static Transform of(double posX, double posY, double posZ) {
        return of(
                Vector3.at(posX, posY, posZ),
                Vector3.ZERO,
                Vector3.ONE
        );
    }

    /**
     * Create a new transform.
     *
     * @param posX The X coordinate of the position.
     * @param posY The Y coordinate of the position.
     * @param posZ The Z coordinate of the position.
     * @param yaw The yaw angle of the rotation.
     * @param pitch The pitch angle of the rotation.
     * @param roll The roll angle of the rotation.
     * @return The created transform.
     */
    public static Transform of(double posX, double posY, double posZ,
                               double yaw, double pitch, double roll) {
        return of(
                Vector3.at(posX, posY, posZ),
                Vector3.at(yaw, pitch, roll),
                Vector3.ONE
        );
    }

    /**
     * Create a new transform and copy all the values
     * from the specified transform.
     *
     * @param transform The transform to copy from.
     */
    public static Transform copyOf(Transform transform) {
        return of(transform.pos, transform.rot, transform.scale);
    }

    private boolean updating;
    private Vector3 pos, rot, scale;
    private final Matrix4 local = Matrix4.of();
    private final Matrix4 global = Matrix4.of();

    private Consumer<Transform> updates = __ -> {};
    private final List<Transform> next = new CopyOnWriteArrayList<>(); // The next links or "children"
    private final AtomicReference<Transform> previous = new AtomicReference<>(); // The previous link or "parent"

    private Transform(Vector3 pos, Vector3 rot, Vector3 scale) {
        this.pos = pos;
        this.rot = rot;
        this.scale = scale;
        this.updateLocal();
    }

    /**
     * The X position of this transform.
     *
     * @return The X position.
     */
    public double x() {
        return global.m30();
    }

    /**
     * The Y position of this transform.
     *
     * @return The Y position.
     */
    public double y() {
        return global.m31();
    }

    /**
     * The Z position of this transform.
     *
     * @return The Z position.
     */
    public double z() {
        return global.m32();
    }

    /**
     * The {@link Vector3 position} of this transform.
     *
     * @return The {@link Vector3} position.
     */
    public Vector3 position() {
        return global.translation();
    }

    /**
     * The direction {@link Vector3 vector} facing to the right
     * of this transform.
     *
     * @return The right direction {@link Vector3 vector}.
     */
    public Vector3 right() {
        return global.calcRight();
    }

    /**
     * The direction {@link Vector3 vector} facing up
     * from this transform.
     *
     * @return The up direction {@link Vector3 vector}.
     */
    public Vector3 up() {
        return global.calcUp();
    }

    /**
     * The forward direction {@link Vector3 vector}
     * of this transform.
     *
     * @return The forward direction {@link Vector3 vector}.
     */
    public Vector3 forward() {
        return global.calcForward();
    }

    /**
     * The local {@link Vector3 position} of this transform
     * without taking into account any global calculations
     * that would normally affect the {@link #position()}.
     *
     * @return The local {@link Vector3 position}.
     */
    public Vector3 localPosition() {
        return pos;
    }

    /**
     * The local {@link Vector3 rotation} of this transform
     * without taking into account any global calculations
     * that would normally affect the rotation.
     *
     * @return The local {@link Vector3 rotation}.
     */
    public Vector3 localRotation() {
        return rot;
    }

    /**
     * The local {@link Vector3 scale} of this transform
     * without taking into account any global calculations
     * that would normally affect the scale.
     *
     * @return The local {@link Vector3 scale}.
     */
    public Vector3 localScale() {
        return scale;
    }

    /**
     * Set the local {@link Vector3 position} of this
     * transform and update it.
     *
     * @param pos The {@link Vector3 position} to set to.
     * @return This transform after it has been updated.
     */
    public Transform position(Vector3 pos) {
        this.pos = pos;
        return this.updateLocal();
    }

    /**
     * Set the local {@link Vector3 rotation} of this
     * transform and update it.
     * <p>
     *     A rotation vector is in the form of {@code (yaw, pitch roll)}
     *     and is not a direction vector.
     *     <br>
     *     To set a direction {@link Vector3} use {@link #direction(Vector3)}.
     * </p>
     *
     * @param rot The {@link Vector3 rotation} to set to.
     * @return This transform after it has been updated.
     */
    public Transform rotation(Vector3 rot) {
        this.rot = rot;
        return this.updateLocal();
    }

    /**
     * Set the local {@link Vector3 direction} of this
     * transform and update it.
     * <p>
     *     Unlike {@link #rotation(Vector3)}, this method takes
     *     a direction vector in the standard {@code (x, y, z)} format.
     * </p>
     *
     * @param dir The direction {@link Vector3} to set to.
     * @return This transform after it has been updated.
     */
    public Transform direction(Vector3 dir) {
        return this.rotation(Vector3.at(dir.toYaw(), dir.toPitch(), 0));
    }

    /**
     * Set the local {@link Vector3 scale} of this
     * transform and update it.
     *
     * @param scale The {@link Vector3 scale} to set to.
     * @return This transform after it has been updated.
     */
    public Transform scale(Vector3 scale) {
        this.scale = scale;
        return this.updateLocal();
    }

    /**
     * Begin a sequence of pushing updates to this transform.
     * <p>
     *     In other words, after the call of this method, no
     *     updates will occur on this transform until the subsequent
     *     {@link #pop()} method is called to release the hold.
     * </p>
     *
     * @return This transform.
     */
    public Transform push() {
        this.updating = true;
        return this;
    }

    /**
     * End the sequence of pushing updates to this transform
     * and trigger and update.
     *
     * @return This transform after it has been updated.
     */
    public Transform pop() {
        this.updating = false;
        this.updateLocal();
        return this;
    }

    /**
     * Add an update listener to run the specified action
     * whenever this transform is updated.
     *
     * @param action The action to execute on an update.
     * @return This transform.
     */
    public Transform listen(Consumer<Transform> action) {
        this.updates = updates.andThen(action);
        return this;
    }

    /**
     * Clear all update listeners on this transform.
     *
     * @return This transform.
     */
    public Consumer<Transform> clearListeners() {
        Consumer<Transform> cleared = updates;
        this.updates = __ -> {};
        return cleared;
    }

    /**
     * Add the specified transform to this one as a next link.
     * <p>
     *     The transform will be updated within the context
     *     of this one using this as a base.
     * </p>
     * <p>
     *     If the specified transform has a previous link already,
     *     then it will be replaced with this one.
     * </p>
     *
     * @param transform The transform to add to this one.
     * @return This transform after it has been updated.
     */
    public Transform add(Transform transform) {
        // Exchange the transform's previous link for this
        Transform previous = transform.previous.getAndSet(this);
        if (previous != null) {
            // If there is already a previous link on
            // the transform we need to remove it properly
            previous.next.remove(transform);
        }
        // Add the transform to the next links and update
        this.next.add(transform);
        this.updateGlobal();
        return this;
    }

    /**
     * Remove the specified transform from this one.
     *
     * @param transform The transform to remove from this one.
     * @return This transform after it has been updated.
     */
    public Transform remove(Transform transform) {
        this.next.remove(transform);
        transform.previous.compareAndSet(this, null);
        transform.updateGlobal();
        return this;
    }

    private Transform updateLocal() {
        // Check whether this transform is currently ignoring updates
        if (updating) {
            return this;
        }
        // Update the local matrix and then push the values to global
        this.local.identity()
                .translate(pos)
                .rotate(rot)
                .scale(scale);
        return this.updateGlobal();
    }

    private Transform updateGlobal() {
        // Update the global transform based on this local
        Transform previous = this.previous.get();
        if (previous != null) {
            // If a previous transform exists, then we need to update
            // the global matrix within that context
            previous.global.mul(local, global);
        } else {
            // With no previous transform, the global is always
            // the same as local
            this.global.set(local);
        }
        // Update all the next transforms
        this.next.forEach(Transform::updateGlobal);
        if (updates != null) {
            this.updates.accept(this);
        }

        return this;
    }
}
