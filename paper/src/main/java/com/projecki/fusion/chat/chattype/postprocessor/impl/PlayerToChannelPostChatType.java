package com.projecki.fusion.chat.chattype.postprocessor.impl;

import com.projecki.fusion.chat.channel.ChatChannel;
import com.projecki.fusion.chat.chattype.postprocessor.PostProcessorChatType;
import com.projecki.fusion.chat.message.Message;

import java.util.UUID;

public class PlayerToChannelPostChatType extends PostProcessorChatType {

    private ChatChannel targetChannel;
    private String originServer;
    private UUID senderUUID;
    private String senderName;

    public PlayerToChannelPostChatType(Message message, ChatChannel targetChannel, String originServer, UUID senderUUID, String senderName) {
        super(message);
        this.targetChannel = targetChannel;
        this.originServer = originServer;
        this.senderUUID = senderUUID;
        this.senderName = senderName;
    }

    public ChatChannel getTargetChannel() {
        return targetChannel;
    }

    public void setTargetChannel(ChatChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

    public String getOriginServer() {
        return originServer;
    }

    public void setOriginServer(String originServer) {
        this.originServer = originServer;
    }

    public UUID getSenderUUID() {
        return senderUUID;
    }

    public void setSenderUUID(UUID senderUUID) {
        this.senderUUID = senderUUID;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
