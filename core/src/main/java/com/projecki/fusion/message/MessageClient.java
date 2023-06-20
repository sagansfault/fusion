package com.projecki.fusion.message;

import com.projecki.fusion.Bootstrap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A manager/client for sending and catching pubsub messages between plugins running on different servers
 */
public abstract class MessageClient {

    public static final String DEFAULT_CHANNEL = "inter-server-messages";

    private final Map<String, Class<? extends Message>> messageTypes = new HashMap<>();
    private final List<ListenerContainer<? extends Message>> listenerContainers = new ArrayList<>();

    public MessageClient() {
        Bootstrap.REFLECTIONS.getSubTypesOf(HandledMessage.class).forEach(this::registerMessage);
    }

    /**
     * Register a {@link HandledMessage} that handles its own
     * receiving of messages.
     *
     * @param target The target type of {@link HandledMessage}.
     */
    public final void registerMessage(Class<? extends HandledMessage> target) {

        if (!messageTypes.containsKey(target.getName())) {
            this.registerMessageListener(target, (c, m) -> m.handle(c));
        }
    }

    /**
     * Register a {@link HandledMessage} that handles its own
     * receiving of messages.
     *
     * @param target The target type of {@link HandledMessage}.
     * @param channel The channel that should be matched to exactly
     *                before {@link HandledMessage#handle(String)} is called.
     */
    public final void registerMessage(Class<? extends HandledMessage> target, String channel) {

        if (messageTypes.containsKey(target.getName())) {
            return;
        }

        this.registerMessageListener(target, (c, m) -> {

            if (channel.equals(c)) {
                m.handle(c);
            }
        });
    }

    /**
     * Registers a message listener for catching messages.
     *
     * @param target The type of message to listen for
     * @param function The function to run when the message is received. String being the channel the message was received
     *                 on and the object being the message received.
     * @param <T> The type of message to listen for
     */
    public final <T extends Message> void registerMessageListener(Class<T> target, BiConsumer<String, T> function) {
        this.messageTypes.put(target.getName(), target); // register message type
        this.listenerContainers.add(new ListenerContainer<>(target, function));
    }

    /**
     * Registers a message listener for catching messages. This method differs in the sense that it takes a raw object.
     * In this object, you can have multiple methods that will handle an incoming message with 2 parameters: the first
     * being a string (the channel), and the message you will be receiving of type {@code Message}
     * <p>
     * This method must be annotated with {@link MessageListener} to be registered.
     * <p>
     * Example:
     * <pre>
     * {@code
     * public class MyListenerClass {
     *      @MessageListener
     *      public void receiveAMessage(String channel, ACertainMessage message) {
     *          // do something...
     *      }
     *
     *      // other methods, constructors, params, more receivers etc.
     * }
     * }
     * </pre>
     *
     * @param listener The object to look for listener methods in
     */
    @SuppressWarnings("unchecked")
    public final void registerMessageListener(Object listener) {
        for (Method method : listener.getClass().getMethods()) {

            if (!method.isAnnotationPresent(MessageListener.class) || method.getParameterCount() != 2) {
                continue;
            }

            var params = method.getParameterTypes();
            if (params[0].isAssignableFrom(String.class) && Message.class.isAssignableFrom(params[1])) {
                Class<? extends Message> param = (Class<? extends Message>) params[1]; // fuck you, I know what I'm doing
                this.messageTypes.put(param.getName(), param); // register message type
                this.listenerContainers.add(new ListenerContainer<>(param, (s, t) -> {
                    try {
                        method.invoke(listener, s, t);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }));
            }
        }
    }

    public Map<String, Class<? extends Message>> getMessageTypes() {
        return messageTypes;
    }

    public List<ListenerContainer<?>> getListenerContainers() {
        return listenerContainers;
    }

    /**
     * Send out a {@link Message} to the {@link #DEFAULT_CHANNEL}.
     *
     * @param message The {@link Message} to send.
     */
    public void send(Message message) {
        this.send(DEFAULT_CHANNEL, message);
    }

    /**
     * Sends a message out. Check the specific implementations of the child classes to see how the message is sent
     *
     * @param channel The channel to send the message on
     * @param message The message to send
     */
    public abstract void send(String channel, Message message);

    /**
     * Subscribe the messaging client to listen to messages on the specified channel
     *
     * @param channel channel to start listening to
     */
    public abstract void subscribe(String channel);

    /**
     * Unsubscribe the message client to stop listening for messages on the specified channel.
     * There is no consequence for unsubscribing to a channel that was never subscribed to.
     *
     * @param channel channel to stop listening to
     */
    public abstract void unsubscribe(String channel);

    public record ListenerContainer<T extends Message>(Class<T> type, BiConsumer<String, T> function) {

        public void handle(String channel, Message message) {

            if (type.isInstance(message)) {
                function.accept(channel, (T) message);
            }
        }
    }

    /**
     * A pseudo marker interface for message types.
     */
    public interface Message {
        default String getIdentifier() {
            return getClass().getName();
        }
    }

    /**
     * A {@link Message} that provides a {@link #handle(String)}
     * method for internal handling of the message.
     */
    public interface HandledMessage extends Message {

        /**
         * Handle this message once it has been received.
         *
         * @param channel The channel this message was received on.
         */
        void handle(String channel);
    }

    /**
     * Marker interface for methods
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MessageListener {
    }
}
