package com.projecki.fusion.redis.pubsub.message.impl;

import com.projecki.fusion.message.MessageClient;

import java.util.UUID;

@Deprecated
public class NetworkMessages {

    @Deprecated
    public static class ServerRegistryUpdateMessage implements MessageClient.Message {}

    @Deprecated
    public static class PlayerServerSendMessage implements MessageClient.Message {

        private UUID playerUuid;
        private String destinationServer;

        public PlayerServerSendMessage() {
        }

        public PlayerServerSendMessage(UUID playerUuid, String destinationServer) {
            this.playerUuid = playerUuid;
            this.destinationServer = destinationServer;
        }

        public UUID getPlayerUuid() {
            return playerUuid;
        }

        public String getDestinationServer() {
            return destinationServer;
        }
    }

}
