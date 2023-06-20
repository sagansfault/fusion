package com.projecki.fusion.redis.pubsub.message.impl.proworldloading;

import com.projecki.fusion.message.MessageClient;

public class LoadWorldMessage implements MessageClient.Message {

    private String worldName;
    private boolean createIfNonexistent;

    public LoadWorldMessage() {
    }

    public LoadWorldMessage(String worldName) {
        this.worldName = worldName;
    }

    public LoadWorldMessage(String worldName, boolean createIfNonexistent) {
        this.worldName = worldName;
        this.createIfNonexistent = createIfNonexistent;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public boolean isCreateIfNonexistent() {
        return createIfNonexistent;
    }

    public void setCreateIfNonexistent(boolean createIfNonexistent) {
        this.createIfNonexistent = createIfNonexistent;
    }
}
