package com.projecki.fusion.util.teleport;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.redis.CommonRedisChannels;
import com.projecki.fusion.redis.pubsub.message.impl.network.PlayerServerSendMessage;

import java.util.UUID;

public final class NetworkTransport {

    private NetworkTransport() {
    }

    /**
     * Send a player to another server across the network. Can be used
     * as long as the player is online, but makes no promise that the player
     * is online.
     *
     * Future completes once the message is sent to the proxies, and does
     * not guarntee that the message was processed sucessfully.
     *
     * @param playerUuid uuid of player to send
     * @param destinationServer name of the destination server
     */
    public static void sendPlayer(UUID playerUuid, String destinationServer) {
        FusionPaper.getMessageClient().send(CommonRedisChannels.PLAYER_SEND_CHANNEL.getChannel(),
                new PlayerServerSendMessage(playerUuid, destinationServer));
    }

}
