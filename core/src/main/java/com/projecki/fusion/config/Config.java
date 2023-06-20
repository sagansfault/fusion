package com.projecki.fusion.config;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Config<T> {

    /**
     * Stores the config as a serialized string in any reliable data structure. Serializing is left to the lower
     * implementations.
     *
     * @param object The config object to store
     * @return A completable future that completes once the object has been successfully stored.
     */
    CompletableFuture<Void> storeConfig(T object);

    /**
     * Returns a completable future containing an optional potentially containing the data reconstructed.
     * Check the lower implementation of this method to see what constitutes the difference between an exceptionally
     * completed future and an empty optional.
     *
     * @return a completable future containing an optional potentially containing the data reconstructed.
     */
    CompletableFuture<Optional<T>> loadConfig();
}
