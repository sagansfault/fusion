package com.projecki.fusion.chat.chattype.postprocessor.impl;

import com.projecki.fusion.chat.chattype.postprocessor.PostProcessorChatType;
import com.projecki.fusion.chat.message.Message;

import java.util.UUID;

public class ServerToPlayerPostChatType extends PostProcessorChatType {

    private UUID target;

    public ServerToPlayerPostChatType(Message message, UUID target) {
        super(message);
        this.target = target;
    }

    public UUID getTarget() {
        return target;
    }

    public void setTarget(UUID target) {
        this.target = target;
    }
}
