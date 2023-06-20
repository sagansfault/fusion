package com.projecki.fusion.customfloatingtexture;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.projecki.fusion.item.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PacketBasedCustomFloatingTexture extends AbstractCustomFloatingTexture {

    private final int entityId = ThreadLocalRandom.current().nextInt(10000);
    private final Set<Player> viewers = new HashSet<>();

    public PacketBasedCustomFloatingTexture(Location origin, int rotation, int modelData) {
        super(origin, rotation, modelData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCustomTextureUpdate(int modelData) {
        PacketContainer equipment = this.getItemEquipmentPacket();

        viewers.forEach(player -> {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, equipment);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void show(Collection<? extends Player> players) {
        this.viewers.addAll(players);

        List<PacketContainer> packets = this.getPackets();

        players.forEach(player -> packets.forEach(packet -> {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }));
    }

    /**
     * {@inheritDoc}
     */
    public void show(Player player) {
        this.show(Collections.singleton(player));
    }

    private PacketContainer getItemEquipmentPacket() {
        PacketContainer equipment = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        equipment.getIntegers().write(0, entityId);
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentData = new ArrayList<>();
        equipmentData.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, ItemBuilder.of(Material.PAPER).customModelData(modelData).build()));
        equipment.getSlotStackPairLists().write(0, equipmentData);
        return equipment;
    }

    private List<PacketContainer> getPackets() {
        List<PacketContainer> containers = new ArrayList<>();

        PacketContainer entity = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        entity.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND); // set to armor stand
        entity.getIntegers().write(0, entityId); // entity id
        entity.getUUIDs().write(0, UUID.randomUUID()); // entity uuid
        entity.getDoubles().write(0, origin.getX());
        entity.getDoubles().write(1, origin.getY());
        entity.getDoubles().write(2, origin.getZ());

        int angleClamped = rotation >= 360 ? rotation - 360 : Math.max(0, rotation);

        PacketContainer metaData = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20); //invis
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true); //no gravity
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) (0x01 | 0x08 | 0x10)); //isSmall, noBasePlate, set Marker
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(16,
                WrappedDataWatcher.Registry.getVectorSerializer()), new Vector3F(0f, (float) angleClamped, 0f));
        metaData.getIntegers().write(0, entityId);
        metaData.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());

        containers.add(entity);
        containers.add(metaData);
        containers.add(this.getItemEquipmentPacket());
        return containers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() {
        PacketContainer delete = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        delete.getIntLists().write(0, List.of(entityId));
        this.viewers.forEach(player -> {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, delete);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }
}
