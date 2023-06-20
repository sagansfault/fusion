package com.projecki.fusion.object;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since May 20, 2022
 * @author Andavin
 */
public class Module<T extends ModularObject> {

    final T object = null; // Set via reflection
    private final AtomicInteger disabled = new AtomicInteger(1); // Initially disabled

    protected Module() {
    }

    /**
     * The {@link ModularObject} that this {@link Module}
     * is attached to (i.e. a module of).
     * <p>
     *     Note that this value may be {@code null} if called
     *     from within the constructor.
     * </p>
     *
     * @return The {@link ModularObject}.
     */
    public T object() {
        //noinspection ConstantConditions
        return object;
    }

    /**
     * The method called when this {@link Module} is added
     * to the {@link ModularObject}.
     */
    protected void onAdd() {
    }

    /**
     * The method called when this {@link Module} is enabled
     * either via the initial setup or when {@link #enable()}
     * is called.
     */
    protected void onEnable() {
    }

    /**
     * The method called when this {@link Module} is disabled
     * most likely when {@link #disable()} is called.
     */
    protected void onDisable() {
    }

    /**
     * The method called when this {@link Module} is removed
     * from the {@link ModularObject}.
     */
    protected void onRemove() {
    }

    /**
     * Determine whether this {@link Module} is currently enabled.
     *
     * @return If this module is enabled.
     */
    public final boolean isEnabled() {
        return disabled.get() == 0;
    }

    /**
     * Determine whether this {@link Module} is currently disabled.
     *
     * @return If this module is disabled.
     */
    public final boolean isDisabled() {
        return disabled.get() > 0;
    }

    /**
     * Set whether this {@link Module} is enabled.
     *
     * @param enabled If this module should be enabled.
     */
    public final void setEnabled(boolean enabled) {

        if (enabled) {
            this.enable();
        } else {
            this.disable();
        }
    }

    /**
     * Enable this {@link Module}.
     * <p>
     *     Enabling and disabling a {@link Module} is executed
     *     in stages. This method must be called the same amount of
     *     times as {@link #disable()} in order for a module to
     *     be fully enabled.
     *     <br>
     *     This is to allow multiple users to enable and disable
     *     {@link Module modules} with consistency.
     * </p>
     */
    public final void enable() {

        if (disabled.get() > 0 && disabled.decrementAndGet() == 0) { // Last enable
            this.onEnable();
        }
    }

    /**
     * Disable this {@link Module}.
     *
     * @see #enable()
     */
    public final void disable() {

        if (disabled.getAndIncrement() == 0) { // First disable
            this.onDisable();
        }
    }

    /**
     * Fully enable this {@link Module}.
     * <p>
     *     Enabling and disabling a {@link Module} is executed
     *     in stages. This method will force this {@link Module}
     *     to become fully enabled rather than only moving a
     *     single stage.
     * </p>
     * <p>
     *     This method should only be used in rare circumstances
     *     and when the user knows the ramifications.
     * </p>
     * Usage will often follow this format:
     * <pre>
     *     int disables = module.fullyEnable();
     *     ... execute on enabled modules ...
     *     module.fullyDisable(disables);
     *</pre>
     *
     * @return The amount of {@link #disable() disables} this
     *         {@link Module} has incurred.
     * @see #fullyDisable(int)
     * @see #enable()
     * @see #disable()
     */
    public final int fullyEnable() {
        int count = disabled.getAndSet(0);
        this.onEnable();
        return count;
    }

    /**
     * Disable this {@link Module} to the specified {@code count}.
     * <p>
     *     This method should only be used in rare circumstances
     *     and when the user knows the ramifications.
     * </p>
     * Usage will often follow this format:
     * <pre>
     *     int disables = module.fullyEnable();
     *     ... execute on enabled modules ...
     *     module.fullyDisable(disables);
     *</pre>
     *
     * @param count The amount of disables to incur on this {@link Module}.
     * @see #fullyEnable()
     */
    public final void fullyDisable(int count) {

        if (disabled.addAndGet(count) > 0) {
            this.onDisable();
        }
    }
}
