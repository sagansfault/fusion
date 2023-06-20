package com.projecki.fusion.message.redis;

import com.projecki.fusion.FusionCore;
import com.projecki.fusion.message.MessageClient;
import io.lettuce.core.pubsub.RedisPubSubListener;

public class RedisMessageIntakeDelegator implements RedisPubSubListener<String, String> {

    private final MessageClient messageClient;

    public RedisMessageIntakeDelegator(MessageClient messageClient) {
        this.messageClient = messageClient;
    }

    @Override
    public void message(String channel, String message) {

        String[] parts = message.split(":", 2);
        if (parts.length != 2) {
            return;
        }

        String id = parts[0];
        String json = parts[1];

        Class<? extends MessageClient.Message> type = messageClient.getMessageTypes().get(id);
        if (type == null) {
            return;
        }

        MessageClient.Message msg = FusionCore.GSON.fromJson(json, type);
        messageClient.getListenerContainers().forEach(l -> l.handle(channel, msg));
    }

    @Override
    public void message(String pattern, String channel, String message) {

    }

    @Override
    public void subscribed(String channel, long count) {

    }

    @Override
    public void psubscribed(String pattern, long count) {

    }

    @Override
    public void unsubscribed(String channel, long count) {

    }

    @Override
    public void punsubscribed(String pattern, long count) {

    }
}
