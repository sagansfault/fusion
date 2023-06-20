package com.projecki.fusion.util;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.message.MessageClient;
import com.projecki.fusion.redis.pubsub.message.impl.chat.BroadcastMessageCrossServer;
import com.projecki.fusion.redis.pubsub.message.impl.chat.MessagePlayerCrossServer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class NetworkChat {

    public static final String CHANNEL = "network-chat";

    /**
     * <b>If you do not have access to the UUID, use the NameResolver to get it</b>
     *
     * Sends a message to the specified player on the network.
     *
     * @param target The target player's uuid
     * @param message The message to send
     */
    public static void sendToPlayer(UUID target, Component message) {
        FusionPaper.getMessageClient().send(CHANNEL, new MessagePlayerCrossServer(target, message));
    }

    /**
     * Broadcasts a message to a group of servers.
     *
     * @param message The message to broadcast
     * @param servers The servers to broadcast to
     */
    public static void broadcastToServers(Component message, String... servers) {
        FusionPaper.getMessageClient().send(CHANNEL, new BroadcastMessageCrossServer(message, servers));
    }


    /**
     * Broadcasts a message to the whole network.
     *
     * @param message The message to broadcast
     */
    public static void broadcastToNetwork(Component message) {
        FusionPaper.getMessageClient().send(CHANNEL, new BroadcastMessageCrossServer(message));
    }

    public static class Receiver {

        private final FusionPaper fusionPaper;

        public Receiver(FusionPaper fusionPaper) {
            this.fusionPaper = fusionPaper;
        }

        @MessageClient.MessageListener
        public void onMessage(String channel, MessagePlayerCrossServer message) {
            Player player = fusionPaper.getServer().getPlayer(message.getTargetUUID());
            if (player != null) {
                player.sendMessage(message.getMessage());
            }
        }

        @MessageClient.MessageListener
        public void onBroadcast(String channel, BroadcastMessageCrossServer message) {
            FusionPaper.getServerInfo().ifPresentOrElse(serverInfo -> {
                if (message.getServers().length == 0) {
                    fusionPaper.getServer().broadcast(message.getMessage());
                } else {
                    for (String server : message.getServers()) {
                        if (serverInfo.getServerName().equalsIgnoreCase(server)) {
                            fusionPaper.getServer().broadcast(message.getMessage());
                            break;
                        }
                    }
                }
            }, () -> fusionPaper.getLogger().log(Level.WARNING, "Server info not loaded yet (broadcast). It should be!"));
        }
    }
}
