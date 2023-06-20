package com.projecki.fusion.menu.deprecated;

import com.projecki.fusion.item.ItemBuilder;
import com.projecki.fusion.menu.deprecated.function.SlotAction;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @deprecated use new v2 package
 */
@Deprecated
public class Page {

    private static final ItemStack FILLER = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(Component.text("")).build();

    private int size;
    private Component title;
    /** Buttons in the inventory to be rendered on render */
    private final Button[] buttons = new Button[54];
    /** The items that aren't buttons to be rendered in this inventory on render */
    private final ItemStack[] items = new ItemStack[54];
    private final SlotFlag[] slotFlags = new SlotFlag[54];
    private SlotAction slotAction = ((info) -> {});

    public Page(int size, Component title) {
        this.size = Math.max(0, Math.min(size, 54));
        this.title = title;
    }

    public final SlotAction getSlotAction() {
        return slotAction;
    }

    public final void setSlotAction(SlotAction slotAction) {
        this.slotAction = slotAction;
    }

    public final int getSize() {
        return size;
    }

    public final Component getTitle() {
        return title;
    }

    public final void setSize(int size) {
        this.size = size;
    }

    public final void setTitle(Component title) {
        this.title = title;
    }
    public final Button[] getButtons() {
        return Arrays.copyOf(this.buttons, this.buttons.length);
    }

    public final Optional<Button> getButton(int slot) {
        Button b = null;
        try {
            b = this.buttons[slot];
        } catch (ArrayIndexOutOfBoundsException ignored) {}
        return Optional.ofNullable(b);
    }

    public final void setButton(int slot, Button button) {
        if (slot < 54 && slot >= 0) {
            this.buttons[slot] = button;
        }
    }

    public final void removeButton(int slot) {
        this.setButton(slot, null);
    }

    public final ItemStack[] getItems() {
        return Arrays.copyOf(this.items, this.items.length);
    }

    public final Optional<ItemStack> getItem(int slot) {
        ItemStack b = null;
        try {
            b = this.items[slot];
        } catch (ArrayIndexOutOfBoundsException ignored) {}
        return Optional.ofNullable(b);
    }

    public final void setItem(int slot, ItemStack item) {
        if (slot < 54 && slot >= 0) {
            this.items[slot] = item;
        }
    }

    public final void removeItem(int slot) {
        this.setItem(slot, null);
    }

    public final SlotFlag[] getSlotFlags() {
        return Arrays.copyOf(this.slotFlags, this.slotFlags.length);
    }

    public final Optional<SlotFlag> getSlotFlag(int slot) {
        SlotFlag b = null;
        try {
            b = this.slotFlags[slot];
        } catch (ArrayIndexOutOfBoundsException ignored) {}
        return Optional.ofNullable(b);
    }

    public final void setSlotFlag(SlotFlag slotFlag, int... slots) {
        for (int slot : slots) {
            if (slot < 54 && slot >= 0) {
                this.slotFlags[slot] = slotFlag;
            }
        }
    }

    public final void removeSlotFlag(int... slots) {
        this.setSlotFlag(null, slots);
    }

    /**
     * Fill given slots with a given filler item. Buttons will override filler.
     *
     * @param filler The filler item to use
     * @param slots The slots to fill
     */
    public final void fill(ItemStack filler, Integer... slots) {
        for (int slot : slots) {
            this.setItem(slot, filler);
        }
    }

    /**
     * Fills given slots with the default filler. Buttons will override filler.
     *
     * @param slots The slots to fill
     */
    public final void fill(Integer... slots) {
        this.fill(FILLER, slots);
    }

    /**
     * Fills all slots except the given ones. Buttons will override filler.
     *
     * @param filler The filler item to use
     * @param exclude The slots to exclude from filling
     */
    public final void fillAllExcept(ItemStack filler, Integer... exclude) {
        List<Integer> non = List.of(exclude);
        List<Integer> slotsList = IntStream.rangeClosed(0, this.size - 1).boxed().collect(Collectors.toList());
        slotsList.removeAll(non);
        this.fill(filler, slotsList.toArray(Integer[]::new));
    }

    /**
     * Fills all slots except the given ones. Buttons will override filler.
     *
     * @param exclude The slots to exclude from filling
     */
    public final void fillAllExcept(Integer... exclude) {
        this.fillAllExcept(FILLER, exclude);
    }

    /**
     * Fills all slots except a rect specified by the parameters.
     * E.g. calling {@code Page.fillAllExcept(1, 1, 2, 2);} will fill all slots,
     * except {@code [10, 11, 19, 20]}.
     *
     * @param x      the x coordinate of the start slot
     * @param y      the y coordinate of the start slot
     * @param width  the width of the rect
     * @param height the height of the rect
     */
    public final void fillAllExcept(int x, int y, int width, int height) {
        fillAllExcept(IntStream
                .range(y, y + height) // range from y -> y + height
                .map(i -> i * 9) // multiply by 9 to get the indices
                .flatMap(i -> // flat map it to compute all the row coordinates
                        IntStream.range(x, x + width) // range from x -> x + width (row width)
                                .map(j -> i + j))  // simply add each row offset to the base row slot index
                .boxed()
                .toArray(Integer[]::new));
    }
}
