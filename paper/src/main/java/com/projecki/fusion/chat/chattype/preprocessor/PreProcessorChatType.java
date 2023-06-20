package com.projecki.fusion.chat.chattype.preprocessor;

import com.projecki.fusion.chat.chattype.ChatType;
import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.redis.pubsub.message.impl.chat.ChatTypeMessage;

public abstract class PreProcessorChatType extends ChatType {

    public PreProcessorChatType(Message message) {
        super(message);
    }

    public abstract ChatTypeMessage mapToChatTypeMessage();
}
