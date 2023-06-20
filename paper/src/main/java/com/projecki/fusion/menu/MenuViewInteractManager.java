package com.projecki.fusion.menu;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.menu.button.Button;
import com.projecki.fusion.menu.button.ButtonFunction;
import com.projecki.fusion.menu.slot.SlotAction;
import com.projecki.fusion.menu.slot.SlotFlag;
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

public class MenuViewInteractManager {

    private final Map<UUID, MenuViewStatus> menuViewStatuses = new HashMap<>();

    public void put(UUID player, MenuViewStatus viewStatus) {
        this.menuViewStatuses.put(player, viewStatus);
    }

    public Optional<MenuViewStatus> getMenuView(UUID player) {
        return Optional.ofNullable(this.menuViewStatuses.get(player));
    }

    public Map<UUID, MenuViewStatus> getMenuViewStatuses() {
        return new HashMap<>(menuViewStatuses);
    }

    public void remove(UUID player) {
        this.menuViewStatuses.remove(player);
    }

    public static final class MenuEventHandler implements Listener {

        private final FusionPaper fusionPaper;

        public MenuEventHandler(FusionPaper fusionPaper) {
            this.fusionPaper = fusionPaper;
        }

        @EventHandler
        public void onDrag(InventoryDragEvent event) {
            MenuViewInteractManager manager = FusionPaper.getMenuViewInteractManager();
            Player player = (Player) event.getWhoClicked();
            UUID uuid = player.getUniqueId();

            if (event.getInventory() instanceof PlayerInventory) {
                return;
            }

            manager.getMenuView(uuid).ifPresent(status -> {
                AbstractMenu menu = status.menu();
                Optional<Page> possiblePage = menu.getPage(status.pageIndex());
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
                }.runTask(fusionPaper);
            });
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            MenuViewInteractManager manager = FusionPaper.getMenuViewInteractManager();
            UUID player = event.getPlayer().getUniqueId();
            manager.getMenuView(player).ifPresent(menuViewStatus -> {
                /*
                This check is here because changing from page to page renders and opens a different inventory than the
                current one. The closing of the previous menu will trigger a menu close even like this one. We only want
                to call this when they actually leave the menu.
                 */
                if (event.getReason() != InventoryCloseEvent.Reason.OPEN_NEW) {
                    menuViewStatus.menu().onClose(player, menuViewStatus.pageIndex(), event.getInventory());
                    manager.remove(player);
                }
            });
        }

        @EventHandler
        public void onInteract(InventoryClickEvent event) {
            MenuViewInteractManager manager = FusionPaper.getMenuViewInteractManager();
            Player player = (Player) event.getWhoClicked();
            UUID uuid = player.getUniqueId();

            manager.getMenuView(uuid).ifPresent(status -> {
                AbstractMenu menu = status.menu();
                Optional<Page> possiblePage = menu.getPage(status.pageIndex());
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
                            button.getFunction().onClick(new ButtonFunction.ClickInfo(menu, page, status.pageIndex(), button, event.getClick(), player));
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
                            }.runTask(fusionPaper);
                        } else if (clicked != null && clicked.getType() != Material.AIR) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    page.getSlotAction().onSlotAction(new SlotAction.ActionInfo(player, menu, page, slot, clicked, event.getClickedInventory()));
                                }
                            }.runTask(fusionPaper);
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
                            }.runTask(fusionPaper);
                        } else if (clicked != null) {
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    page.getSlotAction().onSlotAction(new SlotAction.ActionInfo(player, menu, page, slot, clicked, event.getClickedInventory()));
                                }
                            }.runTask(fusionPaper);
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
