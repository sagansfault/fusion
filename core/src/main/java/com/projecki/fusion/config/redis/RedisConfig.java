package com.projecki.fusion.config.redis;

import com.google.gson.JsonParseException;
import com.projecki.fusion.config.Config;
import com.projecki.fusion.config.serialize.Serializer;
import com.projecki.fusion.message.MessageClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Deprecated in favor of {@link HermesConfig}. This has almost no use outside the realm of hermes' distribution.
 */
@Deprecated
public class RedisConfig<T> implements Config<T> {

    private final Serializer<T> serializer;

    private final MessageClient messageClient;
    private final RedisAsyncCommands<String, String> commands;

    private final String configKey;
    private final String configChannel;

    private Consumer<Optional<T>> onUpdate;

    public RedisConfig(Serializer<T> deserializer, String uniqueName, MessageClient messageClient, RedisAsyncCommands<String, String> commands) {
        this.serializer = deserializer;

        this.configKey = uniqueName + ":config:key";
        this.configChannel = uniqueName + "-config-channel";
        onUpdate = t -> {};

        this.commands = commands;
        this.messageClient = messageClient;
        this.messageClient.subscribe(configChannel);
        messageClient.registerMessageListener(UpdateMessage.class, (s, u) -> {
            // ignore messages coming from other subscribed channels
            if (!s.equalsIgnoreCase(this.configChannel)) {
                return;
            }
            if (u.getSerializedConfig() != null) {
                T casted = null;
                try {
                    casted = u.deserialize(deserializer).orElse(null);
                } catch (JsonParseException | YAMLException e) {
                    System.out.println("Could not deserialize incoming message config:");
                    e.printStackTrace();
                }

                this.onUpdate.accept(Optional.ofNullable(casted));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> storeConfig(T object) {
        String serialized = this.serializer.serialize(object);
        return commands.set(this.configKey, serialized)
                .thenRun(() -> messageClient.send(configChannel, new UpdateMessage(serialized)))
                .toCompletableFuture();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Optional<T>> loadConfig() {
        return commands.get(configKey).thenApply(s -> {
            if (s == null) {
                return Optional.<T>empty();
            } else {
                return this.serializer.deserialize(s);
            }
        }).toCompletableFuture();
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

        public UpdateMessage() {}

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