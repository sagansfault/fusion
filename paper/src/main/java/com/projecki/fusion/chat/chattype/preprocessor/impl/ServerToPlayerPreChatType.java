package com.projecki.fusion.chat.chattype.preprocessor.impl;

import com.projecki.fusion.chat.chattype.preprocessor.PreProcessorChatType;
import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.redis.pubsub.message.impl.chat.ChatTypeMessage;
import com.projecki.fusion.redis.pubsub.message.impl.chat.ServerToPlayerChatTypeMessage;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ServerToPlayerPreChatType extends PreProcessorChatType {

    private UUID targetUUID;
    private String targetName;

    public ServerToPlayerPreChatType(Message message, UUID targetUUID, String targetName) {
        super(message);
        this.targetUUID = targetUUID;
        this.targetName = targetName;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public void setTargetUUID(UUID targetUUID) {
        this.targetUUID = targetUUID;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    @Override
    public ChatTypeMessage mapToChatTypeMessage() {
        return new ServerToPlayerChatTypeMessage(super.getMessage(), super.getAudience(), targetUUID);
    }
}
