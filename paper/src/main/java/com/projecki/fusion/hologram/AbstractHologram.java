package com.projecki.fusion.hologram;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public abstract class AbstractHologram {

    /**
     * Renders a new instance of this holographic at a given location to a collection of players. This instance of the
     * holographic will also get updated versions of the data when {@code .reRender()} is called
     *
     * @param location The location to build the holographic at
     * @param players  The collection of players to show this holographic to
     */
    public abstract void render(@NotNull Location location, @NotNull Collection<? extends Player> players);

    /**
     * Renders a new instance of this holographic at a given location to a player. This instance of the
     * holographic will also get updated versions of the data when {@code .reRender()} is called
     *
     * @param location The location to build the holographic at
     * @param player   The player to show this holographic to
     */
    public void render(@NotNull Location location, @NotNull Player player) {
        this.render(location, Collections.singleton(player));
    }

    /**
     * Renders a new instance of this holographic at a given location to all players currently online. This instance of the
     * holographic will also get updated versions of the data when {@code .reRender()} is called
     *
     * @param location The location to build the holographic at
     */
    public void render(@NotNull Location location) {
        this.render(location, Bukkit.getOnlinePlayers());
    }

    /**
     * This method essentially destroys all instances of this holographic and renders a new one with the current data at
     * each location. Call this when you want to send changes you made to the holographic
     *
     * @param players The players to send this update to
     */
    public abstract void reRender(@NotNull Collection<? extends Player> players);

    /**
     * This method essentially destroys all instances of this holographic and renders a new one with the current data at
     * each location. Call this when you want to send changes you made to the holographic
     *
     * @param player The player to send this update to
     */
    public void reRender(@NotNull Player player) {
        this.reRender(Collections.singleton(player));
    }

    /**
     * This method essentially destroys all instances of this holographic and renders a new one with the current data at
     * each location. Call this when you want to send changes you made to the holographic. This sends an update to all
     * players online, not just the players who this holographic was previously rendered/visible to
     */
    public void reRender() {
        this.reRender(Bukkit.getOnlinePlayers());
    }

    /**
     * Destroy all instances of a holographic for a given set of players.
     *
     * @param players The players to remove/destroy the holographic for
     */
    public abstract void unRender(@NotNull Collection<? extends Player> players);

    /**
     * Destroy all instances of a holographic for a given player.
     *
     * @param player The player to remove/destroy the holographic for
     */
    public void unRender(@NotNull Player player) {
        this.unRender(Collections.singleton(player));
    }

    /**
     * Destroy all instances of a holographic for all players online. This will have no effect for players who couldn't
     * see the holographic to begin with.
     */
    public void unRender() {
        this.unRender(Bukkit.getOnlinePlayers());
    }

    /**
     * Destroy all instances of a holographic for this holographic.
     */
    public abstract void destroy();
}
