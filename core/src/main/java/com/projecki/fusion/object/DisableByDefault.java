package com.projecki.fusion.object;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes that the annotated element should be disabled
 * by default on initialization of a {@link ModularObject}.
 *
 * @since May 26, 2022
 * @author Andavin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DisableByDefault {
}
