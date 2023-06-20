package com.projecki.fusion.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.GamePhase;
import com.projecki.unversioned.scoreboard.ScoreboardService;
import org.bukkit.plugin.Plugin;

import java.util.Set;

/**
 * @since July 23, 2022
 * @author Andavin
 */
public class ScoreboardPacketListener implements PacketListener {

    /**
     * Register this {@link PacketListener} with the {@link Plugin}
     * and {@link ProtocolManager}.
     *
     * @param plugin The {@link Plugin} to use to register.
     * @param protocolManager The {@link ProtocolManager} to register
     *                        this {@link PacketListener} with.
     */
    public static void register(Plugin plugin, ProtocolManager protocolManager) {
        protocolManager.addPacketListener(new ScoreboardPacketListener(plugin));
    }

    private final Plugin plugin;
    private final ListeningWhitelist clientbound, serverbound;

    private ScoreboardPacketListener(Plugin plugin) {
        this.plugin = plugin;
        this.clientbound = ListeningWhitelist.newBuilder()
                .priority(ListenerPriority.NORMAL)
                .gamePhase(GamePhase.PLAYING)
                .options(Set.of(ListenerOptions.ASYNC))
                .types(PacketType.Play.Server.PLAYER_INFO) // ClientboundPlayerInfoPacket
                .build();
        this.serverbound = ListeningWhitelist.EMPTY_WHITELIST;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public ListeningWhitelist getSendingWhitelist() {
        return clientbound;
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist() {
        return serverbound;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        ScoreboardService.INSTANCE.handlePacketSend(event);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        // No need to receive anything yet
    }
}
