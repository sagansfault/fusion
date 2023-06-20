package com.projecki.fusion.chat.gradient;

import com.projecki.fusion.chat.pipeline.postprocessor.impl.GradientNameModule;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GradientStorage {

    CompletableFuture<Void> saveGradients(Map<UUID, GradientNameModule.Gradient> gradients);

    CompletableFuture<Optional<String>> loadGradient(UUID uuid);
}
