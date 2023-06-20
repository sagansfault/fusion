package com.projecki.fusion.chat.gradient;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.chat.pipeline.postprocessor.impl.GradientNameModule;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RedisGradientStorage implements GradientStorage {

    private static final String GRADIENTS_KEY = "fusion:chat:gradients";

    @Override
    public CompletableFuture<Void> saveGradients(Map<UUID, GradientNameModule.Gradient> gradients) {
        if (gradients.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return FusionPaper.getRedisCommands().hset(
                GRADIENTS_KEY,
                gradients.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> entry.getValue().id()))
        ).thenAccept(k -> {}).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Optional<String>> loadGradient(UUID uuid) {
        return FusionPaper.getRedisCommands().hget(GRADIENTS_KEY, uuid.toString())
                .thenApply(Optional::ofNullable).toCompletableFuture();
    }
}
