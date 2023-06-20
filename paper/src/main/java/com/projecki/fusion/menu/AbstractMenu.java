package com.projecki.fusion.menu;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.item.ItemBuilder;
import com.projecki.fusion.menu.button.Button;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractMenu {

    private final List<Page> pages = new ArrayList<>();

    /**
     * Renders this menu for the given collection of players at the given page index and 'registers' them as
     * viewers of this menu
     *
     * @param players The players to render this menu to
     * @param pageIndex The index of page to render
     */
    public void render(Collection<? extends Player> players, int pageIndex) {
        this.getPage(pageIndex).ifPresent(page -> {
            for (Player player : players) {
                FusionPaper.getMenuViewInteractManager().put(player.getUniqueId(), new MenuViewStatus(this, pageIndex));
                this.renderOntoPageInventory(pageIndex, page.getInventory());
                player.openInventory(page.getInventory());
            }
        });
    }

    /**
     * Renders this menu for the given player at the given page index and 'registers' them as a viewer of this menu
     *
     * @param player The player to render this menu to
     * @param pageIndex The index of page to render
     */
    public void render(Player player, int pageIndex) {
        this.render(new ArrayList<>(Collections.singleton(player)), pageIndex);
    }

    /**
     * Renders this menu's first page for the given player and 'registers' them as a viewer of this menu
     *
     * @param player The player to render this menu to
     */
    public void render(Player player) {
        this.render(player, 0);
    }

    /**
     * Renders this menu's first page for the given players and 'registers' them as viewers of this menu
     *
     * @param players The collection of players to render this menu to
     */
    public void render(Collection<? extends Player> players) {
        this.render(players, 0);
    }

    /**
     * Sends an updated version of the menu to each of its viewers, effectively re-rendering it with its new state
     */
    public void sendUpdate() {
        // update inventories
        for (Map.Entry<UUID, MenuViewStatus> entry : FusionPaper.getMenuViewInteractManager().getMenuViewStatuses().entrySet()) {
            UUID uuid = entry.getKey();
            MenuViewStatus viewStatus = entry.getValue();
            AbstractMenu menu = viewStatus.menu();
            int pageIndex = viewStatus.pageIndex();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !menu.equals(this)) {
                continue;
            }
            this.renderOntoPageInventory(pageIndex, player.getOpenInventory().getTopInventory());
            player.updateInventory();
        }
    }

    private void renderOntoPageInventory(int pageIndex, Inventory inv) {
        this.getPage(pageIndex).ifPresent(page -> {

            boolean hasMultiple = pages.size() > 1;

            boolean hasPrevious = pageIndex > 0;
            this.getPreviousPageItems(page, pageIndex).forEach((i, is) -> {
                if (hasPrevious) {
                    page.setButton(new Button(is, info -> {
                        FusionPaper.getMenuViewInteractManager().getMenuView(info.player().getUniqueId()).ifPresent(status -> {
                            info.menu().render(info.player(), status.pageIndex() - 1);
                        });
                    }), i);
                } else if (hasMultiple) {
                    page.setItem(this.getNoPreviousPageItem(), i);
                }
            });

            boolean hasNext = pageIndex < pages.size() - 1;
            this.getNextPageItems(page, pageIndex).forEach((i, is) -> {
                if (hasNext) {
                    page.setButton(new Button(is, info -> {
                        FusionPaper.getMenuViewInteractManager().getMenuView(info.player().getUniqueId()).ifPresent(status -> {
                            info.menu().render(info.player(), status.pageIndex() + 1);
                        });
                    }), i);
                } else if (hasMultiple) {
                    page.setItem(this.getNoNextPageItem(), i);
                }
            });

            /*
            Buttons will always override items. The only case this is false is when a button is null. Buttons in the array
            that aren't set (are null) will NOT override an item in the same spot (obviously)
             */
            for (int i = 0; i < (page.getRows() * 9); i++) {
                ItemStack item = page.getItems()[i];
                Button button = page.getButtons()[i];

                ItemStack toSet = new ItemStack(Material.AIR);
                if (item != null) {
                    toSet = item;
                }
                if (button != null && button.getItem() != null) {
                    toSet = button.getItem();
                }

                inv.setItem(i, toSet);
            }
        });
    }

    /**
     * Adds a page to the end of the list of pages in this menu
     *
     * @param page The page to append
     */
    public final void addPage(Page page) {
        this.pages.add(page);
        page.attachParent(this);
    }

    /**
     * Returns a possible page at the given index. If no page is found an empty optional is returned.
     *
     * @param index The index to get the page of (start at 0)
     * @return A possible page at the given index
     */
    public final Optional<Page> getPage(int index) {
        Page found = null;
        try {
            found = this.pages.get(index);
        } catch (ArrayIndexOutOfBoundsException ignored) {}
        return Optional.ofNullable(found);
    }

    /**
     * @return A copy of the current pages set in this menu
     */
    public final List<Page> getPages() {
        return new ArrayList<>(this.pages);
    }

    /**
     * Removes a page from the list of pages in this menu if it can be found.
     *
     * @param page The index of page to be removed (indexed at 0)
     * @return An optional containing the page removed, empty if the index was out of bounds or page was null
     */
    public final Optional<Page> removePage(int page) {
        if (page >= 0 && page < this.pages.size()) {
            return Optional.ofNullable(this.pages.get(page));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Clears the current list of pages and adds all the passed in pages.
     *
     * @param pages The new list of pages to set in this menu
     */
    public final void setPages(List<Page> pages) {
        this.pages.clear();
        this.pages.addAll(pages);
    }

    /**
     * A function called when this menu is closed for ANY reason other than a new one being opened on top of it. Override
     * this to apply functionality when a menu is closed
     *
     * @param player The player involved
     * @param pageIndex The page index at the time of closure
     * @param inventory The inventory object of that page
     */
    public void onClose(UUID player, int pageIndex, Inventory inventory) {}

    /**
     * A function that returns a map of integers to items representing the slot and items of the buttons that will
     * function as next page buttons. This map is just a map of the slot the button will be and what item it will be.
     * Rendering is automatic and the functions are already pre-programmed. Ideally all the items in the map will
     * be the same.
     *
     * @param page The page these buttons will go on
     * @param pageIndex The index of the page these buttons will go on
     * @return A map from slot to item representing the slot and item in its place of the next page buttons.
     */
    public Map<Integer, ItemStack> getNextPageItems(Page page, int pageIndex) {
        Map<Integer, ItemStack> items = new HashMap<>();
        items.put(page.getRows() * 9 - 1, ItemBuilder.of(Material.ARROW).name(Component.text("Next")).build());
        return items;
    }

    /**
     * Returns the item to represent when no future page is present. This item will be in place of the slots given
     * from the {@code getNextPageItems()} function
     *
     * @return The item to represent when no future page is present
     */
    public ItemStack getNoNextPageItem() {
        return ItemBuilder.of(Material.AIR).build();
    }

    /**
     * A function that returns a map of integers to items representing the slot and items of the buttons that will
     * function as previous page buttons. This map is just a map of the slot the button will be and what item it will be.
     * Rendering is automatic and the functions are already pre-programmed
     *
     * @param page The page these buttons will go on
     * @param pageIndex The index of the page these buttons will go on
     * @return A map from slot to item representing the slot and item in its place of the previous page buttons.
     */
    public Map<Integer, ItemStack> getPreviousPageItems(Page page, int pageIndex) {
        Map<Integer, ItemStack> items = new HashMap<>();
        items.put(page.getRows() * 9 - 9, ItemBuilder.of(Material.ARROW).name(Component.text("Previous")).build());
        return items;
    }

    /**
     * Returns the item to represent when no previous page is present. This item will be in place of the slots given
     * from the {@code getPreviousPageItems()} function
     *
     * @return The item to represent when no previous page is present
     */
    public ItemStack getNoPreviousPageItem() {
        return ItemBuilder.of(Material.AIR).build();
    }
}
