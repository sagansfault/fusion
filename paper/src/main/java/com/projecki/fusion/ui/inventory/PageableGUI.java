package com.projecki.fusion.ui.inventory;

import com.projecki.fusion.ui.inventory.icon.Icon;
import com.projecki.fusion.ui.inventory.icon.Icons;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.kyori.adventure.text.Component.text;

/**
 * @since May 04, 2022
 * @author Andavin
 */
public abstract class PageableGUI<T> extends GUI {

    private int page;
    protected final int maxPageSize;
    private final int backSlot, nextSlot;

    /**
     * Create a new PageableGUI of the specified size of the type
     * {@link InventoryType#CHEST}.
     *
     * @param title The title of the GUI.
     * @param size The size of the GUI to create.
     * @param backSlot The slot in which to place the back
     *                 arrow {@link Icon} when applicable.
     * @param nextSlot The slot in which to place the next
     *                 arrow {@link Icon} when applicable.
     * @param maxPageSize The maximum size of each page.
     */
    public PageableGUI(String title, int size, int backSlot, int nextSlot, int maxPageSize) {
        super(title, size);
        this.backSlot = backSlot;
        this.nextSlot = nextSlot;
        this.maxPageSize = maxPageSize;
    }

    /**
     * Create a new PageableGUI of the specified {@link InventoryType} and
     * using the {@link InventoryType#getDefaultSize() default size}.
     *
     * @param title The title of the GUI.
     * @param type The type of GUI to create.
     * @param backSlot The slot in which to place the back
     *                 arrow {@link Icon} when applicable.
     * @param nextSlot The slot in which to place the next
     *                 arrow {@link Icon} when applicable.
     * @param maxPageSize The maximum size of each page.
     */
    public PageableGUI(String title, InventoryType type, int backSlot, int nextSlot, int maxPageSize) {
        super(title, type);
        this.backSlot = backSlot;
        this.nextSlot = nextSlot;
        this.maxPageSize = maxPageSize;
    }

    /**
     * Create a new PageableGUI of the specified {@link InventoryType} and
     * of the specified size.
     * <p>
     * <b>Note</b>: Be careful with adjusting the size on certain
     * types of inventories (e.g. {@link InventoryType#ANVIL}) as
     * they may either throw errors/crash the client if the size
     * is not the default or just simply not work.
     * <br>
     * In addition, some inventory types may or may not open at all
     * since they are not made for the server to tell the client to open:
     * <ul>
     *     <li>{@link InventoryType#CRAFTING}</li>
     *     <li>{@link InventoryType#CREATIVE}</li>
     *     <li>{@link InventoryType#MERCHANT}</li>
     * </ul>
     * Behavior when attempting to use these types is completely
     * undefined from this API, and it is recommended not to use them.
     *
     * @param title The title of the GUI.
     * @param type The type of GUI to create.
     * @param size The size of the GUI to create.
     * @param backSlot The slot in which to place the back
     *                 arrow {@link Icon} when applicable.
     * @param nextSlot The slot in which to place the next
     *                 arrow {@link Icon} when applicable.
     * @param maxPageSize The maximum size of each page.
     */
    public PageableGUI(String title, InventoryType type, int size, int backSlot, int nextSlot, int maxPageSize) {
        super(title, type, size);
        this.backSlot = backSlot;
        this.nextSlot = nextSlot;
        this.maxPageSize = maxPageSize;
    }

    /**
     * Create a new PageableGUI of the specified size of the type
     * {@link InventoryType#CHEST}.
     *
     * @param title The title of the GUI.
     * @param size The size of the GUI to create.
     * @param backSlot The slot in which to place the back
     *                 arrow {@link Icon} when applicable.
     * @param nextSlot The slot in which to place the next
     *                 arrow {@link Icon} when applicable.
     * @param maxPageSize The maximum size of each page.
     */
    public PageableGUI(Component title, int size, int backSlot, int nextSlot, int maxPageSize) {
        super(title, size);
        this.backSlot = backSlot;
        this.nextSlot = nextSlot;
        this.maxPageSize = maxPageSize;
    }

