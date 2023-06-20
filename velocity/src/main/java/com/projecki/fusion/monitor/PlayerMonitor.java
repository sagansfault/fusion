package com.projecki.fusion.monitor;

import com.projecki.fusion.network.PlayerStorage;
import com.velocitypowered.api.proxy.ProxyServer;

import java.time.Duration;
import java.util.UUID;

public class PlayerMonitor {

    private final PlayerStorage storage;

    public PlayerMonitor(Object plugin, ProxyServer server, PlayerStorage storage) {
        this.storage = storage;

        server.getScheduler()
                .buildTask(plugin, this::cleanPlayers)
                .repeat(Duration.ofMinutes(1))
                .schedule();
    }

    /**
     * Removes players from the player storage that are no
     * longer online.
     */
    private void cleanPlayers() {
        storage.getOnlinePlayers()
                .thenAccept(players -> players.forEach(this::checkPlayer));
    }

    /**
     * Check if a player needs to be marked as offline
     */
    private void checkPlayer(UUID playerUuid) {
        storage.getLastHeartbeat(playerUuid)
                .thenAccept(heartbeat -> {
                    if (heartbeat.isEmpty() || heartbeat.get() + (1000 * 30) < System.currentTimeMillis()) {
                        storage.playerLeft(playerUuid);
                    }
                });
    }

}
