package com.projecki.fusion.network.message;

import com.projecki.fusion.message.MessageClient;

import java.util.UUID;

/**
 * Message that is sent on {@code CommonRedisChannels.PLAYER_NETWORK_EVENT_CHANNEL}
 * whenever a player leaves the network.
 *
 * It is possible that this message will not be
 * sent if a player leaves the network unexpectely (ex. proxy crash).
 */
public class PlayerLeftNetworkMessage implements MessageClient.Message {

    private UUID playerLeaving;

    public PlayerLeftNetworkMessage() {
    }

    public PlayerLeftNetworkMessage(UUID playerLeaving) {
        this.playerLeaving = playerLeaving;
    }

    /**
     * Get the UUID of the player leaving the network
     */
    public UUID getPlayerLeaving() {
        return playerLeaving;
    }

}