    /**
     * Create a new PageableGUI of the specified {@link InventoryType} and
     * using the {@link InventoryType#getDefaultSize() default size}.
     *
     * @param title The title of the GUI.
     * @param type The type of GUI to create.
     * @param backSlot The slot in which to place the back
     *                 arrow {@link Icon} when applicable.
     * @param nextSlot The slot in which to place the next
     *                 arrow {@link Icon} when applicable.
     * @param maxPageSize The maximum size of each page.
     */
    public PageableGUI(Component title, InventoryType type, int backSlot, int nextSlot, int maxPageSize) {
        super(title, type);
        this.backSlot = backSlot;
        this.nextSlot = nextSlot;
        this.maxPageSize = maxPageSize;
    }

    /**
     * Create a new PageableGUI of the specified {@link InventoryType} and
     * of the specified size.
     * <p>
     * <b>Note</b>: Be careful with adjusting the size on certain
     * types of inventories (e.g. {@link InventoryType#ANVIL}) as
     * they may either throw errors/crash the client if the size
     * is not the default or just simply not work.
     * <br>
     * In addition, some inventory types may or may not open at all
     * since they are not made for the server to tell the client to open:
     * <ul>
     *     <li>{@link InventoryType#CRAFTING}</li>
     *     <li>{@link InventoryType#CREATIVE}</li>
     *     <li>{@link InventoryType#MERCHANT}</li>
     * </ul>
     * Behavior when attempting to use these types is completely
     * undefined from this API, and it is recommended not to use them.
     *
     * @param title The title of the GUI.
     * @param type The type of GUI to create.
     * @param size The size of the GUI to create.
     * @param backSlot The slot in which to place the back
     *                 arrow {@link Icon} when applicable.
     * @param nextSlot The slot in which to place the next
     *                 arrow {@link Icon} when applicable.
     * @param maxPageSize The maximum size of each page.
     */
    public PageableGUI(Component title, InventoryType type, int size, int backSlot, int nextSlot, int maxPageSize) {
        super(title, type, size);
        this.backSlot = backSlot;
        this.nextSlot = nextSlot;
        this.maxPageSize = maxPageSize;
    }

    /**
     * Create a new PageableGUI from the specified {@link Builder}.
     *
     * @param builder The {@link Builder} to use.
     */
    public PageableGUI(Builder builder) {
        super(builder.title, builder.type, builder.size);
        this.page = builder.page;
        this.backSlot = builder.backSlot;
        this.nextSlot = builder.nextSlot;
        this.maxPageSize = builder.maxPageSize;
    }

    @Override
    protected final void populate(Player player) {

        List<T> items = this.getItems(player);
        int size = items.size();
        int start = page * maxPageSize;
        int end = Math.min(size, start + maxPageSize);
        this.populate(player, items, start, end);
        if (page > 0) {
            this.set(backSlot, Icons.BACK_ARROW.action(ClickType.LEFT, click -> {
                this.page--;
                this.populate();
                click.playSound(Sound.UI_BUTTON_CLICK);
            }));
        } else {
            this.set(backSlot, this.blankBackSlotIcon());
        }

        if (end < size) {
            this.set(nextSlot, Icons.NEXT_ARROW.action(ClickType.LEFT, click -> {
                this.page++;
                this.populate();
                click.playSound(Sound.UI_BUTTON_CLICK);
            }));
        } else {
            this.set(nextSlot, this.blankNextSlotIcon());
        }
    }

    /**
     * The {@link Icon} that should be placed in the back
     * arrow slot when the back arrow itself is not present.
     * <p>
     *     This should be overridden to replace the value.
     * </p>
     *
     * @return The back slot filler.
     */
    protected Icon blankBackSlotIcon() {
        return null;
    }

