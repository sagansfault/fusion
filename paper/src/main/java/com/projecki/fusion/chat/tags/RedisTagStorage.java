package com.projecki.fusion.chat.tags;

import com.projecki.fusion.FusionPaper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RedisTagStorage implements TagStorage {

    @Override
    public CompletableFuture<Map<UUID, String>> getTags() {
        return FusionPaper.getRedisCommands().hgetall("fusion:chat:tags").thenApply(map -> {
            Map<UUID, String> tagsMap = new HashMap<>();
            if (map != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    try {
                        UUID uuid = UUID.fromString(entry.getKey());
                        tagsMap.put(uuid, entry.getValue());
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            return tagsMap;
        }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Optional<String>> getTag(UUID key) {
        return FusionPaper.getRedisCommands().hget("fusion:chat:tags", key.toString())
                .thenApply(Optional::ofNullable).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> saveTags(Map<UUID, String> tags) {
        if (tags.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return FusionPaper.getRedisCommands().hset(
                "fusion:chat:tags",
                tags.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toString(), Map.Entry::getValue))
        ).toCompletableFuture().thenAccept(l -> {});
    }

    @Override
    public CompletableFuture<Void> saveTag(UUID key, String tagId) {
        return FusionPaper.getRedisCommands().hset("fusion:chat:tags", key.toString(), tagId)
                .thenAccept(l -> {}).toCompletableFuture();
    }
}
