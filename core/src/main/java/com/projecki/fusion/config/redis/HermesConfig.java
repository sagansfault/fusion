package com.projecki.fusion.config.redis;

import com.google.gson.JsonParseException;
import com.projecki.fusion.config.Config;
import com.projecki.fusion.config.serialize.Serializer;
import com.projecki.fusion.message.MessageClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Represents a config managed and updated from Hermes. This config system is one-way, sending configs does nothing.
 *
 * @param <T> The object type to deserialize this config to
 */
public class HermesConfig<T> implements Config<T> {

    private final Serializer<T> serializer;

    private final MessageClient messageClient;
    private final RedisAsyncCommands<String, String> commands;

    private final String configKey;
    private final String configChannel;

    private Consumer<Optional<T>> onUpdate;

    private int loadAttempts = 0;

    public HermesConfig(Serializer<T> deserializer,
                        String organization,
                        String group,
                        String config,
                        MessageClient messageClient,
                        RedisAsyncCommands<String, String> commands) {
        this.serializer = deserializer;

        this.configKey = organization + ":" + group + ":" + config + ":config:key";
        this.configChannel = organization + ":" + group + ":" + config + "-config-channel";
        onUpdate = t -> {
        };

        this.commands = commands;
        this.messageClient = messageClient;
        this.messageClient.subscribe(configChannel);
        messageClient.registerMessageListener(HermesConfig.UpdateMessage.class, (s, u) -> {
            // ignore messages coming from other subscribed channels
            if (!s.equalsIgnoreCase(this.configChannel)) {
                return;
            }
            if (u.getSerializedConfig() != null) {
                try {
                    T casted = u.deserialize(deserializer).orElse(null);
                    this.onUpdate.accept(Optional.ofNullable(casted));
                } catch (JsonParseException | YAMLException e) {
                    System.out.println("Could not deserialize incoming message config:");
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * @deprecated {@link HermesConfig} does not implement this
     */
    @Deprecated
    @Override
    public CompletableFuture<Void> storeConfig(T object) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Optional<T>> loadConfig() {
        loadAttempts++;
        return commands.get(configKey).thenApplyAsync(s -> {
                    if (s == null) {
                        return Optional.<T>empty();
                    } else {
                        return this.serializer.deserialize(s);
                    }
                })
                .toCompletableFuture()
                .orTimeout(500, TimeUnit.MILLISECONDS)
                .exceptionallyCompose(ex -> {
                    if ((ex instanceof TimeoutException || ex.getCause() instanceof TimeoutException) && loadAttempts < 100) {
                        return loadConfig();
                    } else {
                        ex.printStackTrace();
                        return CompletableFuture.completedFuture(Optional.empty());
                    }
                });
    }

    /**
     * Defines the function to run when an update/load is received for this config
     *
     * @param onUpdate The function to run when this config is updated/loaded.
     */
    public void onUpdate(Consumer<Optional<T>> onUpdate) {
        this.onUpdate = onUpdate;
    }

    public static final class UpdateMessage implements MessageClient.Message {

        private String serializedConfig;

        public UpdateMessage() {
        }

        public UpdateMessage(String serializedConfig) {
            this.serializedConfig = serializedConfig;
        }

        public String getSerializedConfig() {
            return serializedConfig;
        }

        public <T> Optional<T> deserialize(Serializer<T> serializer) {
            return serializer.deserialize(this.serializedConfig);
        }
    }
}
