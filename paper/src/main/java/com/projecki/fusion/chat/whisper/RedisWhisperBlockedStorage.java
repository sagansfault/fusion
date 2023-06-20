package com.projecki.fusion.chat.whisper;

import com.google.gson.JsonSyntaxException;
import com.projecki.fusion.FusionCore;
import com.projecki.fusion.FusionPaper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RedisWhisperBlockedStorage implements WhisperBlockedStorage {

    private static final String WHISPER_BLOCKED_KEY = "fusion:chat:whisper:blocked";

    @Override
    public CompletableFuture<Map<UUID, Set<UUID>>> loadBlockedWhisperers() {
        return FusionPaper.getRedisCommands().hgetall(WHISPER_BLOCKED_KEY).thenApply(map -> {
            Map<UUID, Set<UUID>> blockedData = new HashMap<>();
            if (map != null) {
                map.forEach((s, s2) -> {
                    UUID player;
                    String[] casted;
                    try {
                        player = UUID.fromString(s);
                        casted = FusionCore.GSON.fromJson(s2, String[].class);
                    } catch (IllegalStateException | JsonSyntaxException ex) {
                        return;
                    }

                    Set<UUID> uuids = Arrays.stream(casted).flatMap(string -> {
                        Optional<UUID> potentialUUID;
                        try {
                            potentialUUID = Optional.of(UUID.fromString(string));
                        } catch (IllegalStateException ex) {
                            potentialUUID = Optional.empty();
                        }
                        return potentialUUID.stream();
                    }).collect(Collectors.toSet());
                    blockedData.put(player, uuids);
                });
            }
            return blockedData;
        }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> saveBlockedWhisperers(Map<UUID, Set<UUID>> blockedData) {
        if (blockedData.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        Map<String, String> castedBlockedData = new HashMap<>();
        blockedData.forEach((uuid, set) -> {
            Set<String> uuidStrings = set.stream().map(UUID::toString).collect(Collectors.toSet());
            String singleString = FusionCore.GSON.toJson(uuidStrings.toArray(String[]::new));
            castedBlockedData.put(uuid.toString(), singleString);
        });
        return FusionPaper.getRedisCommands().hset(WHISPER_BLOCKED_KEY, castedBlockedData).thenAccept(l -> {}).toCompletableFuture();
    }
}
