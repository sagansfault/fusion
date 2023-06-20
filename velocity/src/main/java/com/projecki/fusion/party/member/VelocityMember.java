package com.projecki.fusion.party.member;

import com.projecki.fusion.FusionVelocity;
import com.projecki.fusion.redis.CommonRedisChannels;
import com.projecki.fusion.redis.pubsub.message.impl.network.PlayerServerSendMessage;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.UUID;

import static java.util.Objects.requireNonNullElseGet;

/**
 * @since June 30, 2022
 * @author Andavin
 */
public class VelocityMember implements Member {

    private final UUID id;
    private final String name;
    private final ProxyServer server;
    private WeakReference<Player> player;

    public VelocityMember(ProxyServer server, UUID id, String name) {
        this.id = id;
        this.name = name;
        this.server = server;
    }

    public VelocityMember(ProxyServer server, Player player) {
        this(server, player.getUniqueId(), player.getUsername());
        this.player = new WeakReference<>(player);
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public @NotNull Audience audience() {
        return requireNonNullElseGet(player.get(), () -> {
            Player player = server.getPlayer(id)
                    .orElseThrow(() -> new NullPointerException(name + " is not online"));
            this.player = new WeakReference<>(player);
            return player;
        });
    }

    @Override
    public void send(String serverId) {
        server.getPlayer(id).ifPresentOrElse(
                player -> server.getServer(serverId).ifPresent(server -> // The player is on this server
                        player.createConnectionRequest(server).fireAndForget()),
                () -> FusionVelocity.getMessageClient().send(
                        CommonRedisChannels.SERVER_LOOKUP_CHANNEL.getChannel(),
                        new PlayerServerSendMessage(id, serverId)
                )
        );
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof VelocityMember m && m.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
