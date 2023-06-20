package com.projecki.fusion.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Resolve player (and non-player related) skins from an on-premises cache.
 * Player skins are stored when a player joins a proxy or manually if
 * the skin stored isn't related to one player.
 */
public class SkinResolver {

    private final Cache<UUID, SkinPair> skinPairs;

    private final SkinStorage skinStorage;

    {
        skinPairs = CacheBuilder.newBuilder()
                .concurrencyLevel(2)
                .maximumSize(500)
                .build();
    }

    /**
     * New {@link SkinResolver} that uses {@code storage} as an
     * external cache for skins
     *
     * @param storage storage for {@link SkinPair}s by UUID
     */
    public SkinResolver(SkinStorage storage) {
        this.skinStorage = storage;
    }

    /**
     * Get a skin from its UUID
     * <p>
     * This will only query the remote cache and not
     * Mojang servers, so it is possible this uuid is valid,
     * but the player's skin is not stored.
     *
     * @param uuid uuid of player's skin to retrieve if retrieving player skin
     *             or uuid of the skin itself
     * @return future containing optional containing uuid's {@link SkinPair}
     */
    public CompletableFuture<Optional<SkinPair>> resolveSkin(UUID uuid) {
        var pair = skinPairs.getIfPresent(uuid);

        if (pair != null)
            return CompletableFuture.completedFuture(Optional.of(pair));

        var future = skinStorage.getSkin(uuid);
        future.thenAccept(pairOpt -> pairOpt.ifPresent(p -> skinPairs.put(uuid, p)));

        return future;
    }

    public interface SkinStorage {

        /**
         * Get a stored {@link SkinPair} if it exists in this
         * storage by a UUID
         *
         * @param uuid uuid of player's skin to retrieve if retrieving player skin
         *             or uuid of the skin itself
         * @return future containing optional containing player's {@link SkinPair}
         */
        CompletableFuture<Optional<SkinPair>> getSkin(UUID uuid);

        /**
         * Put a {@link SkinPair} into storage by a UUID
         *
         * @param uuid     uuid of player's skin to store if store player skin
         *                 or uuid of the skin itself
         * @param skinPair skin pair to store
         * @return future that completes once this method stores the {@link SkinPair}
         */
        CompletableFuture<Void> storeSkin(UUID uuid, SkinPair skinPair);

    }

    public static class RedisSkinStorage implements SkinStorage {

        private static final String SKIN_KEY = "skin";
        private static final String SIGNATURE_KEY = "signature";

        private final String keyspace;
        private final RedisAsyncCommands<String, String> redisCommands;

        public RedisSkinStorage(String keyspace, RedisAsyncCommands<String, String> redisCommands) {
            this.keyspace = keyspace;
            this.redisCommands = redisCommands;
        }

        @Override
        public CompletableFuture<Optional<SkinPair>> getSkin(UUID uuid) {
            return redisCommands.hgetall(getKey(uuid))
                    .<Optional<SkinPair>>thenApply(map -> {
                        var pair = new SkinPair(map.get(SKIN_KEY), map.get(SIGNATURE_KEY));

                        if (pair.skin() == null || pair.signature() == null)
                            return Optional.empty();

                        return Optional.of(pair);
                    })
                    .toCompletableFuture();
        }

        @Override
        public CompletableFuture<Void> storeSkin(UUID uuid, SkinPair skinPair) {
            var map = new HashMap<String, String>(3);

            map.put(SKIN_KEY, skinPair.skin());
            map.put(SIGNATURE_KEY, skinPair.signature());

            return redisCommands.hset(getKey(uuid), map)
                    .toCompletableFuture().thenApply(v -> null);
        }

        private String getKey(UUID playerUuid) {
            return keyspace + ":skin:" + playerUuid;
        }
    }

}
