package com.projecki.fusion.hologram;

import com.google.common.base.Preconditions;
import com.projecki.fusion.FusionPaper;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class HologramAnchor<T extends AbstractHologram> extends AbstractHologram implements Listener {

    /**
     * All chunks that need to be watched in order to render the {@link AbstractHologram} to the correct players
     */
    private final Set<Long> chunks = new HashSet<>();

    /**
     * The holographic that will be anchored to a specific location
     */
    private final T hologram;

    /**
     * Construct a new {@link HologramAnchor} that will anchor {@link AbstractHologram} instances to their location, and render them for
     * each player that comes into range.
     */
    public HologramAnchor(@NotNull T hologram) {

        // make sure argument isn't an anchor
        Preconditions.checkArgument(!(hologram instanceof HologramAnchor), "Hologram Anchors cannot be anchored again!");

        this.hologram = hologram;
        Bukkit.getPluginManager().registerEvents(this, FusionPaper.getPlugin(FusionPaper.class));
    }

    /**
     * Get the {@link AbstractHologram} that's anchored by this anchor.
     *
     * @return the holographic instance
     */
    @NotNull
    public T getHologram() {
        return hologram;
    }

    @Override
    public void render(@NotNull Location location, @NotNull Collection<? extends Player> players) {
        chunks.add(location.getChunk().getChunkKey());
        hologram.render(location, players);
    }

    /**
     * This method essentially destroys all instances of this holographic and renders a new one with the current data at
     * each location. Call this when you want to send changes you made to the holographic
     *
     * @param players The players to send this update to
     */
    @Override
    public void reRender(@NotNull Collection<? extends Player> players) {
        hologram.reRender(players);
    }

    /**
     * Destroy all instances of a holographic for a given set of players.
     *
     * @param players The players to remove/destroy the holographic for
     */
    @Override
    public void unRender(@NotNull Collection<? extends Player> players) {
        hologram.unRender(players);
    }

    /**
     * Destroy this{@link HologramAnchor} instance, by unregistering the {@link Listener}.
     */
    @Override
    public void destroy() {
        hologram.destroy();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onChunkRender(PlayerChunkLoadEvent event) {
        if (!chunks.contains(event.getChunk().getChunkKey())) return;

        // render the hologram for the player
        this.reRender(event.getPlayer());
    }

    @EventHandler
    public void onChunkUnloadEvent(PlayerChunkUnloadEvent event) {

        if (!chunks.contains(event.getChunk().getChunkKey())) return;

        this.unRender(event.getPlayer());

    }
}
