package com.projecki.fusion.monitor;

import com.projecki.fusion.network.PlayerStorage;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.time.Duration;

public class PlayerHeartbeats {

    private final PlayerStorage storage;
    private final ProxyServer server;

    public PlayerHeartbeats(Object plugin, ProxyServer server, PlayerStorage storage) {
        this.storage = storage;
        this.server = server;

        server.getScheduler()
                .buildTask(plugin, this::updateHeartbeats)
                .repeat(Duration.ofSeconds(1))
                .schedule();
    }

    /**
     * Updates heartbeats of all players online on this proxy
     */
    private void updateHeartbeats() {
        server.getAllPlayers().stream()
                .map(Player::getUniqueId)
                .forEach(storage::storeLastHeartbeat);
    }

}
