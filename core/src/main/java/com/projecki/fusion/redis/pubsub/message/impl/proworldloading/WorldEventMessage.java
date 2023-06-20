package com.projecki.fusion.redis.pubsub.message.impl.proworldloading;

import com.projecki.fusion.message.MessageClient;

public class WorldEventMessage implements MessageClient.Message {

    private String worldName;
    private String serverName;
    private EventType type;

    public WorldEventMessage() {
    }

    public WorldEventMessage(String worldName, String serverName, EventType type) {
        this.worldName = worldName;
        this.serverName = serverName;
        this.type = type;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public enum EventType {LOADED, UNLOADED}
}
