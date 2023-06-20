package com.projecki.fusion.chat.chattype.preprocessor.impl;

import com.projecki.fusion.chat.chattype.preprocessor.PreProcessorChatType;
import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.redis.pubsub.message.impl.chat.ChatTypeMessage;
import com.projecki.fusion.redis.pubsub.message.impl.chat.PlayerToPlayerChatTypeMessage;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerToPlayerPreChatType extends PreProcessorChatType {

    private Player sender;
    private UUID targetUUID;
    private String targetName;

    public PlayerToPlayerPreChatType(Message message, Player sender, UUID targetUUID, String targetName) {
        super(message);
        this.sender = sender;
        this.targetUUID = targetUUID;
        this.targetName = targetName;
    }

    public Player getSender() {
        return sender;
    }

    public void setSender(Player sender) {
        this.sender = sender;
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
        return new PlayerToPlayerChatTypeMessage(super.getMessage(), super.getAudience(), sender.getUniqueId(),
                targetUUID, sender.getName(), targetName);
    }
}
