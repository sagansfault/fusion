package com.projecki.fusion.message.redis;

import com.projecki.fusion.FusionCore;
import com.projecki.fusion.message.MessageClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

public class RedisMessageClient extends MessageClient {

    private final RedisAsyncCommands<String, String> commands;
    private final RedisPubSubAsyncCommands<String, String> pubsubConn;

    public RedisMessageClient(RedisAsyncCommands<String, String> commands, RedisPubSubAsyncCommands<String, String> pubsubConn) {
        this.commands = commands;
        this.pubsubConn = pubsubConn;
        this.subscribe(DEFAULT_CHANNEL);
        pubsubConn.getStatefulConnection().addListener(new RedisMessageIntakeDelegator(this));
    }

    /**
     * {@inheritDoc}
     *
     * @param channel The channel to send the message on
     * @param message The message to send
     */
    @Override
    public void send(String channel, Message message) {

        String id = message.getIdentifier();
        String json = FusionCore.GSON.toJson(message);
        String toSend = id + ":" + json;

        this.commands.publish(channel, toSend);
    }

    @Override
    public void subscribe(String channel) {
        this.pubsubConn.subscribe(channel);
    }

    @Override
    public void unsubscribe(String channel) {
        pubsubConn.unsubscribe(channel);
    }
}
