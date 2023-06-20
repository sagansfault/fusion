package com.projecki.fusion.user;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * A collection object that manages {@link User} objects.
 *
 * @since May 30, 2022
 * @author Andavin
 */
public interface Users<U extends User<T>, T> {

    /**
     * Get the {@link User} from the specified reference.
     *
     * @param reference The reference to get the {@link User} by.
     * @return The {@link User} for the reference.
     */
    @NotNull
    U get(T reference);

    /**
     * Get all the {@link User Users} that are currently online.
     *
     * @return The online {@link User Users}.
     */
    List<U> getOnline();

    /**
     * Get all the {@link User Users} that are currently loaded.
     *
     * @return The loaded {@link User Users}.
     */
    Collection<U> getAll();
}
