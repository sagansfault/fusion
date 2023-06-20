package com.projecki.fusion.server;

import com.projecki.fusion.redis.CommonRedisKeys;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Store {@link ServerData} using redis
 */
public class RedisServerDataStorage implements ServerDataStorage {

    private final RedisAsyncCommands<String, String> commands;

    @Nullable
    private CompletableFuture<Set<RawServerData>> allServerDataFuture;

    /**
     * Create a new {@link RedisServerDataStorage} from a redis client.
     * A redis client is required because this class may need to procure itself
     * multiple connections as it sometimes uses transactions, which would have
     * unintended consequences if that connection was shared
     *
     * @param redisClient redis client to use for internal operation
     */
    public RedisServerDataStorage(RedisClient redisClient) {
        this.commands = redisClient.connect().async();
    }

    /**
     * {@inheritDoc} <p>
     * Only fields that are present inside of the provided {@link ServerData} will be
     * updated into Redis. There is no way to clear a field from server without using
     * {@code deleteInfo()} to delete the entire object in redis.
     */
    @Override
    public CompletableFuture<Void> storeInfo(ServerData info) {
        return commands.hset(getServerKey(info.getServerName()), info.getFields())
                .<Void>thenApply(v -> null)
                .toCompletableFuture();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends ServerData> CompletableFuture<Optional<T>> getInfo(String serverName, Class<T> clazz) {
        return getServerData(serverName)
                .thenApply(data -> createObject(clazz, data))
                .toCompletableFuture();
    }

    private CompletableFuture<RawServerData> getServerData(String serverName) {
        return commands.hgetall(getServerKey(serverName))
                .thenApply(map -> new RawServerData(serverName, map))
                .toCompletableFuture();
    }

    private <T extends ServerData> Optional<T> createObject(Class<T> clazz, RawServerData rawServerData) {
        // if the map is empty that means the key doesn't exist
        if (rawServerData.serverData().isEmpty()) {
            return Optional.empty();
        }

        T serverInfo = null;

        try {
            serverInfo = clazz.getConstructor(String.class).newInstance(rawServerData.serverName());
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (serverInfo != null) {
            serverInfo.setFields(rawServerData.serverData());
            serverInfo.setServer(rawServerData.serverName());
        }

        return Optional.ofNullable(serverInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> deleteInfo(String serverName) {
        return commands.del(getServerKey(serverName))
                .<Void>thenApply(v -> null)
                .toCompletableFuture();
    }

    /**
     * {@inheritDoc}<p>
     * This method uses {@code CommonRedisKeys.SERVER_LOOKUP}, which points to a hash
     * in redis that contains all of the server name, to find all the server names to
     * get all of the server data.
     */
    @Override
    public <T extends ServerData> CompletableFuture<Set<T>> getAllServerData(Class<T> clazz) {
        if (allServerDataFuture == null || allServerDataFuture.isDone()) {
            List<String> keys = new ArrayList<>();
            List<CompletableFuture<RawServerData>> futures = new ArrayList<>();

            allServerDataFuture = commands.hvals(CommonRedisKeys.SERVER_LOOKUP.getKey())
                    .thenAccept(keys::addAll)
                    .thenRun(() -> keys.forEach(s -> futures.add(getServerData(s))))
                    .thenCompose(v -> CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)))
                    .thenApply(v -> futures.stream()
                            .map(f -> f.getNow(null))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet()))
                    .toCompletableFuture();
        }

        return allServerDataFuture
                .thenApply(data -> data.stream()
                        .map(raw -> createObject(clazz, raw))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toUnmodifiableSet()));
    }

    private String getServerKey(String serverName) {
        return "server:" + serverName;
    }

    private record RawServerData(String serverName, Map<String, String> serverData) {
    }
}
