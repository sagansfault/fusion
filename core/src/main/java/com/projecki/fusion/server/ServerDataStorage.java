package com.projecki.fusion.server;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Store and retrieve {@link ServerData} for all active
 * and possibly inactive servers on the network
 */
public interface ServerDataStorage {

    /**
     * Store {@link ServerData} in storage by server name
     *
     * @param info serverData containing new information
     * @return future that completes once the information is successfully stored
     */
    CompletableFuture<Void> storeInfo(ServerData info);

    /**
     * Get a stored {@link ServerData} of a specified type that is a super of {@link ServerData}
     * by the specified server name. {@link CompletableFuture}` will complete successfully if no
     * data for that server exists, but the internal {@link Optional} will be empty.
     *
     * @param serverName name of server
     * @param clazz class of {@link T} type
     * @param <T> type that is a super of {@link ServerData}
     * @return future that completes once the data is retrieved
     */
    <T extends ServerData> CompletableFuture<Optional<T>> getInfo(String serverName, Class<T> clazz);

    /**
     * Delete info for the specified server from storage
     *
     * @param serverName name of server
     * @return future that completes once the data is deleted
     */
    CompletableFuture<Void> deleteInfo(String serverName);

    /**
     * Get a set of all stored {@link ServerData} objects in storage of a specified
     * type that is a super of {@link ServerData}.
     *
     * @param clazz class of {@link T} type
     * @param <T> type that is a super of {@link ServerData}
     * @return future that completes once all the data is retrieved
     */
    <T extends ServerData> CompletableFuture<Set<T>> getAllServerData(Class<T> clazz);

}
