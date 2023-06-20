package com.projecki.fusion.redis.pubsub.message.impl.chat;

import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.message.MessageClient;

import java.util.Set;
import java.util.UUID;

public class NetworkBroadcastChatTypeMessage extends ChatTypeMessage {

    private String[] excludedServers;

    public NetworkBroadcastChatTypeMessage(Message message, Set<UUID> audience, String[] excludedServers) {
        super(message, audience);
        this.excludedServers = excludedServers;
    }

    public NetworkBroadcastChatTypeMessage(Message message, String[] excludedServers) {
        super(message);
        this.excludedServers = excludedServers;
    }

    public String[] getExcludedServers() {
        return excludedServers;
    }
}
