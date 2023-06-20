package com.projecki.fusion.redis.pubsub.message.impl.chat;

import com.projecki.fusion.message.MessageClient;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class MessagePlayerCrossServer implements MessageClient.Message {

    private UUID targetUUID;
    private Component message;

    public MessagePlayerCrossServer() {}

    public MessagePlayerCrossServer(UUID targetUUID, Component message) {
        this.targetUUID = targetUUID;
        this.message = message;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public Component getMessage() {
        return message;
    }
}
