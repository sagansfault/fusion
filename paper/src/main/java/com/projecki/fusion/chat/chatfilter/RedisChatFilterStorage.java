package com.projecki.fusion.chat.chatfilter;

import com.projecki.fusion.FusionPaper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RedisChatFilterStorage implements ChatFilterStorage {

    private static final String BLOCKED_MAP = "fusion:chat:filter:blocked";

    @Override
    public CompletableFuture<Map<String, String>> loadBlockedChatFilter() {
        return FusionPaper.getRedisCommands().hgetall(BLOCKED_MAP).thenApply(map -> Optional.ofNullable(map).orElse(new HashMap<>())).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> saveBlockedChatFilter(Map<String, String> idFilterMap) {
        if (idFilterMap.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return FusionPaper.getRedisCommands().hset(BLOCKED_MAP, idFilterMap).thenAccept(l -> {}).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> saveBlockedChatFilter(String id, String block) {
        return FusionPaper.getRedisCommands().hset(BLOCKED_MAP, id, block).thenAccept(l -> {}).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Boolean> removeBlockedChatFilter(String id) {
        return FusionPaper.getRedisCommands().hdel(BLOCKED_MAP, id).thenApply(l -> l != 0).toCompletableFuture();
    }
}
