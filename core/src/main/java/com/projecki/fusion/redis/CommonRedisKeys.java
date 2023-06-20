package com.projecki.fusion.redis;

/**
 * A set of common redis keys used amongst depending plugins and cross platform projects.
 */
public enum CommonRedisKeys {

    // a great example of the type of keys that should be in here
    SERVER_LOOKUP("procommon:serverlookup"),
    PLAYER_CACHE("player-cache"),
    SERVER_CHAT_AND_API("fusion-server-chat-and-api"),
    ;

    private final String key;

    CommonRedisKeys(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
