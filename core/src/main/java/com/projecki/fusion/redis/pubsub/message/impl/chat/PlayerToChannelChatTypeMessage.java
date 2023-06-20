package com.projecki.fusion.redis.pubsub.message.impl.chat;

import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.message.MessageClient;

import java.util.Set;
import java.util.UUID;

public class PlayerToChannelChatTypeMessage extends ChatTypeMessage {

    private String originServer;
    private String channelId;
    private UUID senderUUID;
    private String senderName;

    public PlayerToChannelChatTypeMessage(Message message, Set<UUID> audience, String originServer, String channelId, UUID senderUUID, String senderName) {
        super(message, audience);
        this.originServer = originServer;
        this.channelId = channelId;
        this.senderUUID = senderUUID;
        this.senderName = senderName;
    }

    public PlayerToChannelChatTypeMessage(Message message, String originServer, String channelId, UUID senderUUID, String senderName) {
        super(message);
        this.originServer = originServer;
        this.channelId = channelId;
        this.senderUUID = senderUUID;
        this.senderName = senderName;
    }

    public String getOriginServer() {
        return originServer;
    }

    public String getChannelId() {
        return channelId;
    }

    public UUID getSenderUUID() {
        return senderUUID;
    }

    public String getSenderName() {
        return senderName;
    }
}
