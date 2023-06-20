package com.projecki.fusion.redis.pubsub.message.impl;

import com.projecki.fusion.message.MessageClient;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@Deprecated
public class ProCommonMessage {

    @Deprecated
    public static class MessagePlayerCrossServer implements MessageClient.Message {

        private UUID targetUUID;
        private Component message;

        public MessagePlayerCrossServer() {}

        public MessagePlayerCrossServer(UUID targetUUID, Component message) {
            this.targetUUID = targetUUID;
            this.message = message;
        }

        public UUID getTargetUUID() {
            return targetUUID;
        }

        public Component getMessage() {
            return message;
        }
    }

    @Deprecated
    public static class BroadcastMessageCrossServer implements MessageClient.Message {

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
}
