package com.projecki.fusion.transport;

import com.projecki.fusion.constants.PluginMessageNames;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.proxy.protocol.packet.JoinGame;
import com.velocitypowered.proxy.protocol.packet.Respawn;
import dev.simplix.protocolize.api.Direction;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.listener.AbstractPacketListener;
import dev.simplix.protocolize.api.listener.PacketReceiveEvent;
import dev.simplix.protocolize.api.listener.PacketSendEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Listens for plugin messages to do a seamless server connection.
 * <p>
 * A seamless server connection connects a player to another server
 * without sending a respawn packet or sending a join game packet.
 *<p>
 * <b>DO NOT USE:</b> Unless the sending server supports this kind of
 * connction. Otherwise, weird bugs will occur for the client.
 */
public class SeamlessConnectListener {

    private static final ChannelIdentifier CHANNEL = MinecraftChannelIdentifier.create(
            PluginMessageNames.CHANNEL_NAMESPACE, PluginMessageNames.SEAMLESS_CHANNEL);

    private final ProxyServer proxy;

    private final Map<UUID, BlockedTimes> packetblocks = new ConcurrentHashMap<>();

    /**
     * @param proxy velocity proxy object needed to register listening
     *              to plugin channels
     */
    public SeamlessConnectListener(ProxyServer proxy) {
        this.proxy = proxy;
        proxy.getChannelRegistrar().register(CHANNEL);
        Protocolize.listenerProvider().registerListener(new RespawnListener());
        Protocolize.listenerProvider().registerListener(new JoinListener());
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(CHANNEL))
            return;

        if (event.getSource() instanceof ServerConnection connection) {
            var player = connection.getPlayer();


            var in = event.dataAsDataStream();
            var serverName = in.readUTF();

            var server = proxy.getServer(serverName);

            if (server.isPresent()) {
                player.createConnectionRequest(server.get()).connect()
                        .thenRun(() -> packetblocks.remove(player.getUniqueId()));

                packetblocks.put(player.getUniqueId(), new BlockedTimes());
            } else {
                Logger.getGlobal().warning("Cannot find server with name '" + serverName + "'. " +
                        "From plugin message to seamless connect.");
            }
        }
    }

    @Subscribe
    public void onLeave(DisconnectEvent event) {
        packetblocks.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Tracks how many times to block a packet for a player
     */
    private class BlockedTimes {
        // default values are how many times each packet is blocked
        private int respawnBlocked = 2;
        private int joinBlocked = 1;
    }

    public class RespawnListener extends AbstractPacketListener<Respawn> {

        private RespawnListener() {
            super(Respawn.class, Direction.UPSTREAM, 0);
        }

        @Override
        public void packetReceive(PacketReceiveEvent<Respawn> packetReceiveEvent) {
        }

        @Override
        public void packetSend(PacketSendEvent<Respawn> packetSendEvent) {
            var times = packetblocks.get(packetSendEvent.player().uniqueId());
            if (times == null) return;

            if (times.respawnBlocked > 0) {
                packetSendEvent.cancelled(true);
                times.respawnBlocked--;
            }
        }
    }

    public class JoinListener extends AbstractPacketListener<JoinGame> {

        private JoinListener() {
            super(JoinGame.class, Direction.UPSTREAM, 0);
        }

        @Override
        public void packetReceive(PacketReceiveEvent<JoinGame> packetReceiveEvent) {
        }

        @Override
        public void packetSend(PacketSendEvent<JoinGame> packetSendEvent) {
            var times = packetblocks.get(packetSendEvent.player().uniqueId());
            if (times == null) return;

            if (times.joinBlocked > 0) {
                packetSendEvent.cancelled(true);
                times.joinBlocked--;
            }
        }
    }

}
