package com.projecki.fusion.hologram.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.projecki.fusion.hologram.AbstractHologram;
import com.projecki.fusion.hologram.PacketEntityObserver;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ItemHologram extends AbstractHologram {

    private final PacketEntityObserver observer = new PacketEntityObserver();

    @NotNull
    private ItemStack itemStack;

    /**
     * Construct a new {@link ItemHologram}
     *
     * @param itemStack the item stack that will be rendered
     */
    public ItemHologram(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Render the {@link ItemHologram} at the specified {@link Location} for the specified players.
     *
     * @param location the location to render the item hologram at
     * @param players  the players to render the hologram for
     */
    @Override
    public void render(@NotNull Location location, @NotNull Collection<? extends Player> players) {

        int entityId = ThreadLocalRandom.current().nextInt(100000);
        renderHologram(entityId, location, players);
        observer.addObservable(location, entityId, players);
    }

    /**
     * Renders the hologram with the specified entity id.
     *
     * @param entityId the entity id
     * @param location the location to render the hologram at
     * @param players  the players to render the hologram for
     */
    private void renderHologram(int entityId, @NotNull Location location, Collection<? extends Player> players) {
        PacketContainer entityPacket = createEntityPacket(location, entityId);
        PacketContainer metadataPacket = createMetadataPacket(entityId);

        players.forEach(player -> {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, entityPacket);
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, metadataPacket);

            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Send out update packets to all the specified players.
     *
     * @param players the players to send out the update packet to
     */
    @Override
    public void reRender(@NotNull Collection<? extends Player> players) {

        // un render the hologram
        unRender(players);
        observer.getLocations().forEach(location -> render(location, players));
    }

    /**
     * Destroy the {@link ItemHologram} for all players
     */
    @Override
    public void unRender(@NotNull Collection<? extends Player> players) {
        players.forEach(player -> {
            try {

                // construct packet
                PacketContainer removePacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
                removePacket.getIntLists().write(0, observer.getEntityIds(player));

                ProtocolLibrary.getProtocolManager().sendServerPacket(player, removePacket);
                observer.removeObserver(player);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Destroy all instances of a holographic for this holographic.
     */
    @Override
    public void destroy() {
        unRender();
        observer.clear();
    }

    /**
     * Update the {@link ItemStack} for this {@link ItemHologram}.
     * This will update the hologram for all online players, automatically.
     *
     * @param itemStack the item stack
     */
    public void setItemStack(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
        reRender();
    }

    private PacketContainer createEntityPacket(@NotNull Location location, int entityId) {

        PacketContainer entityPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        entityPacket.getIntegers().write(0, entityId);
        entityPacket.getUUIDs().write(0, UUID.randomUUID());
        entityPacket.getEntityTypeModifier().write(0, EntityType.DROPPED_ITEM);

        entityPacket.getDoubles().write(0, location.getX());
        entityPacket.getDoubles().write(1, location.getY());
        entityPacket.getDoubles().write(2, location.getZ());

        return entityPacket;
    }

    private PacketContainer createMetadataPacket(int entityId) {
        PacketContainer metadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.getItemStackSerializer(false)), BukkitConverters.getItemStackConverter().getGeneric(itemStack));

        metadataPacket.getIntegers().write(0, entityId);
        metadataPacket.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        return metadataPacket;
    }
}
