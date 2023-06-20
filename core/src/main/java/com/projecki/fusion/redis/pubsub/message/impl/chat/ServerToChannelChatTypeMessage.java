package com.projecki.fusion.redis.pubsub.message.impl.chat;

import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.message.MessageClient;

import java.util.Set;
import java.util.UUID;

public class ServerToChannelChatTypeMessage extends ChatTypeMessage {

    private String targetChannel;

    public ServerToChannelChatTypeMessage(Message message, Set<UUID> audience, String targetChannel) {
        super(message, audience);
        this.targetChannel = targetChannel;
    }

    public ServerToChannelChatTypeMessage(Message message, String targetChannel) {
        super(message);
        this.targetChannel = targetChannel;
    }

    public String getTargetChannel() {
        return targetChannel;
    }
}
