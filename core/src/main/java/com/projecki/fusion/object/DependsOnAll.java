package com.projecki.fusion.object;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes that the annotated {@link ModularObject} depends
 * on all subclasses of the specified {@link Module}.
 * <p>
 *     In other words, the all classes that are found that extend
 *     the specified {@link Module} type will be added as dependencies
 *     of the annotated {@link ModularObject} type.
 * </p>
 *
 * @since June 23, 2022
 * @author Andavin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DependsOnAll {

    /**
     * The type of {@link Module} to depend on all subclasses of.
     *
     * @return The {@link Module} type.
     */
    Class<? extends Module> value();
}
