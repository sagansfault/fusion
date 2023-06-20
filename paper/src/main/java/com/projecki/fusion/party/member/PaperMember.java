package com.projecki.fusion.party.member;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.redis.CommonRedisChannels;
import com.projecki.fusion.redis.pubsub.message.impl.network.PlayerServerSendMessage;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNullElseGet;

/**
 * @since May 16, 2022
 * @author Andavin
 */
public class PaperMember implements Member {

    private final UUID id;
    private final String name;
    private WeakReference<Player> player;

    public PaperMember(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public PaperMember(Player player) {
        this(player.getUniqueId(), player.getName());
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
            Player player = Bukkit.getPlayer(id);
            //noinspection ConstantConditions
            checkNotNull(player, "%s is not online", name);
            this.player = new WeakReference<>(player);
            return player;
        });
    }

    @Override
    public void send(String serverId) {
        FusionPaper.getMessageClient().send(
                CommonRedisChannels.SERVER_LOOKUP_CHANNEL.getChannel(),
                new PlayerServerSendMessage(id, serverId)
        );
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof PaperMember m && m.id.equals(id);
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
