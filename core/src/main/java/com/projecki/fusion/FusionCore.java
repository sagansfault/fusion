package com.projecki.fusion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projecki.fusion.gson.IsoFormatInstantTypeAdapter;
import com.projecki.fusion.gson.RecordTypeAdapterFactory;
import com.projecki.fusion.party.Parties;
import com.projecki.fusion.redis.CommonRedisKeys;
import io.lettuce.core.api.async.RedisAsyncCommands;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class FusionCore {

    private static Supplier<Parties<?>> parties;
    public static final Logger LOGGER = LoggerFactory.getLogger("Fusion");

    // project wide serializer for components and other modifications
    public static final Gson GSON;

    static {

        GsonBuilder builder = GsonComponentSerializer.gson().populator()
                .apply(new GsonBuilder())
                .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
                .registerTypeAdapter(Instant.class, new IsoFormatInstantTypeAdapter());
        // Attempt to locate the PlayerProfileTypeAdapter and register it
        try {
            Class<?> profileType = Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
            Class<?> adapterType = Class.forName("com.projecki.fusion.gson.PlayerProfileTypeAdapter");
            builder.registerTypeAdapter(profileType, adapterType.getConstructor().newInstance());
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException ignored) {
        }

        GSON = builder.create();
    }

    /**
     * Returns a completable future (wrapper over a completeablefuture) containing an optional of a  String representing
     * the server name. An empty optional returned if there were no results for that ip/port
     *
     * @param redisCommands The redis connection to get this info through
     * @param ip The ip of the server you wish to get the name of
     * @param port The port of the server you wish to get the name of
     * @return A completable future containing a possibly empty optional of a string of the server name
     */
    public static CompletableFuture<Optional<String>> getServerName(RedisAsyncCommands<String, String> redisCommands, String ip, String port) {
        return redisCommands.hget(CommonRedisKeys.SERVER_LOOKUP.getKey(), ip + ":" + port)
                .thenApply(Optional::ofNullable)
                .toCompletableFuture();
    }

    /**
     * Returns a completable future (wrapper over a completeablefuture) containing the current map of ips to server names.
     * An empty map is returned if it does not exist
     *
     * @param redisCommands The redis commands to get this info through
     *
     * @return A completable future containing the full current map of server ip/ports to server names.
     */
    public static CompletableFuture<Map<String, String>> getServerIpNameMap(RedisAsyncCommands<String, String> redisCommands) {
        return redisCommands.hgetall(CommonRedisKeys.SERVER_LOOKUP.getKey()).toCompletableFuture();
    }

    /**
     * Get the current {@link Parties party manager} for this platform.
     *
     * @param <T> The type of player for this platform.
     * @return The {@link Parties}.
     */
    public static <T> Parties<T> getParties() {
        return (Parties<T>) parties;
    }

    /**
     * Set the {@link Parties party manager} for this platform.
     *
     * @param parties The {@link Parties} getter.
     */
    static void setParties(Supplier<Parties<?>> parties) {
        FusionCore.parties = parties;
    }
}
