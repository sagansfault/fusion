package com.projecki.fusion.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class PacketEntityObserver {

    /**
     * Map that maps locations to observable entity instances
     */
    private final Map<Location, List<ObservableEntity>> viewers = new HashMap<>();

    /**
     * Get a list of all locations that contain observable entities.
     *
     * @return an {@link Unmodifiable} list of all locations that contain observable entities
     */
    @Unmodifiable
    public List<Location> getLocations() {
        return viewers.keySet().stream().toList();
    }

    /**
     * Add a new observable entity that will be tracked for a specific list of players
     *
     * @param location the location of the observable entity
     * @param entityId the entity of the observable entity
     * @param players  the players that are observing the entity
     */
    public void addObservable(@NotNull Location location, int entityId, @NotNull Collection<? extends Player> players) {
        if (!viewers.containsKey(location)) {
            viewers.put(location, new ArrayList<>());
        }

        viewers.get(location).add(new ObservableEntity(entityId, location.getChunk().getChunkKey(), location, new ArrayList<>(players)));
    }

    /**
     * Remove all observable entities for the specified {@link Player}.
     *
     * @param player the player
     */
    public void removeObserver(@NotNull Player player) {
        viewers.values()
                .forEach(observers -> observers.stream()
                        .filter(observable -> observable.isObserver(player))
                        .forEach(observable -> observable.removeObserver(player))
                );

        viewers.values()
                .stream()
                .flatMap(observers -> observers
                        .stream()
                        .filter(Predicate.not(ObservableEntity::hasObservers))
                )
                .toList() // make list to prevent CME
                .forEach(observable -> viewers.get(observable.location()).remove(observable));

    }


    /**
     * Get a {@link Unmodifiable} list of all entity ids of observable entities.
     *
     * @param player the player to get the entity ids for
     * @return an {@link Unmodifiable} list of all entity ids being observed by this player
     */
    @Unmodifiable
    public List<Integer> getEntityIds(@NotNull Player player) {
        return viewers.values()
                .stream()
                .flatMap(observers -> observers
                        .stream()
                        .filter(observable -> observable.isObserver(player))
                        .map(ObservableEntity::entityId)
                )
                .toList();

    }

    /**
     * Clear all the viewers of this observer
     */
    public void clear() {
        viewers.clear();
    }


    record ObservableEntity(int entityId, long chunkKey, @NotNull Location location, @NotNull List<Player> observers) {

        public void removeObserver(@NotNull Player player) {
            observers.remove(player);
        }

        /**
         * Get whether the specified {@link Player} is an observer.
         *
         * @param player the player
         * @return {@code true} if the player is an observer or else {@code false}
         */
        public boolean isObserver(@NotNull Player player) {
            return observers.contains(player);
        }

        /**
         * Get whether this {@link ObservableEntity} has any observers left.
         *
         * @return {@code true} if the hologram has observers left or else {@code false}
         */
        public boolean hasObservers() {
            return !observers.isEmpty();
        }

    }
}
