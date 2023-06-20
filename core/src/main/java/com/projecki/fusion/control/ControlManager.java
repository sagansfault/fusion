package com.projecki.fusion.control;

import com.projecki.fusion.message.MessageClient;

import java.util.concurrent.CompletableFuture;

public class ControlManager {

    private final MessageClient messageClient;

    public ControlManager(MessageClient messageClient) {
        this.messageClient = messageClient;
    }

    /**
     * Send the {@link ControlAction} to the specified server.
     *
     * This method makes no guarantee that the server exists or that
     * the server is online and received the message.
     *
     * @param serverName name of server
     * @param action action to send to server
     */
    public void sendControlAction(String serverName, ControlAction action) {
        messageClient.send(ControlMessage.getServerChannel(serverName), new ControlMessage(action));
    }
}
