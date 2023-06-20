package com.projecki.fusion.object;

import com.projecki.fusion.object.Dependencies.Dependency;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * A general purpose modular object.
 * <p>
 *     A modular object is an object that has {@link Module modules}
 *     each of which can depend on other {@link Module modules} and
 *     each object can depend on modules.
 * </p>
 * <p>
 *     When an object loads it will collect the {@link Module modules}
 *     it  depends on as well as transitive dependencies and order them
 *     via a topological sort to ensure initialization order.
 * </p>
 * <p>
 *     Note that, once fully constructed, the order of {@link Module modules}
 *     is no longer guaranteed to be dependency due to the use of the
 *     {@link #add(Class)} and {@link #remove(Class)} methods.
 * </p>
 *
 * @since May 19, 2022
 * @author Andavin
 */
public abstract class ModularObject<T extends Module> {

    private boolean destroyed;
    private final Set<InitializationStep> initialized =
            EnumSet.noneOf(InitializationStep.class);

    private final List<T> modules;
    private final Map<Class<?>, List<T>> moduleHierarchy;

    protected ModularObject() {
        this.modules = Dependencies.collectDependencies(this.getClass()).stream()
                .filter(Dependency::explicit)
                .map(d -> d.create(this))
                .filter(this::validate)
                .collect(toList()); // NOTE: needs to be mutable
        this.moduleHierarchy = computeHierarchy(modules);
    }

    /**
     * Initialize this object and all its {@link Module modules}.
     */
    public final void initialize() {
        this.initialize(InitializationStep.SETUP);
        this.initialize(InitializationStep.CREATE);
        this.initialize(InitializationStep.ENABLE);
    }

    /**
     * Initialize this object and all its {@link Module modules}.
     *
     * @param step The {@link InitializationStep} to initialize for.
     */
    public final void initialize(InitializationStep step) {

        if (initialized.contains(step)) {
            return;
        }

        switch (step) {
            case SETUP -> {
                this.initialized.add(InitializationStep.SETUP);
                this.onSetup();
                this.modules.forEach(Module::onAdd);
            }
            case CREATE -> {
                this.initialized.add(InitializationStep.CREATE);
                this.onCreate();
            }
            case ENABLE -> {

                this.initialized.add(InitializationStep.ENABLE);
                for (T module : modules) {

                    if (!module.getClass().isAnnotationPresent(DisableByDefault.class)) {
                        module.enable();
                    }
                }

                this.onEnable();
            }
        }
    }

    /**
     * Irreversibly destroy this object.
     */
    public final void destroy() {

        if (destroyed) {
            return;
        }

        this.destroyed = true;
        this.onDisable();
        this.modules.forEach(Module::disable);
        this.onDestroy();
        this.modules.forEach(Module::onRemove);
        this.modules.clear();
        this.moduleHierarchy.clear();
    }

    /**
     * Determine if this object is destroyed.
     *
     * @return If this object is destroyed.
     */
    public final boolean isDestroyed() {
        return destroyed;
    }

    /**
     * The method called to validate a {@link Module} before
     * it is added to this object.
     * <p>
     *     This method should be overridden by inheritors to validate
     *     custom properties of {@link Module modules}.
     * </p>
     *
     * @param module The {@link Module} to validate.
     * @return If the {@link Module} is valid.
     */
    protected boolean validate(T module) {
        return true;
    }

    /**
     * The method called when this object is being first
     * initialized before anything else.
     */
    protected void onSetup() {
    }

    /**
     * The method called when this object is set up and
     * {@link Module modules} are added, but not yet enabled.
     */
    protected void onCreate() {
    }

    /**
     * The method called when this object is fully enabled.
     */
    protected void onEnable() {
    }

    /**
     * The method called when this object is being disabled.
     */
    protected void onDisable() {
    }

    /**
     * The method called when this object is being destroyed.
     * All {@link Module modules} are disabled, but not yet removed.
     */
    protected void onDestroy() {
    }

    /**
     * Get all the {@link Module Modules} present on this object.
     *
     * @return The list of all {@link Module Modules}.
     */
    public List<T> modules() {
        return unmodifiableList(modules);
    }

    /**
     * Get the first {@link Module} of the specified type
     * that is present on this object.
     * <p>
     *     This is not {@link Nullable} because it is easily
     *     possible to know that a {@link Module} will be present
     *     on this object.
     *     <br>
     *     If this is unknown, then {@link #getOptional(Class)}
     *     should be preferred to this method.
     * </p>
     *
     * @param type The {@link Module} type to get.
     * @return The first {@link Module} on this object of the type.
     * @param <M> The {@link Module} type to get.
     */
    public <M> M get(Class<M> type) {
        List<T> modules = moduleHierarchy.get(type);
        return modules != null && !modules.isEmpty() ?
                type.cast(modules.get(0)) : null;
    }

    /**
     * Get the first {@link Module} of the specified type
     * that is present on this object.
     *
     * @param type The {@link Module} type to get.
     * @return The first {@link Module} on this object of the type.
     * @param <M> The {@link Module} type to get.
     */
    @NotNull
    public <M> Optional<M> getOptional(Class<M> type) {
        List<T> modules = moduleHierarchy.get(type);
        return modules != null && !modules.isEmpty() ?
                Optional.of(type.cast(modules.get(0))) : Optional.empty();
    }

    /**
     * Get all the {@link Module Modules} of the specified
     * type that are present on this object.
     *
     * @param type The {@link Module} type to get.
     * @return The list of {@link Module Modules} on this
     *         object of the type.
     * @param <M> The {@link Module} type to get.
     */
    @NotNull
    public <M> List<M> getAll(Class<M> type) {
        return (List<M>) moduleHierarchy.getOrDefault(type, List.of());
    }

    /**
     * Determine whether this object has a {@link Module} that
     * is assignable from the specified type.
     *
     * @param type The type to determine if a {@link Module} is assignable from.
     * @return If this object has a {@link Module} of the type.
     */
    public boolean has(Class<?> type) {
        return moduleHierarchy.containsKey(type);
    }

    /**
     * Add a new {@link Module} of the specified type to
     * this object.
     * <p>
     *     This will also add all the required dependencies of
     *     the specified {@link Module} type to this object.
     * </p>
     *
     * @param type The type of {@link Module} to add.
     */
    public void add(Class<? extends T> type) {

        Dependencies dependencies = Dependencies.collect(type);
        List<T> modules = dependencies.stream()
                .filter(d -> !this.has(d.type()))
                .filter(Dependency::explicit)
                .map(d -> d.create(this))
                .filter(this::validate)
                .toList();
        this.modules.addAll(modules);
        modules.forEach(Module::onAdd);
        for (T module : modules) {

            addHierarchy(moduleHierarchy, module);
            if (!module.getClass().isAnnotationPresent(DisableByDefault.class)) {
                module.enable();
            }
        }

        T module = dependencies.createModule(this);
        if (this.validate(module)) {

            this.modules.add(module);
            module.onAdd();
            addHierarchy(moduleHierarchy, module);
            if (!module.getClass().isAnnotationPresent(DisableByDefault.class)) {
                module.enable();
            }
        }
    }

    /**
     * Remove the {@link Module} of the specified type from
     * this object.
     * <p>
     *     This will <b>not</b> remove any dependencies of the
     *     {@link Module} type from this object.
     * </p>
     *
     * @param type The type of {@link Module} to remove.
     */
    public void remove(Class<? extends T> type) {
        // Remove the modules
        this.modules.removeIf(m -> {

            if (type.isInstance(m)) {
                m.disable();
                m.onRemove();
                return true;
            }

            return false;
        });
        // Remove the modules from the entire hierarchy
        this.moduleHierarchy.values().removeIf(m ->
                m.removeIf(type::isInstance) && m.isEmpty());
    }

    /**
     * Find and enable all the {@link Module Modules} of the specified types
     * if they are currently {@link Module#isDisabled() disabled}.
     * <p>
     *     If this object does not currently have any {@link Module Modules}
     *     of the specified types, this method will not enable anything.
     * </p>
     *
     * @param types The types of {@link Module Modules} to enable.
     */
    public void enable(Class<? extends T>... types) {

        for (Class<? extends T> type : types) {

            for (T t : this.getAll(type)) {

                if (t.isDisabled()) {
                    t.enable();
                }
            }
        }
    }

    /**
     * Enable all {@link Module Modules} attached to this object
     * except for those of the specified types.
     *
     * @param types The types of {@link Module Modules} to exclude
     *              from being enabled.
     */
    public void enableAllExcept(Class<? extends T>... types) {

        nextModule:
        for (T t : modules) {

            if (t.isDisabled()) {

                for (Class<? extends T> type : types) {

                    if (type.isInstance(t)) {
                        continue nextModule;
                    }
                }
                // Enable the module if it is disabled
                t.enable();
            }
        }
    }

    /**
     * Find and disable all the {@link Module Modules} of the specified types
     * if they are currently {@link Module#isEnabled() enabled}.
     * <p>
     *     If this object does not currently have any {@link Module Modules}
     *     of the specified types, this method will not disable anything.
     * </p>
     *
     * @param types The types of {@link Module Modules} to disable.
     */
    public void disable(Class<? extends T>... types) {

        for (Class<? extends T> type : types) {
            this.getAll(type).forEach(Module::disable); // Only works if enabled
        }
    }

    /**
     * Disable all {@link Module Modules} attached to this object
     * except for those of the specified types.
     *
     * @param types The types of {@link Module Modules} to exclude
     *              from being disabled.
     */
    public void disableAllExcept(Class<? extends T>... types) {

        nextModule:
        for (T t : modules) {

            if (t.isEnabled()) {

                for (Class<? extends T> type : types) {

                    if (type.isInstance(t)) {
                        continue nextModule;
                    }
                }
                // Disable the module if it is enabled
                t.disable();
            }
        }
    }

    @Override
    public abstract boolean equals(@Nullable Object o);

    @Override
    public abstract String toString();

    private static <T extends Module> Map<Class<?>, List<T>> computeHierarchy(List<T> modules) {
        Map<Class<?>, List<T>> hierarchy = new HashMap<>();
        modules.forEach(m -> addHierarchy(hierarchy, m));
        return hierarchy; // NOTE: mutable
    }

    private static <T extends Module> void addHierarchy(Map<Class<?>, List<T>> map, T module) {

        for (Class<?> current = module.getClass();
             current != Object.class;
             current = current.getSuperclass()) {
            map.computeIfAbsent(current, __ -> new ArrayList<>(1)).add(module);
            addInterfaces(map, current, module);
        }
    }

    private static <T extends Module> void addInterfaces(Map<Class<?>, List<T>> map, Class<?> type, T module) {

        for (Class<?> i : type.getInterfaces()) {
            map.computeIfAbsent(i, __ -> new ArrayList<>(1)).add(module);
            addInterfaces(map, i, module);
        }
    }

    public enum InitializationStep {
        SETUP,
        CREATE,
        ENABLE
    }
}
