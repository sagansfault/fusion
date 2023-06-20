package com.projecki.fusion.redis;

public enum CommonRedisChannels {

    SERVER_LOOKUP_CHANNEL("procommon-serverlookup"),
    PLAYER_SEND_CHANNEL("network-send"),
    PLAYER_NETWORK_EVENT_CHANNEL("player-network-event");

    private final String channel;

    CommonRedisChannels(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }
}
