package com.projecki.fusion.menu.deprecated;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.item.ItemBuilder;
import com.projecki.fusion.menu.deprecated.function.ButtonFunction;
import com.projecki.fusion.menu.deprecated.function.MenuCloseFunction;
import com.projecki.fusion.menu.deprecated.function.RenderPaginationButtonFunction;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @deprecated use new v2 package
 */
@Deprecated

public class Menu {

    private static final ButtonFunction BACK_PAGE_FUNCTION = info -> {
        FusionPaper.getMenuManager().getStatus(info.player().getUniqueId()).ifPresent(status -> {
            info.menu().render(info.player(), status.currentPageIndex - 1);
        });
    };

    private static final ButtonFunction NEXT_PAGE_FUNCTION = info -> {
        FusionPaper.getMenuManager().getStatus(info.player().getUniqueId()).ifPresent(status -> {
            info.menu().render(info.player(), status.currentPageIndex + 1);
        });
    };

    private static final ItemStack DEFAULT_BACK_PAGE_ITEM = ItemBuilder.of(Material.ARROW).name(Component.text("Previous")).build();
    private static final ItemStack DEFAULT_NEXT_PAGE_ITEM = ItemBuilder.of(Material.ARROW).name(Component.text("Next")).build();

    private ItemStack noBackPageItem = ItemBuilder.of(Material.AIR).build();
    private ItemStack noNextPageItem = ItemBuilder.of(Material.AIR).build();

    private final List<Page> pages = new ArrayList<>();
    private MenuCloseFunction closeFunction = ((info) -> {});
    private RenderPaginationButtonFunction.NextPage renderNextPageButtonFunction = info -> {
        Map<Integer, ItemStack> map = new HashMap<>();
        map.put(info.page().getSize() - 1, DEFAULT_NEXT_PAGE_ITEM);
        return map;
    };
    private RenderPaginationButtonFunction.BackPage renderBackPageButtonFunction = info -> {
        Map<Integer, ItemStack> map = new HashMap<>();
        map.put(info.page().getSize() - 9, DEFAULT_BACK_PAGE_ITEM);
        return map;
    };

    public Menu(Page... pages) {
        this.pages.addAll(List.of(pages));
    }

    public void setNoBackPageItem(ItemStack item) {
        this.noBackPageItem = item;
    }

    public void setNoNextPageItem(ItemStack item) {
        this.noNextPageItem = item;
    }

    /**
     * Sets the function to be run to generate how to render the next-page buttons.
     *
     * @param renderNextPageButtonFunction The function to run.
     */
    public final void renderNextPageButtons(RenderPaginationButtonFunction.NextPage renderNextPageButtonFunction) {
        this.renderNextPageButtonFunction = renderNextPageButtonFunction;
    }

    /**
     * Sets the function to be run to generate how to render the back-page buttons.
     *
     * @param renderBackPageButtonFunction The function to run.
     */
    public final void renderBackPageButtons(RenderPaginationButtonFunction.BackPage renderBackPageButtonFunction) {
        this.renderBackPageButtonFunction = renderBackPageButtonFunction;
    }

    /**
     * Sets the function to run when this menu is closed by the player themselves. All reasons/triggers apply with the
     * exclusion of {@link org.bukkit.event.inventory.InventoryCloseEvent.Reason}: {@code PLUGIN, OPEN_NEW}
     *
     * @param closeFunction The function to run when the inventory is closed.
     */
    public final void onClose(MenuCloseFunction closeFunction) {
        this.closeFunction = closeFunction;
    }

    public final MenuCloseFunction getCloseFunction() {
        return closeFunction;
    }

    public final List<Page> getPages() {
        return new ArrayList<>(this.pages);
    }

    public final Optional<Page> getPage(int index) {
        Page page = null;
        try {
            page = this.pages.get(index);
        } catch (IndexOutOfBoundsException ignored) {}
        return Optional.ofNullable(page);
    }

    public final Optional<Page> getFirstPage() {
        return this.getPage(0);
    }

    public final Optional<Page> getLastPage() {
        return this.getPage(this.pages.size() - 1);
    }

    public final void placePage(int index, Page page) {
        this.pages.add(Math.min(pages.size(), Math.max(index, 0)), page);
    }

    public final void addPage(Page page) {
        this.pages.add(page);
    }

    public final void addPages(Page... pages) {
        this.pages.addAll(Arrays.asList(pages));
    }

    public final void render(Player player) {
        this.render(player, 0);
    }

    /**
     * Renders a given page in this menu for the player. Next and back page buttons are automatically added
     *
     * @param player The player to render the page for
     * @param pageIndex The index of the page to render (indexes start at 0)
     */
    public final void render(Player player, int pageIndex) {
        if (pageIndex < 0 || pageIndex >= pages.size()) {
            return;
        }
        Page page = this.pages.get(pageIndex);
        Inventory inventory = Bukkit.createInventory(null, page.getSize(), page.getTitle());

        // back page button rendering
        if (pages.size() > 1 && pageIndex > 0) {
            this.renderBackPageButtonFunction.getBackPageButton(
                    new RenderPaginationButtonFunction.FunctionInfo(player, this, page, pageIndex)
            ).forEach((i, is) -> page.setButton(i, new Button(is, BACK_PAGE_FUNCTION)));
        } else {
            this.renderBackPageButtonFunction.getBackPageButton(
                    new RenderPaginationButtonFunction.FunctionInfo(player, this, page, pageIndex)
            ).forEach((i, is) -> page.setItem(i, noBackPageItem));
        }

        // next page button rendering
        if (pages.size() > 1 && pageIndex < pages.size() - 1) {
            this.renderNextPageButtonFunction.getNextPageButton(
                    new RenderPaginationButtonFunction.FunctionInfo(player, this, page, pageIndex)
            ).forEach((i, is) -> page.setButton(i, new Button(is, NEXT_PAGE_FUNCTION)));
        } else {
            this.renderNextPageButtonFunction.getNextPageButton(
                    new RenderPaginationButtonFunction.FunctionInfo(player, this, page, pageIndex)
            ).forEach((i, is) -> page.setItem(i, noNextPageItem));
        }


        // these must always be called last, after any modifications have been made to the button and item array
        for (int i = 0; i < page.getItems().length; i++) {
            if (i < page.getSize() && page.getItems()[i] != null) {
                inventory.setItem(i, page.getItems()[i]);
            }
        }
        for (int i = 0; i < page.getButtons().length; i++) {
            if (i < page.getSize() && page.getButtons()[i] != null) {
                inventory.setItem(i, page.getButtons()[i].getItem());
            }
        }

        player.openInventory(inventory);
        player.updateInventory();
        FusionPaper.getMenuManager().put(player.getUniqueId(), new Menu.Status(this, pageIndex));
    }

    /**
     * Re-render's the current page the given player is viewing if at all.
     *
     * @param player The player to re-render the current page for
     */
    public final void reRenderCurrentPage(Player player) {
        FusionPaper.getMenuManager().getStatus(player.getUniqueId()).ifPresent(status -> {
            status.menu.render(player, status.currentPageIndex);
        });
    }

    public static record Status(Menu menu, int currentPageIndex) {}
}
