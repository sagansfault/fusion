package com.projecki.fusion.object;

import com.projecki.fusion.object.DependsOn.Dependencies;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes that the annotated element depends on a
 * {@link Module} and is required in order to load.
 *
 * @since May 20, 2022
 * @author Andavin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Dependencies.class)
public @interface DependsOn {

    /**
     * The type of the {@link Module} that is depended on.
     *
     * @return The {@link Module}.
     */
    Class<? extends Module> value();

    /**
     * Denotes whether this dependency is soft and should not
     * be loaded automatically.
     * <p>
     *     If {@code true}, then the depended on {@link Module}
     *     will not be automatically loaded.
     * </p>
     * <p>
     *     Default: {@code false}
     * </p>
     *
     * @return Whether this dependency is soft.
     */
    boolean soft() default false;

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Dependencies {

        /**
         * The dependencies.
         *
         * @return The dependencies.
         */
        DependsOn[] value();
    }
}
