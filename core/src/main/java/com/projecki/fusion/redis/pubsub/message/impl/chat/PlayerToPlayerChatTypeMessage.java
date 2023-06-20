package com.projecki.fusion.redis.pubsub.message.impl.chat;

import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.message.MessageClient;

import java.util.Set;
import java.util.UUID;

public class PlayerToPlayerChatTypeMessage extends ChatTypeMessage {

    private UUID senderUUID;
    private UUID targetUUID;
    private String senderName;
    private String targetName;

    public PlayerToPlayerChatTypeMessage(Message message, Set<UUID> audience, UUID senderUUID, UUID targetUUID, String senderName, String targetName) {
        super(message, audience);
        this.senderUUID = senderUUID;
        this.targetUUID = targetUUID;
        this.senderName = senderName;
        this.targetName = targetName;
    }

    public PlayerToPlayerChatTypeMessage(Message message, UUID senderUUID, UUID targetUUID, String senderName, String targetName) {
        super(message);
        this.senderUUID = senderUUID;
        this.targetUUID = targetUUID;
        this.senderName = senderName;
        this.targetName = targetName;
    }

    public UUID getSenderUUID() {
        return senderUUID;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getTargetName() {
        return targetName;
    }
}
