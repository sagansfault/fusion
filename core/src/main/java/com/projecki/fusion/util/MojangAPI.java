package com.projecki.fusion.util;

import com.google.gson.JsonSyntaxException;
import com.projecki.fusion.FusionCore;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MojangAPI {

    // http client used for requests
    private final HttpClient httpClient;

    /**
     * Create a new {@link MojangAPI} using the specified {@link Executor} to execute async http requests.
     *
     * @param executor the executor to execute the http requests on
     */
    public MojangAPI(@NotNull Executor executor) {
        this.httpClient = HttpClient.newBuilder()
                .executor(executor)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Get the {@link UUID} of a player by username.
     *
     * @param username the username of the player
     *
     * @return future containing optional containing player uuid
     */
    public CompletableFuture<Optional<UUID>> getUniqueId(@NotNull String username) {
        return getMojangAPIProfile("https://api.mojang.com/users/profiles/minecraft/" + username)
                .thenApply(opt -> opt.flatMap(MojangAPIProfile::uniqueId));
    }

    /**
     * Get the username of a player by {@link UUID}.
     *
     * @param uniqueId the uuid of the player
     *
     * @return future containing optional containing player username
     */
    public CompletableFuture<Optional<String>> getUsername (@NotNull UUID uniqueId) {
        return getMojangAPIProfile("https://api.mojang.com/user/profile/" + uniqueId)
                .thenApply(opt -> opt.map(MojangAPIProfile::name));
    }

    /**
     * Perform a get request to the specified api endpoint and map the resulting body to a {@link MojangAPIProfile}.
     *
     * @param endpoint the endpoint to send a get request to, this url should contain all the required parameters.
     *
     * @return future containing optional containing {@link MojangAPIProfile}
     */
    private CompletableFuture<Optional<MojangAPIProfile>> getMojangAPIProfile(@NotNull String endpoint) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseBody)
                .thenApply(Optional::ofNullable);
    }

    /**
     * Parse a json string body to a {@link MojangAPIProfile}.
     *
     * @param body the json body
     *
     * @return a {@link MojangAPIProfile} or {@code null} if it couldn't be parsed
     */
    private MojangAPIProfile parseBody (@NotNull String body) {
        try {
            return FusionCore.GSON.fromJson(body, MojangAPIProfile.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    /**
     * Record that models the profile object returned by the mojang api
     */
    private static record MojangAPIProfile (@NotNull String name, @NotNull String id) {

        public Optional<UUID> uniqueId() {
            return UUIDUtil.createUUID(id);
        }

    };
}
