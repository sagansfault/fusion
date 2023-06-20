package com.projecki.fusion.network.message;

import com.projecki.fusion.message.MessageClient;

import java.util.UUID;

/**
 * Message that is sent on {@code CommonRedisChannels.PLAYER_NETWORK_EVENT_CHANNEL}
 * whenever a player joins the network.
 */
public class PlayerJoinedNetworkMessage implements MessageClient.Message {

    private UUID playerJoining;

    public PlayerJoinedNetworkMessage() {
    }

    public PlayerJoinedNetworkMessage(UUID playerJoining) {
        this.playerJoining = playerJoining;
    }

    /**
     * Get the UUID of the player joining the network.
     */
    public UUID getPlayerJoining() {
        return playerJoining;
    }

}
