package com.projecki.fusion.server;

import java.util.Optional;

public class BasicServerData extends ServerData {

    private static final String HEARTBEAT = "heartbeat";
    private static final String PLAYER_COUNT = "playercount";
    private static final String SERVER_GROUP = "servergroup";
    private static final String MAX_PLAYERS = "maxplayers";

    public BasicServerData(String serverName) {
        super(serverName);
    }

    public BasicServerData(String serverName, int maxPlayers) {
        super(serverName);
        putField(MAX_PLAYERS, maxPlayers);
    }

    public BasicServerData(String serverName, String serverGroup, int maxPlayers) {
        this(serverName, maxPlayers);
        putField(SERVER_GROUP, serverGroup);
    }

    public BasicServerData(String serverName, long lastHeartbeat) {
        this(serverName);
        putField(HEARTBEAT, lastHeartbeat);
    }

    public BasicServerData(String serverName, int playerCount, long lastHeartbeat) {
        this(serverName, lastHeartbeat);
        putField(PLAYER_COUNT, playerCount);
    }

    public BasicServerData(String serverName, int playerCount, int maxPlayers, long lastHeartbeat) {
        this(serverName, playerCount, lastHeartbeat);
        putField(MAX_PLAYERS, maxPlayers);
    }

    public BasicServerData(String serverName, String serverGroup, int playerCount, long lastHeartbeat) {
        this(serverName, playerCount, lastHeartbeat);
        putField(SERVER_GROUP, serverGroup);
    }

    public Optional<Integer> getPlayerCount() {
        return getField(PLAYER_COUNT, Integer.class);
    }

    public Optional<Long> getHeartbeat() {
        return getField(HEARTBEAT, Long.class);
    }

    public Optional<String> getServerGroup() {
        return getField(SERVER_GROUP, String.class);
    }

    public Optional<Integer> getMaxPlayers() {
        return getField(MAX_PLAYERS, Integer.class);
    }
}
