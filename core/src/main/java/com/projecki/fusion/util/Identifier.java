package com.projecki.fusion.util;

/**
 * Represents an object with a unique value to be compared among others. Provides enforcement of Ids among objects
 * This class does nothing to ensure uniqueness, this is simply a utility interface to help enforce unique Ids.
 *
 * @param <T> The type of object to use to identify against. Typically Integer, Enum, String.
 */
public interface Identifier<T> {

    /**
     * The unique Id of this object
     *
     * @return The unique Id of this Object
     */
    T getId();
}
