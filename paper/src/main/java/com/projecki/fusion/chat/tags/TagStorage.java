package com.projecki.fusion.chat.tags;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface TagStorage {

    CompletableFuture<Map<UUID, String>> getTags();

    CompletableFuture<Optional<String>> getTag(UUID key);

    CompletableFuture<Void> saveTags(Map<UUID, String> tags);

    CompletableFuture<Void> saveTag(UUID key, String tagId);
}