    /**
     * The {@link Icon} that should be placed in the next
     * arrow slot when the next arrow itself is not present.
     * <p>
     *     This should be overridden to replace the value.
     * </p>
     *
     * @return The next slot filler.
     */
    protected Icon blankNextSlotIcon() {
        return null;
    }

    /**
     * @return The current page this GUI is on
     */
    protected final int getPage() {
        return this.page;
    }

    /**
     * Collect all the items that should be populated in this GUI.
     *
     * @param player The {@link Player} that the items are being populated for.
     * @return The list of items to populate.
     */
    protected abstract List<T> getItems(Player player);

    /**
     * Populate a page of this GUI with items.
     * <p>
     * For example, items could be iterated:
     * <pre>
     *     for (int i = start, slot = 0; i < end; i++, slot++) {
     *          this.set(slot, this.createIconFromItem(items.get(i)));
     *     }
     * </pre>
     *
     * @param player The {@link Player} to populate items for.
     * @param items The items to populate.
     * @param start The item index to start from.
     * @param end The item index to end at.
     */
    protected abstract void populate(Player player, List<T> items, int start, int end);

    public static class Builder {

        private int size;
        private Component title;
        private InventoryType type;

        private int page;
        private int maxPageSize;
        private int backSlot, nextSlot;

        /**
         * The title of the {@link PageableGUI}.
         *
         * @param title The title.
         * @return This {@link Builder}.
         */
        public Builder title(String title) {
            return this.title(text(checkNotNull(title, "GUI title")));
        }

        /**
         * The title of the {@link PageableGUI}.
         *
         * @param title The {@link Component title}.
         * @return This {@link Builder}.
         */
        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        /**
         * The {@link GUI#size() size} of the {@link PageableGUI}.
         *
         * @param size The size to set to.
         * @return This {@link Builder}.
         */
        public Builder size(int size) {
            this.size = size;
            return this;
        }

        /**
         * The {@link InventoryType} of the {@link PageableGUI}.
         *
         * @param type The {@link InventoryType}.
         * @return This {@link Builder}.
         */
        public Builder type(InventoryType type) {
            this.type = type;
            return this;
        }

        /**
         * The page index at which the {@link PageableGUI} should
         * initially start at.
         * <p>
         *     Default: {@code 0}
         * </p>
         *
         * @param pageIndex The page index to start at.
         * @return This {@link Builder}.
         */
        public Builder initialPageIndex(int pageIndex) {
            checkArgument(pageIndex >= 0, "invalid page: %s", pageIndex);
            this.page = pageIndex;
            return this;
        }

        /**
         * The maximum page size of the {@link PageableGUI}.
         * In other words, the maximum amount of items that will
         * be displayed on a single page.
         *
         * @param maxPageSize The maximum page size.
         * @return This {@link Builder}.
         */
        public Builder maxPageSize(int maxPageSize) {
            this.maxPageSize = maxPageSize;
            return this;
        }

        /**
         * The index slot within which the back arrow {@link Icon}
         * will be placed when applicable.
         * <p>
         *     For example, this is applicable when the {@link PageableGUI}
         *     is on a page greater than {@code 0} (the first page).
         * </p>
         *
         * @param backSlot The slot index to place the back arrow.
         * @return This {@link Builder}.
         */
        public Builder backSlot(int backSlot) {
            this.backSlot = backSlot;
            return this;
        }

        /**
         * The index slot within which the next arrow {@link Icon}
         * will be placed when applicable.
         * <p>
         *     For example, this is applicable when the {@link PageableGUI}
         *     has more items than can fit on the current and previous pages.
         * </p>
         *
         * @param nextSlot The slot index to place the next arrow.
         * @return This {@link Builder}.
         */
        public Builder nextSlot(int nextSlot) {
            this.nextSlot = nextSlot;
            return this;
        }
    }
}
