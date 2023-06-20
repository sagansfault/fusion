package com.projecki.fusion.listener;

import com.projecki.fusion.message.MessageClient;
import com.projecki.fusion.network.message.PlayerJoinedNetworkMessage;
import com.projecki.fusion.network.message.PlayerLeftNetworkMessage;
import com.projecki.fusion.redis.CommonRedisChannels;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;

/**
 * Broadcasts messages on the {@code CommonRedisChannels.PLAYER_NETWORK_EVENT_CHANNEL}
 * when a player joins or leaves the network.
 */
public class JoinLeaveListener {

    private final MessageClient messageClient;

    public JoinLeaveListener(MessageClient messageClient) {
        this.messageClient = messageClient;
    }

    @Subscribe
    public void onJoin(LoginEvent event) {
        messageClient.send(CommonRedisChannels.PLAYER_NETWORK_EVENT_CHANNEL.getChannel(),
                new PlayerJoinedNetworkMessage(event.getPlayer().getUniqueId()));
    }

    @Subscribe
    public void onLeave(DisconnectEvent event) {
        messageClient.send(CommonRedisChannels.PLAYER_NETWORK_EVENT_CHANNEL.getChannel(),
                new PlayerLeftNetworkMessage(event.getPlayer().getUniqueId()));
    }

}
