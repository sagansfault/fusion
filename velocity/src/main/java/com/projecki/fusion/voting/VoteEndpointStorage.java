package com.projecki.fusion.voting;

import com.projecki.fusion.util.NetworkingUtil;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.logging.Logger;

/**
 * Stores the IP of this proxy in redis so that
 * the vote-forwarder can forward votes to this proxy
 */
public class VoteEndpointStorage {

    private final RedisAsyncCommands<String, String> redis;
    private final Logger logger;
    private final String proxyId;
    private final int port;

    public VoteEndpointStorage(RedisAsyncCommands<String, String> redis, Logger logger, String proxyId, int port) {
        this.redis = redis;
        this.logger = logger;
        this.proxyId = proxyId;
        this.port = port;

        store();
    }

    private void store() {
        NetworkingUtil.getIp()
                .thenCompose(ip -> redis.set(getKey(), ip + ':' + port))
                .exceptionally(e -> {
                    logger.warning("Unable to store proxy's ip in redis");
                    e.printStackTrace();

                    return null;
                });
    }

    public void shutdown() {
        redis.del(getKey());
    }

    private String getKey() {
        return "instance:" + proxyId + ":endpoint";
    }
}
