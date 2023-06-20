package com.projecki.fusion.control;

import com.projecki.fusion.message.MessageClient;
import org.bukkit.Bukkit;

public class PaperControlMessageHandler extends ControlMessageHandler {

    private final SafeStopHandler safeStopHandler;

    /**
     * @param messageClient   message client to receive messages
     * @param serverName      name of this server
     * @param safeStopHandler {@link SafeStopHandler} for the server
     */
    public PaperControlMessageHandler(MessageClient messageClient, String serverName, SafeStopHandler safeStopHandler) {
        super(messageClient, serverName);
        this.safeStopHandler = safeStopHandler;

        registerHandler(ControlAction.SAFE_STOP, this::handleSafeStop);
        registerHandler(ControlAction.SHUTDOWN, this::handleShutdown);
        registerHandler(ControlAction.KILL, this::handleKill);
    }

    private void handleSafeStop() {
        if (safeStopHandler.safeToStop()) {
            Bukkit.shutdown();
        }
    }

    private void handleShutdown() {
        Bukkit.shutdown();
    }

    private void handleKill() {
        Bukkit.getLogger().warning("This server was just commanded to forcibly exit!");
        System.exit(1);
    }
}
