package com.projecki.fusion.chat.chattype.postprocessor.impl;

import com.projecki.fusion.chat.chattype.postprocessor.PostProcessorChatType;
import com.projecki.fusion.chat.message.Message;

public class NetworkBroadcastPostChatType extends PostProcessorChatType {

    private String[] excludedServers;

    public NetworkBroadcastPostChatType(Message message, String[] excludedServers) {
        super(message);
        this.excludedServers = excludedServers;
    }

    public String[] getExcludedServers() {
        return excludedServers;
    }

    public void setExcludedServers(String[] excludedServers) {
        this.excludedServers = excludedServers;
    }
}
