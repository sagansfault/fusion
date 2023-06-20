package com.projecki.fusion.control;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.projecki.fusion.message.MessageClient;

import java.util.logging.Logger;

/**
 * Handles receiving and processing {@link ControlMessage} messages
 */
public class ControlMessageHandler {

    private final Multimap<ControlAction, Runnable> actionHandlers =
            MultimapBuilder.hashKeys().hashSetValues().build();

    /**
     * @param messageClient message client to receive messages
     * @param serverName    name of this server
     */
    public ControlMessageHandler(MessageClient messageClient, String serverName) {
        messageClient.subscribe(ControlMessage.getServerChannel(serverName));
        messageClient.registerMessageListener(ControlMessage.class, this::processMessage);
    }

    private void processMessage(String channel, ControlMessage message) {
        Logger.getGlobal().info("Control: " + message.action() + " was called on this instance");
        actionHandlers.get(message.action())
                .forEach(Runnable::run);
    }

    /**
     * Register an action to be run when a {@link ControlMessage} is received
     * with the specified {@link ControlAction}
     *
     * @param action  the type of action that you want to listen for
     * @param handler what to run when the action message is received
     */
    public void registerHandler(ControlAction action, Runnable handler) {
        actionHandlers.put(action, handler);
    }

}
