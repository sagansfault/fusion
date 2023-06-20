package com.projecki.fusion.listener;

import com.projecki.fusion.network.PlayerStorage;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;

/**
 * Keeps player state up to date in the database
 */
public class PlayerListener {

    private final PlayerStorage storage;
    private final String proxyId;

    public PlayerListener(PlayerStorage storage, String proxyId) {
        this.storage = storage;
        this.proxyId = proxyId;
    }

    @Subscribe
    public void onConnect(PostLoginEvent event) {
        storage.playerJoined(event.getPlayer().getUniqueId(), proxyId);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        storage.playerLeft(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onServerChange(ServerConnectedEvent event) {
        storage.serverChange(event.getPlayer().getUniqueId(), event.getServer().getServerInfo().getName());
    }

}
