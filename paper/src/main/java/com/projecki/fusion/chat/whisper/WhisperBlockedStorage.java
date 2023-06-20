package com.projecki.fusion.chat.whisper;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface WhisperBlockedStorage {

    CompletableFuture<Map<UUID, Set<UUID>>> loadBlockedWhisperers();

    CompletableFuture<Void> saveBlockedWhisperers(Map<UUID, Set<UUID>> blockedData);
}
