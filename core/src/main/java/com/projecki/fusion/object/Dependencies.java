package com.projecki.fusion.object;

import com.projecki.fusion.Bootstrap;
import com.projecki.fusion.FusionCore;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @since May 20, 2022
 * @author Andavin
 */
public class Dependencies {

    private static final Field MODULE_OBJECT;
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Map<Class<?>, Dependencies> DEPENDENCIES = new HashMap<>();

    static {
        try {
            MODULE_OBJECT = Module.class.getDeclaredField("object");
            MODULE_OBJECT.trySetAccessible();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Collect all the {@link Dependencies} and sort them properly for
     * the specified type.
     *
     * @param type The type to collect the {@link Dependencies} for.
     * @return The {@link Dependencies} that were collected.
     */
    static Dependencies collect(Class<?> type) {
        return DEPENDENCIES.computeIfAbsent(type, Dependencies::new);
    }

    /**
     * Collect all the {@link Dependencies} and sort them properly for
     * the specified type.
     *
     * @param type The type to collect the {@link Dependencies} for.
     * @return The {@link Dependencies} that were collected.
     */
    static List<Dependency> collectDependencies(Class<?> type) {
        return collect(type).dependencies;
    }

    private final List<Dependency> dependencies;
    private final Constructor<? extends Module> moduleConstructor;

    private Dependencies(Class<?> type) {
        this.dependencies = sort(findDependencies(type));
        this.moduleConstructor = Module.class.isAssignableFrom(type) ?
                findConstructor((Class<? extends Module>) type) : null;
    }

    /**
     * Create a new {@link Stream} of all the
     * {@link Dependency dependencies} within this object.
     *
     * @return The new stream.
     */
    Stream<Dependency> stream() {
        return dependencies.stream();
    }

    /**
     * Create a new instance of the {@link Module} the class
     * for which this object was created is a {@link Module}.
     *
     * @param object The {@link ModularObject} to use.
     * @return The newly created {@link Module}.
     * @param <T> The type of {@link Module} to return.
     * @throws NullPointerException If this object is not of a {@link Module}.
     */
    <T extends Module> T createModule(ModularObject<T> object) {
        checkNotNull(moduleConstructor, "not a module");
        return newInstance(moduleConstructor, object);
    }

    private static List<Dependency> findDependencies(Class<?> type) {

        List<Dependency> dependencies = new ArrayList<>();
        for (Class<?> annotatedType = type;
             annotatedType != Object.class;
             annotatedType = annotatedType.getSuperclass()) {

            DependsOn[] depends = annotatedType.getDeclaredAnnotationsByType(DependsOn.class);
            for (DependsOn depend : depends) {
                dependencies.add(new Dependency(depend));
                dependencies.addAll(findDependencies(depend.value()));
            }

            DependsOnAll[] allDepends = annotatedType.getDeclaredAnnotationsByType(DependsOnAll.class);
            for (DependsOnAll allDepend : allDepends) {

                Set<Class<? extends Module>> subTypes = (Set) Bootstrap.REFLECTIONS.getSubTypesOf(allDepend.value());
                for (Class<? extends Module> subType : subTypes) {
                    dependencies.add(new Dependency(true, subType));
                    dependencies.addAll(findDependencies(subType));
                }
            }
        }

        return dependencies;
    }

    private static List<Dependency> sort(List<Dependency> dependencies) {

        if (dependencies.isEmpty()) {
            return List.of();
        }

        /*
         * Create a sorted list of dependencies. Each item in this list
         * will never be preceded by its own dependency.
         *
         * Therefore, an ordered iteration of this list will yield a
         * perfect structure of initialization.
         */

        List<Dependency> sorted = new ArrayList<>(dependencies.size());
        List<SortedNode> nodes = dependencies.stream()
                .filter(Dependency::explicit)
                .map(SortedNode::new)
                .collect(toList()); // NOTE: needs to be mutable
        do {

            boolean missingOrCircular = true;
            Iterator<SortedNode> itr = nodes.iterator();
            while (itr.hasNext()) {

                SortedNode node = itr.next();
                if (node.dependencyNodes.isEmpty()) {
                    // If no dependencies remain for this node, then it can
                    // be safely added to the sorted list
                    missingOrCircular = false;
                    sorted.add(node.dependency);
                    for (SortedNode other : nodes) {
                        // Once added, we need to remove this node as a dependency of
                        // other nodes as it is now in the list and can be depended on
                        other.dependencyNodes.remove(node);
                    }

                    itr.remove();
                }
            }
            // Everything that remains in the list has dependencies. This means
            // that we have a dependency that is either missing or circular
            // (i.e. depends on each other directly or indirectly).
            if (missingOrCircular) {

                int size = nodes.size();
                Logger logger = FusionCore.LOGGER;
                logger.error("Found missing or circular dependencies for {} nodes...", size);
                for (int i = 0; i < size; i++) {
                    SortedNode node = nodes.get(i);
                    logger.error("{}. {} ({})", i + 1, node.type, node.type.getSimpleName());
                    nodes.add(node);
                }

                logger.error("This should be corrected as soon as possible!");
                break;
            }
        } while (!nodes.isEmpty());

        return List.copyOf(sorted);
    }

    @NotNull
    private static Constructor<? extends Module> findConstructor(Class<? extends Module> type) {

        try {
            Constructor<? extends Module> constructor = type.getDeclaredConstructor(EMPTY_CLASS_ARRAY);
            constructor.trySetAccessible();
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("no default constructor in " + type, e);
        }
    }

    private static <T extends Module> T newInstance(Constructor<? extends Module> constructor, ModularObject<T> object) {

        try {
            T module = (T) constructor.newInstance();
            MODULE_OBJECT.set(module, object);
            return module;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static final class Dependency {

        private final boolean explicit;
        private final Constructor<? extends Module> constructor;

        Dependency(DependsOn depend) {
            this(!depend.soft(), depend.value());
        }

        Dependency(boolean explicit, Class<? extends Module> type) {
            this.explicit = explicit;
            this.constructor = findConstructor(type);
        }

        /**
         * The type of the dependency module.
         *
         * @return The module type.
         */
        public Class<?> type() {
            return constructor.getDeclaringClass();
        }

        /**
         * Tell whether this dependency is explicitly specified
         * as a dependency.
         *
         * @return {@code true} if this dependency is explicit.
         * @see DependsOn#soft() Derived from
         */
        public boolean explicit() {
            return explicit;
        }

        /**
         * Create a new {@link Module} by passing the specified
         * {@link ModularObject} to the constructor.
         *
         * @param object The {@link ModularObject} to use.
         * @return The newly created {@link Module}.
         * @param <T> The type of {@link Module} to return.
         */
        public <T extends Module> T create(ModularObject<T> object) {
            return newInstance(constructor, object);
        }
    }

    private static final class SortedNode {

        private final Class<?> type;
        private final Dependency dependency;
        private final Set<SortedNode> dependencyNodes;

        public SortedNode(Dependency dependency) {
            this.dependency = dependency;
            this.type = dependency.constructor.getDeclaringClass();
            this.dependencyNodes = findDependencies(type).stream()
                    .map(SortedNode::new)
                    .collect(toSet());
        }

        @Override
        public int hashCode() {
            return type.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o == this || o instanceof SortedNode n && n.type.equals(type);
        }
    }
}
