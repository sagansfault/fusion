package com.projecki.fusion.chat.chattype.postprocessor.impl;

import com.projecki.fusion.chat.chattype.postprocessor.PostProcessorChatType;
import com.projecki.fusion.chat.message.Message;

import java.util.UUID;

public class PlayerToPlayerPostChatType extends PostProcessorChatType {

    private UUID senderUUID;
    private UUID targetUUID;
    private String senderName;
    private String targetName;

    public PlayerToPlayerPostChatType(Message message, UUID senderUUID, UUID targetUUID, String senderName, String targetName) {
        super(message);
        this.senderUUID = senderUUID;
        this.targetUUID = targetUUID;
        this.senderName = senderName;
        this.targetName = targetName;
    }

    public UUID getSenderUUID() {
        return senderUUID;
    }

    public void setSenderUUID(UUID senderUUID) {
        this.senderUUID = senderUUID;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public void setTargetUUID(UUID targetUUID) {
        this.targetUUID = targetUUID;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
}
