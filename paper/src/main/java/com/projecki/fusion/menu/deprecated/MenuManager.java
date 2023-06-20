package com.projecki.fusion.menu.deprecated;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.menu.deprecated.function.ButtonFunction;
import com.projecki.fusion.menu.deprecated.function.MenuCloseFunction;
import com.projecki.fusion.menu.deprecated.function.SlotAction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @deprecated use new v2 package
 */
@Deprecated
public class MenuManager {

    private final FusionPaper fusionPaper;
    private final Map<UUID, Menu.Status> statuses = new HashMap<>();

    public MenuManager(FusionPaper fusionPaper) {
        this.fusionPaper = fusionPaper;
    }

    public Optional<Menu.Status> getStatus(UUID player) {
        return Optional.ofNullable(this.statuses.get(player));
    }

    public void put(UUID uuid, Menu.Status status) {
        this.statuses.put(uuid, status);
    }

    public void remove(UUID uuid) {
        this.statuses.remove(uuid);
    }

    public static class MenuEventHandler implements Listener {

        @EventHandler
        public void onDrag(InventoryDragEvent event) {
            MenuManager menuManager = FusionPaper.getMenuManager();
            Player player = (Player) event.getWhoClicked();
            UUID uuid = player.getUniqueId();

            if (event.getInventory() instanceof PlayerInventory) {
                return;
            }

            menuManager.getStatus(uuid).ifPresent(status -> {
                Menu menu = status.menu();
                Optional<Page> possiblePage = menu.getPage(status.currentPageIndex());
                if (possiblePage.isEmpty()) {
                    return;
                }
                Page page = possiblePage.get();

                for (Integer slot : event.getInventorySlots()) {
                    SlotFlag slotFlag = page.getSlotFlag(slot).orElse(null);
                    if (slotFlag != SlotFlag.PLACE_INTO && slotFlag != SlotFlag.TAKE_AND_PLACE) {
                        event.setCancelled(true);
                        return;
                    }
                }

                new BukkitRunnable(){
                    @Override
                    public void run() {
                        page.getSlotAction().onSlotAction(new SlotAction.ActionInfo(player, menu, page, 0, null, event.getInventory()));
                    }
                }.runTask(menuManager.fusionPaper);
            });
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            MenuManager menuManager = FusionPaper.getMenuManager();
            UUID player = event.getPlayer().getUniqueId();
            menuManager.getStatus(player).ifPresent(status -> {
                status.menu().getCloseFunction().onClose(new MenuCloseFunction.CloseInfo(player, status.menu(), status.currentPageIndex(), event.getInventory()));
                menuManager.remove(player);
            });
        }

        @EventHandler
        public void onInteract(InventoryClickEvent event) {

            MenuManager menuManager = FusionPaper.getMenuManager();
            Player player = (Player) event.getWhoClicked();
            UUID uuid = player.getUniqueId();

            menuManager.getStatus(uuid).ifPresent(status -> {

                Menu menu = status.menu();
                Optional<Page> possiblePage = menu.getPage(status.currentPageIndex());
                if (possiblePage.isEmpty()) {
                    return;
                }
                Page page = possiblePage.get();

                ItemStack clicked = event.getCurrentItem();
                int slot = event.getSlot();
                ItemStack cursor = event.getCursor();

                if (clicked != null) {
                    if (Button.isButton(clicked)) {
                        page.getButton(slot).ifPresent(button -> {
                            button.getFunction().onClick(new ButtonFunction.ClickInfo(menu, page, status.currentPageIndex(), button, event.getClick(), player));
                        });
                        event.setCancelled(true);
                        return;
                    }
                }

                switch (event.getAction()) {
                    case NOTHING, DROP_ALL_CURSOR, DROP_ONE_CURSOR, CLONE_STACK -> {
                        // dont care
                    }
                    case PICKUP_ALL, PICKUP_SOME, PICKUP_HALF, PICKUP_ONE, DROP_ALL_SLOT, DROP_ONE_SLOT -> {

                        if (event.getClickedInventory() instanceof PlayerInventory) {
                            return;
                        }

                        // take from
                        SlotFlag slotFlag = page.getSlotFlag(slot).orElse(null);
                        if (slotFlag != SlotFlag.TAKE_FROM && slotFlag != SlotFlag.TAKE_AND_PLACE) {
                            event.setCancelled(true);
                            return;
                        }

                        if (cursor != null && cursor.getType() != Material.AIR) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    page.getSlotAction().onSlotAction(new SlotAction.ActionInfo(player, menu, page, slot, cursor, event.getClickedInventory()));
                                }
                            }.runTask(menuManager.fusionPaper);
                        } else if (clicked != null && clicked.getType() != Material.AIR) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    page.getSlotAction().onSlotAction(new SlotAction.ActionInfo(player, menu, page, slot, clicked, event.getClickedInventory()));
                                }
                            }.runTask(menuManager.fusionPaper);
                        }
                    }
                    case PLACE_ALL, PLACE_SOME, PLACE_ONE -> {

                        if (event.getClickedInventory() instanceof PlayerInventory) {
                            return;
                        }

                        // place into
                        SlotFlag slotFlag = page.getSlotFlag(slot).orElse(null);
                        if (slotFlag != SlotFlag.PLACE_INTO && slotFlag != SlotFlag.TAKE_AND_PLACE) {
                            event.setCancelled(true);
                            return;
                        }

                        if (cursor != null) {
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    page.getSlotAction().onSlotAction(new SlotAction.ActionInfo(player, menu, page, slot, cursor, event.getClickedInventory()));
                                }
                            }.runTask(menuManager.fusionPaper);
                        } else if (clicked != null) {
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    page.getSlotAction().onSlotAction(new SlotAction.ActionInfo(player, menu, page, slot, clicked, event.getClickedInventory()));
                                }
                            }.runTask(menuManager.fusionPaper);
                        }
                    }
                    case SWAP_WITH_CURSOR, HOTBAR_SWAP -> {
                        // take from and place into
                        SlotFlag slotFlag = page.getSlotFlag(slot).orElse(null);
                        if (slotFlag != SlotFlag.TAKE_AND_PLACE) {
                            event.setCancelled(true);
                        }
                    }
                    case MOVE_TO_OTHER_INVENTORY, UNKNOWN, HOTBAR_MOVE_AND_READD, COLLECT_TO_CURSOR -> event.setCancelled(true);
                }
            });
        }
    }
}
