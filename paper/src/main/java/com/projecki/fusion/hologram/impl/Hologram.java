package com.projecki.fusion.hologram.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.hologram.AbstractHologram;
import com.projecki.fusion.hologram.PacketEntityObserver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class Hologram extends AbstractHologram {

    private static final Random RANDOM = ThreadLocalRandom.current();
    private static final double LINE_HEIGHT = 0.3;
    private final List<Component> lines;

    private final PacketEntityObserver observer = new PacketEntityObserver();

    /**
     * Constructs a new hologram with the given lines. This constructor does nothing special except accept the lines
     * you give it as a starting collection.
     *
     * @param lines The lines to start this hologram off with
     */
    public Hologram(Component... lines) {
        this.lines = Arrays.stream(lines).collect(Collectors.toList());
    }

    /**
     * Appends a lines to the bottom of the hologram. Make sure to call {@code .reRender()} to make the
     * changes visible.
     *
     * @param lines The lines to append
     */
    public void append(Component... lines) {
        Collections.addAll(this.lines, lines);
    }

    /**
     * Sets a specific line in this hologram to a component. If the index is outside the current list size (the line does
     * not exist), an error is thrown.
     *
     * @param index The index to set the line at, if the index is out of bounds an error is thrown
     * @param line  The line to send
     */
    public void setLine(int index, Component line) {
        if (index >= this.lines.size()) {
            throw new IndexOutOfBoundsException("There is no line there to set!");
        } else {
            this.lines.set(index, line);
        }
    }

    /**
     * Sets a specific line in this hologram to an empty component. If the index is outside the current list size (the
     * line does not exist), an error is thrown.
     *
     * @param index The index to set the line at, if the index is out of bounds an error is thrown
     */
    public void setEmptyLine(int index) {
        this.setLine(index, Component.empty());
    }

    /**
     * Appends an empty line to the hologram
     */
    public void appendEmptyLine() {
        this.append(Component.empty());
    }

    /**
     * Removes a line from the hologram. Line indexes start at 0. Make sure to call {@code .reRender()} to make the
     * changes visible
     *
     * @param index The index of the line to remove (line indexes start at 0)
     */
    public void removeLine(int index) {
        try {
            this.lines.remove(index);
        } catch (UnsupportedOperationException | IndexOutOfBoundsException ignored) {
        }
    }

    /**
     * Renders a new instance of this hologram at a given location to a collection of players. This instance of the
     * hologram will also get updated versions of the data when {@code .reRender()} is called
     *
     * @param location The location to build the hologram at
     * @param players  The collection of players to show this hologram to
     */
    @Override
    public void render(@NotNull Location location, @NotNull Collection<? extends Player> players) {

        // The last line should be the location, so extend all others up
        int lines = this.lines.size();
        double offset_y = LINE_HEIGHT * lines;

        ProtocolManager manager = FusionPaper.getProtocolManager();

        for (Component line : this.lines) {
            int entityId = RANDOM.nextInt(100000);

            // Entity itself
            PacketContainer entityPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
            entityPacket.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND); // set to armor stand
            entityPacket.getIntegers().write(0, entityId); // entity id
            entityPacket.getUUIDs().write(0, UUID.randomUUID()); // entity uuid

            // Entity invisible, and name
            PacketContainer metadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

            WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
            String json = GsonComponentSerializer.gson().serialize(line);
            Optional<?> opt = Optional.of(WrappedChatComponent.fromJson(json).getHandle());
            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20); //invis
            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), opt);
            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true); //custom name visible
            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true); //no gravity
            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) (0x01 | 0x08 | 0x10)); //isSmall, noBasePlate, set Marker

            metadataPacket.getIntegers().write(0, entityId);
            metadataPacket.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());

            entityPacket.getDoubles().write(0, location.getX());
            entityPacket.getDoubles().write(1, location.getY() + offset_y);
            entityPacket.getDoubles().write(2, location.getZ());
            offset_y -= LINE_HEIGHT;

            for (Player player : players) {
                try {
                    manager.sendServerPacket(player, entityPacket);
                    manager.sendServerPacket(player, metadataPacket);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            observer.addObservable(location, entityId, new ArrayList<>(players));
        }
    }

    /**
     * This method essentially destroys all instances of this hologram and renders a new one with the current data at
     * each location. Call this when you want to send changes you made to the hologram
     *
     * @param players The players to send this update to
     */
    @Override
    public void reRender(@NotNull Collection<? extends Player> players) {
        unRender(players);
        observer.getLocations().forEach(location -> render(location, players));
    }

    /**
     * Destroy all instances of a hologram for a given set of players.
     *
     * @param players The players to remove/destroy the hologram for
     */
    @Override
    public void unRender(@NotNull Collection<? extends Player> players) {
        ProtocolManager manager = FusionPaper.getProtocolManager();

        players.forEach(player -> {
            PacketContainer destroy = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
            destroy.getIntLists().write(0, observer.getEntityIds(player)); // entity ids to destroy
            try {
                manager.sendServerPacket(player, destroy, false);
                observer.removeObserver(player);

            } catch (InvocationTargetException e) {
                System.out.println("Could not send entity destroy packet! Holograms error:");
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
}
