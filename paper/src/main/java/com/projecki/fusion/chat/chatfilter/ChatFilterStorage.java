package com.projecki.fusion.chat.chatfilter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ChatFilterStorage {

    CompletableFuture<Map<String, String>> loadBlockedChatFilter();

    CompletableFuture<Void> saveBlockedChatFilter(Map<String, String> idFilterMap);

    CompletableFuture<Void> saveBlockedChatFilter(String id, String block);

    CompletableFuture<Boolean> removeBlockedChatFilter(String id);
}
