package com.projecki.fusion.chat.chattype.postprocessor.impl;

import com.projecki.fusion.chat.channel.ChatChannel;
import com.projecki.fusion.chat.chattype.postprocessor.PostProcessorChatType;
import com.projecki.fusion.chat.message.Message;

public class ServerToChannelPostChatType extends PostProcessorChatType {

    private ChatChannel targetChannel;

    public ServerToChannelPostChatType(Message message, ChatChannel targetChannel) {
        super(message);
        this.targetChannel = targetChannel;
    }

    public ChatChannel getTargetChannel() {
        return targetChannel;
    }

    public void setTargetChannel(ChatChannel targetChannel) {
        this.targetChannel = targetChannel;
    }
}
