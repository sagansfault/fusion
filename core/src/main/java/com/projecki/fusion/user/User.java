package com.projecki.fusion.user;

import com.projecki.fusion.object.ModularObject;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

/**
 * @since May 27, 2022
 * @author Andavin
 */
public abstract class User<T> extends ModularObject<UserModule> {

    private static final WeakReference<?> ABSENT = new WeakReference<>(null);

    private WeakReference<T> reference = (WeakReference<T>) ABSENT;

    /**
     * Get the name of this user.
     *
     * @return The name.
     */
    public abstract String name();

    /**
     * The underlying reference that this user is built upon.
     * <p>
     *     This reference is platform dependent.
     * </p>
     *
     * @return The reference.
     * @param <R> The type of reference to return.
     */
    public <R extends T> R reference() {
        return (R) reference.get();
    }

    /**
     * Set the underlying reference for this user.
     *
     * @param reference The reference to set to.
     */
    protected void reference(@NotNull T reference) {
        this.reference = new WeakReference<>(reference);
    }

    /**
     * Clear the underlying reference for this user.
     */
    protected void clearReference() {
        this.reference.clear();
    }

    /**
     * Determine whether this user is currently online.
     * <p>
     *     This is determined by testing whether there is a
     *     {@link #reference()} currently present.
     * </p>
     *
     * @return If the user is online.
     */
    public boolean isOnline() {
        return !reference.refersTo(null);
    }

    /**
     * {@inheritDoc}
     * <p>
     *     The {@link #reference()} may not be available during
     *     this step and should be avoided. However, the implementation
     *     will always provide essential identifiers by this step.
     * </p>
     */
    @Override
    protected void onSetup() {
    }

    @Override
    protected boolean validate(UserModule module) {
        // TODO: add error logging if annotation is not present
        return module.getClass().isAnnotationPresent(UserModuleId.class);
    }

    @Override
    public String toString() {
        return this.name();
    }
}
