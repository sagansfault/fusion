package com.projecki.fusion.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.projecki.fusion.FusionPaper;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class HotbarItem {

    private static final NamespacedKey HOTBAR_ITEM_ID = new NamespacedKey("fusion", "hotbar-item-id");

    private final ItemStack item;
    private final UUID uuid;
    private final Consumer<Player> onInteract;

    private HotbarItem(ItemStack item, Consumer<Player> onInteract) {
        this.uuid = UUID.randomUUID();
        this.item = ItemBuilder.from(item).nbtString(HOTBAR_ITEM_ID, this.uuid.toString()).build();
        this.onInteract = onInteract;
    }

    private ItemStack getItem() {
        return item;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Consumer<Player> getOnInteract() {
        return onInteract;
    }

    /**
     * Creates and gives a player a new hotbar item.
     *
     * @param player The player to give this hotbar item to
     * @param slot The slot in the players inventory to set this item to (doesn't have to be a hotbar slot)
     * @param item The item to set
     */
    public static void give(Player player, int slot, ItemStack item, Consumer<Player> onInteract) {
        HotbarItem hotbarItem = new HotbarItem(item, onInteract);
        player.getInventory().setItem(slot, hotbarItem.getItem());
        FusionPaper.getHotbarItemRegistry().getRegistry().put(player.getUniqueId(), hotbarItem);
    }

    /**
     * Returns whether this item given is a hotbar item, (ie.) it's tags are set to be a hotbar item; it was created here.
     *
     * @param item The item to check
     * @return Whether this item is a hotbar item
     */
    public static boolean isHotbarItem(ItemStack item) {
        return item != null &&
                item.getType() != Material.AIR &&
                item.getItemMeta().getPersistentDataContainer().getKeys().contains(HOTBAR_ITEM_ID);
    }

    /**
     * Returns an optional containing the uuid of the hotbar item of the given item. The returned optional is empty
     * if the item is not a hotbar item.
     *
     * @param item The item to check
     * @return The id of the hotbar item or empty if it was not a hotbar item
     */
    public static Optional<UUID> getHotbarItemId(ItemStack item) {
        String id = item.getItemMeta().getPersistentDataContainer().get(HOTBAR_ITEM_ID, PersistentDataType.STRING);
        if (id == null) {
            return Optional.empty();
        }
        UUID uuid = null;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ignored) {}
        return Optional.ofNullable(uuid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HotbarItem that = (HotbarItem) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    public static class Registry implements Listener {

        private final Multimap<UUID, HotbarItem> registry = HashMultimap.create();

        private Multimap<UUID, HotbarItem> getRegistry() {
            return registry;
        }

        @EventHandler
        public void interact(PlayerInteractEvent event) {
            ItemStack item = event.getItem();
            if (item != null) {
                HotbarItem.getHotbarItemId(item).ifPresent(uuid -> {
                    event.setCancelled(true);
                    registry.get(event.getPlayer().getUniqueId()).stream()
                            .filter(hotbarItem -> hotbarItem.getUUID().equals(uuid))
                            .forEach(hotbarItem -> hotbarItem.getOnInteract().accept(event.getPlayer()));
                });
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void inventory(InventoryClickEvent event) {
            if (HotbarItem.isHotbarItem(event.getCurrentItem()) || HotbarItem.isHotbarItem(event.getCursor())) {
                event.setCancelled(true);
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void drop(PlayerDropItemEvent event) {
            if (HotbarItem.isHotbarItem(event.getItemDrop().getItemStack())) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void leave(PlayerQuitEvent event) {
            registry.removeAll(event.getPlayer().getUniqueId());
        }
    }
}
