package com.projecki.fusion.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.projecki.fusion.redis.CommonRedisKeys;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Resolve player names and uuids from an on-premesis cache
 * or by using the MojangAPI. Player mappings are stored when
 * a player joins a proxy.
 */
@SuppressWarnings({"unused", "ClassCanBeRecord"})
public class NameResolver {

    private final Cache<UUID, String> uuidToName;
    private final Cache<String, UUID> nameToUuid;
    private final Cache<String, String> nameToName;

    private final MojangAPI mojangAPI;
    private final NameResolverStorage storage;

    {
        var builder = CacheBuilder.newBuilder()
                .concurrencyLevel(2)
                .maximumSize(500);
        //.expireAfterWrite(Duration.ofMinutes(30));

        uuidToName = builder.build();
        nameToUuid = builder.build();
        nameToName = builder.build();
    }

    /**
     * New {@link NameResolver} that uses {@code storage} as an
     * on-premisis cache for player name to uuid mappings
     *
     * @param executor the executor service used by the mojang api instance for async operations
     * @param storage  player mappings storage
     */
    public NameResolver(@NotNull Executor executor, NameResolverStorage storage) {
        this.mojangAPI = new MojangAPI(executor);
        this.storage = storage;
    }

    /**
     * Get a player uuid from the player's name.
     * <p>
     * This will only query the remote cache and not
     * Mojang servers, so it is possible this uuid is valid,
     * but not stored. If you want to use the MojangAPI as a
     * fall back use {@code resolveUuidMojang()}
     * <p>
     * If the player is a bedrock player that has not logged on,
     * their information will not return from this method.
     *
     * @param playerName name of player (case-insensitive)
     * @return future containing optional containing player uuid
     */
    public CompletableFuture<Optional<UUID>> resolveUuid(String playerName) {
        return CompletableFuture.completedFuture(nameToUuid.getIfPresent(playerName.toLowerCase()))
                .thenApply(Optional::ofNullable)
                .thenCompose(opt -> {
                    if (opt.isPresent()) return CompletableFuture.completedFuture(opt);
                    return storage.getUuid(playerName)
                            .whenComplete((val, exe) -> val.ifPresent(uuid -> {
                                nameToUuid.put(playerName.toLowerCase(), uuid);
                                uuidToName.put(uuid, playerName);
                            }));
                });
    }

    /**
     * Get a player name from the player's uuid.
     * <p>
     * This will only query the remote cache and not
     * Mojang servers, so it is possible this name is valid,
     * but not stored. If you want to use the MojangAPI as a
     * fall back use {@code resolveUuidMojang()}
     * <p>
     * If the player is a bedrock player that has not logged on,
     * their information will not return from this method.
     *
     * @param playerUuid uuid of player
     * @return future containing optional containing player uuid
     */
    public CompletableFuture<Optional<String>> resolveName(UUID playerUuid) {
        return CompletableFuture.completedFuture(uuidToName.getIfPresent(playerUuid))
                .thenApply(Optional::ofNullable)
                .thenCompose(opt -> {
                    if (opt.isPresent()) return CompletableFuture.completedFuture(opt);
                    return storage.getName(playerUuid)
                            .whenComplete((val, exe) -> val.ifPresent(name -> {
                                uuidToName.put(playerUuid, name);
                                nameToUuid.put(name.toLowerCase(), playerUuid);
                                nameToName.put(name.toLowerCase(), name);
                            }));
                });
    }

    /**
     * Get a properly capizalized player name from a player name.
     * <p>
     * If the proper name cannot be located, the same input will
     * be returned.
     * <p>
     * This will only query the remote cache and not
     * Mojang servers, so it is possible this name is valid,
     * but not stored. If you want to use the MojangAPI as a
     * fall back use {@code resolveUuidMojang()}
     * <p>
     * If the player is a bedrock player that has not logged on,
     * their information will not return from this method.
     *
     * @param playerName name of player (case-insensitive)
     */
    public CompletableFuture<String> resolveRealName(String playerName) {
        return storage.getName(playerName.toLowerCase())
                .whenComplete((val, exe) -> val.ifPresent(name -> nameToName.put(name.toLowerCase(), name)))
                .thenApply(opt -> opt.orElse(playerName));
    }

    /**
     * Get a player uuid from the player's name, and fallback to
     * using the MojangAPI if the uuid is not cached.
     * <p>
     * If the player is a bedrock player that has not logged on,
     * their information will not return from this method.
     *
     * @param playerName name of player
     * @return future containing optional containing player uuid
     */
    public CompletableFuture<Optional<UUID>> resolveUuidMojang(String playerName) {
        return resolveUuid(playerName)
                .thenCompose(opt -> {
                    if (opt.isPresent()) return CompletableFuture.completedFuture(opt);

                    // fetch uuid from mojang api
                    return mojangAPI.getUniqueId(playerName)
                            .thenCompose(optU -> {

                                // if we find an uuid, store it
                                if (optU.isPresent())
                                    return storage.store(playerName, optU.get()).thenApply(_v -> optU);

                                return CompletableFuture.completedFuture(optU);
                            });
                });
    }

