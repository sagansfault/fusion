package com.projecki.fusion.util.teleport;

import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * Used to send players to other servers on the network
 */
public interface CrossServerTransport {

    /**
     * Send the player to another server on the same proxy.
     *
     * @param player player to send to another server
     * @param serverName name of server to send player to
     * @return future that completes when the request is sent to the proxy
     */
    CompletableFuture<Void> transport(Player player, String serverName);

}
