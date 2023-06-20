package com.projecki.fusion.redis.pubsub.message.impl.chat;

import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.message.MessageClient;

import java.util.Set;
import java.util.UUID;

public class ServerToPlayerChatTypeMessage extends ChatTypeMessage {

    private UUID target;

    public ServerToPlayerChatTypeMessage(Message message, Set<UUID> audience, UUID target) {
        super(message, audience);
        this.target = target;
    }

    public ServerToPlayerChatTypeMessage(Message message, UUID target) {
        super(message);
        this.target = target;
    }

    public UUID getTarget() {
        return target;
    }
}
