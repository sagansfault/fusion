package com.projecki.fusion.chat.chattype.preprocessor.impl;

import com.projecki.fusion.chat.channel.ChatChannel;
import com.projecki.fusion.chat.chattype.preprocessor.PreProcessorChatType;
import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.redis.pubsub.message.impl.chat.ChatTypeMessage;
import com.projecki.fusion.redis.pubsub.message.impl.chat.ServerToChannelChatTypeMessage;

public class ServerToChannelPreChatType extends PreProcessorChatType {

    private ChatChannel targetChannel;

    public ServerToChannelPreChatType(Message message, ChatChannel targetChannel) {
        super(message);
        this.targetChannel = targetChannel;
    }

    public ChatChannel getTargetChannel() {
        return targetChannel;
    }

    public void setTargetChannel(ChatChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

    @Override
    public ChatTypeMessage mapToChatTypeMessage() {
        return new ServerToChannelChatTypeMessage(super.getMessage(), super.getAudience(), targetChannel.getId());
    }
}
