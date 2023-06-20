package com.projecki.fusion.chat.chattype.preprocessor.impl;

import com.projecki.fusion.chat.chattype.preprocessor.PreProcessorChatType;
import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.redis.pubsub.message.impl.chat.ChatTypeMessage;
import com.projecki.fusion.redis.pubsub.message.impl.chat.NetworkBroadcastChatTypeMessage;

public class NetworkBroadcastPreChatType extends PreProcessorChatType {

    private String[] excludedServers;

    public NetworkBroadcastPreChatType(Message message, String[] excludedServers) {
        super(message);
        this.excludedServers = excludedServers;
    }

    public String[] getExcludedServers() {
        return excludedServers;
    }

    public void setExcludedServers(String[] excludedServers) {
        this.excludedServers = excludedServers;
    }

    @Override
    public ChatTypeMessage mapToChatTypeMessage() {
        return new NetworkBroadcastChatTypeMessage(super.getMessage(), super.getAudience(), excludedServers);
    }
}