    /**
     * Get a player name from the player's uuid, and fallback to
     * using the MojangAPI if the name is not cached.
     * <p>
     * If the player is a bedrock player that has not logged on,
     * their information will not return from this method.
     *
     * @param playerUuid uuid of player
     *
     * @return future containing optional containing player uuid
     */
    public CompletableFuture<Optional<String>> resolveNameMojang(UUID playerUuid) {
        return resolveName(playerUuid)
                .thenCompose(opt -> {
                    if (opt.isPresent()) return CompletableFuture.completedFuture(opt);

                    // fetch username from mojang api
                    return mojangAPI.getUsername(playerUuid)
                            .thenCompose(optName -> {

                                // if we find a name, store it
                                if (optName.isPresent())
                                    return storage.store(optName.get(), playerUuid).thenApply(_v -> optName);

                                return CompletableFuture.completedFuture(optName);
                            });
                });
    }

    /**
     * Get a properly capizalized player name from a player name,
     * and fallback to the MojangAPI if the name is not cached.
     * <p>
     * If the proper name cannot be located, the same input will
     * be returned.
     * <p>
     * This will only query the remote cache and not
     * Mojang servers, so it is possible this name is valid,
     * but not stored. If you want to use the MojangAPI as a
     * fall back use {@code resolveUuidMojang()}
     * <p>
     * If the player is a bedrock player that has not logged on,
     * their information will not return from this method.
     *
     * @param playerName name of player
     */
    public CompletableFuture<String> resolveRealNameMojang(String playerName) {
        return resolveRealName(playerName);
    }

    public interface NameResolverStorage {

        /**
         * Get a player uuid from the player's name.
         * <p>
         * Optional returns empty if there is a value stored,
         * but it's not able to be parsed into a uuid
         *
         * @param playerName name of player
         * @return future containing optional containing player uuid
         */
        CompletableFuture<Optional<UUID>> getUuid(String playerName);

        /**
         * Get a player name from the player's uuid.
         *
         * @param playerUuid uuid of player
         * @return future containing optional containing player name
         */
        CompletableFuture<Optional<String>> getName(UUID playerUuid);

        /**
         * Get a player name with the proper capitalizaion from their
         * name in lowercase. You do not need to convert the input
         * to lowercase before using this method, this method does that.
         *
         * @param playerName name of player
         * @return future containing optional containing real play name
         */
        CompletableFuture<Optional<String>> getName(String playerName);

        /**
         * Put a player name and uuid pair into the storage
         *
         * @param playerName name of the player
         * @param playerUuid uuid of the player
         * @return future that completes when storing is completed sucessfully
         */
        CompletableFuture<Void> store(String playerName, UUID playerUuid);

    }

    public static class RedisNameResolverStorage implements NameResolverStorage {

        private static final String KEY_PREFIX = CommonRedisKeys.PLAYER_CACHE.getKey() + ':';
        private final RedisAsyncCommands<String, String> connection;

        public RedisNameResolverStorage(RedisAsyncCommands<String, String> connection) {
            this.connection = connection;
        }

        @Override
        public CompletableFuture<Optional<UUID>> getUuid(String playerName) {
            return connection.get(getKey(playerName))
                    .thenApply(UUID::fromString)
                    .thenApply(Optional::ofNullable)
                    .exceptionally(exe -> Optional.empty())
                    .toCompletableFuture();
        }

        @Override
        public CompletableFuture<Optional<String>> getName(UUID playerUuid) {
            return connection.get(getKey(playerUuid))
                    .thenApply(Optional::ofNullable)
                    .toCompletableFuture();
        }

        @Override
        public CompletableFuture<Optional<String>> getName(String playerName) {
            return connection.get(getKeyName(playerName))
                    .thenApply(Optional::ofNullable)
                    .toCompletableFuture();
        }

        @Override
        public CompletableFuture<Void> store(String playerName, UUID playerUuid) {
            return CompletableFuture.allOf(
                    connection.set(getKey(playerName), playerUuid.toString()).toCompletableFuture(),
                    connection.set(getKey(playerUuid), playerName).toCompletableFuture(),
                    connection.set(getKeyName(playerName), playerName).toCompletableFuture()
            );
        }

        private static String getKey(UUID uuid) {
            return KEY_PREFIX + uuid.toString();
        }

        private static String getKey(String playerName) {
            return KEY_PREFIX + playerName.toLowerCase();
        }

        public static String getKeyName(String playerName) {
            return KEY_PREFIX + "name:" + playerName.toLowerCase();
        }

    }
}
