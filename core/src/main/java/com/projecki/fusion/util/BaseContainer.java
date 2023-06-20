package com.projecki.fusion.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A base container class that has support for mapping a key of type K to a value of type V.
 * This container provides some chill features like {@link Optional} get methods.
 *
 * @param <K> the type of the key used by the container
 * @param <V> the type of the value used by the container
 */
public class BaseContainer<K, V> {

    /**
     * The map instance that holds the contained data
     */
    private final Map<K, V> containerMap;

    /**
     * Construct a new {@link BaseContainer}.
     *
     * @param containerMap the map instance to use for the container
     */
    public BaseContainer(@NotNull Map<K, V> containerMap) {
        this.containerMap = containerMap;
    }

    /**
     * Construct a new {@link BaseContainer}.
     * This constructor uses a plain {@link HashMap} as it's {@link Map} implementation.
     */
    public BaseContainer() {
        this(new HashMap<>());
    }

    /**
     * Get a value from the {@link BaseContainer} for the specified key.
     *
     * @param key the key to retrieve the value from for the container
     * @return the value wrapped in an {@link Optional}, this {@link Optional} will be empty if there's no value for
     * the specified key
     */
    public Optional<V> getValue(@NotNull K key) {
        return containerMap.containsKey(key) ? Optional.of(containerMap.get(key)) : Optional.empty();
    }

    /**
     * Put a value in the {@link BaseContainer} using the specified key.
     *
     * @param key   the key to use for the value
     * @param value the value to store in the container
     */
    public void setValue(@NotNull K key, @NotNull V value) {
        containerMap.put(key, value);
    }

    /**
     * Remove the value from the {@link BaseContainer} using the specified key.
     *
     * @param key the key to use for the value
     *
     * @return an {@link Optional} that contains the value that just got removed
     */
    public Optional<V> removeValue (@NotNull K key) {
        return Optional.ofNullable(containerMap.remove(key));
    }

    /**
     * Get an <strong>unmodifiable</strong> view of the contained data in this {@link BaseContainer}.
     *
     * @return an unmodifiable view of the contained data in this container
     */
    @NotNull
    @Unmodifiable
    public Map<K, V> getContainerMap() {
        return Collections.unmodifiableMap(containerMap);
    }
}
