package com.projecki.fusion.network.redis;

import com.google.common.primitives.Longs;
import com.projecki.fusion.network.PlayerStorage;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RedisPlayerStorage implements PlayerStorage {

    private static final String PLAYER_LIST = "cerberus:players";
    private static final String PLAYER_HASH = "cerberus:player:"; // don't change, this value is used externally at https://github.dev/Projecki-LLC/multiproxy-vote-forward

    private static final String SERVER_KEY = "server";
    private static final String PROXY_KEY = "proxy";
    private static final String HEARTBEAT = "heartbeat";
    private static final String LAST_ONLINE = "last_online";

    private final RedisAsyncCommands<String, String> redis;

    public RedisPlayerStorage(RedisAsyncCommands<String, String> redis) {
        this.redis = redis;
    }


    private String getPlayerKey(UUID player) {
        return PLAYER_HASH + player;
    }

    @Override
    public void serverChange(UUID player, String serverName) {
        redis.hset(getPlayerKey(player), SERVER_KEY, serverName);
    }

    @Override
    public CompletableFuture<Optional<String>> getPlayerServer(UUID player) {
        return redis.hget(getPlayerKey(player), SERVER_KEY)
                .thenApply(Optional::ofNullable)
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Optional<String>> getPlayerProxy(UUID player) {
        return redis.hget(getPlayerKey(player), PROXY_KEY)
                .thenApply(Optional::ofNullable)
                .toCompletableFuture();
    }

    @Override
    public void playerLeft(UUID player) {
        redis.srem(PLAYER_LIST, player.toString());
        redis.hdel(getPlayerKey(player), SERVER_KEY, PROXY_KEY, HEARTBEAT);
        redis.hset(getPlayerKey(player), LAST_ONLINE, String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void playerJoined(UUID player, String proxyName) {
        redis.hset(getPlayerKey(player), PROXY_KEY, proxyName);
        redis.sadd(PLAYER_LIST, player.toString());
    }

    @Override
    public CompletableFuture<Optional<Long>> getLastHeartbeat(UUID player) {
        return getHashLong(player, HEARTBEAT);
    }

    @Override
    public void storeLastHeartbeat(UUID player) {
        redis.hset(getPlayerKey(player), HEARTBEAT, String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public CompletableFuture<Optional<Long>> getLastOnline(UUID player) {
        return getHashLong(player, LAST_ONLINE);
    }

    private CompletableFuture<Optional<Long>> getHashLong(UUID player, String key) {
        //noinspection UnstableApiUsage
        return redis.hget(getPlayerKey(player), key)
                .thenApply(Optional::ofNullable)
                .thenApply(opt -> opt.map(Longs::tryParse))
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Long> getPlayerCount() {
        return redis.scard(PLAYER_LIST)
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Set<UUID>> getOnlinePlayers() {
        return redis.smembers(PLAYER_LIST)
                .thenApply(strings ->
                    strings.stream()
                            .map(uuidString -> {
                                try {
                                    return UUID.fromString(uuidString);
                                } catch (IllegalArgumentException e) {
                                    Logger.getGlobal().info("String present in '" + PLAYER_LIST + "' that" +
                                            "cannot be converted to a UUID: " + uuidString);
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet())
                )
                .toCompletableFuture();
    }
}
