package com.projecki.fusion.user;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a unique identifier for the annotated {@link UserModule}.
 *
 * @since May 27, 2022
 * @author Andavin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserModuleId {

    // At the moment, this is required but unused.
    // It is intended for use in identifying data for a specific module
    // once this is implemented this ID will be needed

    /**
     * The unique identifier for the {@link UserModule}.
     *
     * @return The identifier.
     */
    String value();
}
