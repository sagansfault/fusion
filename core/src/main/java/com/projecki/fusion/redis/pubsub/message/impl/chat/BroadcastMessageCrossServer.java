package com.projecki.fusion.redis.pubsub.message.impl.chat;

import com.projecki.fusion.message.MessageClient;
import net.kyori.adventure.text.Component;

public class BroadcastMessageCrossServer implements MessageClient.Message {

    private String[] servers;
    private Component message;

    public BroadcastMessageCrossServer() {}

    public BroadcastMessageCrossServer(Component message, String... servers) {
        this.servers = servers;
        this.message = message;
    }

    public String[] getServers() {
        return servers;
    }

    public Component getMessage() {
        return message;
    }
}
