package com.projecki.fusion.redis.pubsub.message.impl.chat;

import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.message.MessageClient;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class ChatTypeMessage implements MessageClient.Message {

    private Message message;
    private Set<UUID> audience;

    public ChatTypeMessage(Message message, Set<UUID> audience) {
        this.message = message;
        this.audience = audience;
    }

    public ChatTypeMessage(Message message) {
        this(message, new HashSet<>());
    }

    public ChatTypeMessage() {}

    public Message getMessage() {
        return message;
    }

    public Set<UUID> getAudience() {
        return audience;
    }
}
