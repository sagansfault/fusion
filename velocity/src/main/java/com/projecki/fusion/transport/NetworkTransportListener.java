package com.projecki.fusion.transport;

import com.projecki.fusion.message.MessageClient;
import com.projecki.fusion.redis.CommonRedisChannels;
import com.projecki.fusion.redis.pubsub.message.impl.network.PlayerServerSendMessage;
import com.velocitypowered.api.proxy.ProxyServer;

public class NetworkTransportListener {

    private final ProxyServer proxyServer;

    public NetworkTransportListener(MessageClient messageClient, ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
        messageClient.subscribe(CommonRedisChannels.PLAYER_SEND_CHANNEL.getChannel());
        messageClient.registerMessageListener(this);
    }

    @MessageClient.MessageListener
    public void onMessage(String channel, PlayerServerSendMessage message) {
        var player = proxyServer.getPlayer(message.getPlayerUuid());
        var server = proxyServer.getServer(message.getDestinationServer());

        if (player.isPresent() && server.isPresent()) {
            player.get().createConnectionRequest(server.get()).fireAndForget();
        }
    }
}
