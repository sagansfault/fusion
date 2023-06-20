package com.projecki.fusion.chat.chattype;

import com.projecki.fusion.chat.message.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatType {

    private Message message;
    private final Set<UUID> audience;

    public ChatType(Message message) {
        this.message = message;
        audience = new HashSet<>();
    }

    public final Message getMessage() {
        return message;
    }

    public final void setMessage(Message message) {
        this.message = message;
    }

    public final Set<UUID> getAudience() {
        return audience;
    }
}
