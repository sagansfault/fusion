package com.projecki.fusion.network;

import co.aikar.commands.annotation.CommandAlias;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerStorage {

    /**
     * Store that a player has joined another server
     *
     * @param player uuid of player who is changing servers
     * @param serverName name of the player's new server
     */
    void serverChange(UUID player, String serverName);

    /**
     * Get the name of the server that the player is currently connected to
     * if they are online and connected to a server.
     *
     * @param player uuid of player
     * @return future that completes with optional of server name,
     * if the player is online and on a server.
     */
    CompletableFuture<Optional<String>> getPlayerServer(UUID player);

    /**
     * Get the name of the proxy the player is connected to if they are
     * online.
     *
     * @param player uuid of the player
     * @return future that completes with an optional of the proxy name,
     * if the player is online.
     */
    CompletableFuture<Optional<String>> getPlayerProxy(UUID player);

    /**
     * Remove the player from being actively online on the network.
     * This method also stored the player's last online time.
     *
     * @param player uuid of player who left the network
     */
    void playerLeft(UUID player);

    /**
     * Store that the player joined the network and store
     * the proxy they connected on
     *
     * @param player uuid of the player that connected to the network
     * @param proxyName name of the proxy that the player connected to
     */
    void playerJoined(UUID player, String proxyName);

    /**
     * Get the player's heartbeat. Which is a timestamp is that is often
     * updated while the player is online.
     *
     * @param player uuid of player to get heartbeat of
     * @return future that completes with an optional of
     * long of player's last heartbeat. the optional WILL be empty
     * if the player has never logged on before, and MAY be empty
     * if the player is offline.
     */
    CompletableFuture<Optional<Long>> getLastHeartbeat(UUID player);

    /**
     * Store the last time that the player was actively on the network.
     * This value is intended to be updated so often while the player is
     * online.
     *
     * @param player uuid of player to store heartbeat for
     */
    void storeLastHeartbeat(UUID player);

    /**
     * Get the last time a player was online on the network
     *
     * @param player uuid of player
     * @return future that completes with optional containing time player was last online.
     * if the player was never online, the optional is empty.
     */
    CompletableFuture<Optional<Long>> getLastOnline(UUID player);

    /**
     * Get total amount of players currently online
     *
     * @return future containing amount of online players
     */
    CompletableFuture<Long> getPlayerCount();

    /**
     * Get a list of all players that are currently marked as
     * online by their UUIDs
     *
     * @return future containing set of the uuids of all players
     * that are online
     */
    CompletableFuture<Set<UUID>> getOnlinePlayers();

}
