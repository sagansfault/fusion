package com.projecki.fusion.menu;

import com.projecki.fusion.item.ItemBuilder;
import com.projecki.fusion.menu.button.Button;
import com.projecki.fusion.menu.slot.SlotAction;
import com.projecki.fusion.menu.slot.SlotFlag;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public class Page {

    private static final ItemStack FILLER = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(Component.text("")).build();

    private final Inventory inventory;
    private @Nullable AbstractMenu parent;

    private final int rows;
    private final Component title;
    private final Button[] buttons;
    private final ItemStack[] items;
    private final SlotFlag[] slotFlags;
    private SlotAction slotAction = ((info) -> {});

    public Page(int rows, Component title) {
        this.rows = Math.max(0, Math.min(6, rows));
        this.title = title;

        int slots = rows * 9;
        this.buttons = new Button[slots];
        this.items = new ItemStack[slots];
        this.slotFlags = new SlotFlag[slots];

        inventory = Bukkit.createInventory(null, this.rows * 9, this.title);
    }

    final void attachParent(AbstractMenu menu) {
        this.parent = menu;
    }

    final Inventory getInventory() {
        return this.inventory;
    }

    public final int getRows() {
        return rows;
    }

    public final Component getTitle() {
        return title;
    }

    public final SlotAction getSlotAction() {
        return slotAction;
    }

    public final void setSlotAction(SlotAction slotAction) {
        this.slotAction = slotAction;
    }

    public final Button[] getButtons() {
        return Arrays.copyOf(this.buttons, this.buttons.length);
    }

    public final ItemStack[] getItems() {
        return Arrays.copyOf(this.items, this.items.length);
    }

    public final SlotFlag[] getSlotFlags() {
        return Arrays.copyOf(this.slotFlags, this.slotFlags.length);
    }

    public final Optional<Button> getButton(int slot) {
        Button button = null;
        try {
            button = this.buttons[slot];
        } catch (ArrayIndexOutOfBoundsException ignored) {}
        return Optional.ofNullable(button);
    }

    public final Optional<ItemStack> getItem(int slot) {
        ItemStack item = null;
        try {
            item = this.items[slot];
        } catch (ArrayIndexOutOfBoundsException ignored) {}
        return Optional.ofNullable(item);
    }

    public final Optional<SlotFlag> getSlotFlag(int slot) {
        SlotFlag flag = null;
        try {
            flag = this.slotFlags[slot];
        } catch (ArrayIndexOutOfBoundsException ignored) {}
        return Optional.ofNullable(flag);
    }

    public final void setButton(Button button, boolean sendUpdate, int... slots) {
        int total = this.rows * 9;
        for (int slot : slots) {
            if (slot <= (total - 1) && slot >= 0) {
                this.buttons[slot] = button;
            }
        }
        if (slots.length != 0 && sendUpdate && parent != null) {
            parent.sendUpdate();
        }
    }

    public final void setItem(ItemStack item, boolean sendUpdate, int... slots) {
        int total = this.rows * 9;
        for (int slot : slots) {
            if (slot <= (total - 1) && slot >= 0) {
                this.items[slot] = item;
            }
        }
        if (slots.length != 0 && sendUpdate && parent != null) {
            parent.sendUpdate();
        }
    }

    public final void setSlotFlag(SlotFlag slotFlag, boolean sendUpdate, int... slots) {
        int total = this.rows * 9;
        for (int slot : slots) {
            if (slot <= (total - 1) && slot >= 0) {
                this.slotFlags[slot] = slotFlag;
            }
        }
        if (slots.length != 0 && sendUpdate && parent != null) {
            parent.sendUpdate();
        }
    }

    public final void setButton(Button button, int... slots) {
        this.setButton(button, false, slots);
    }

    public final void changeButtonItem(int slot, ItemStack newItem, boolean sendUpdate) {
        Button button = this.buttons[slot];
        if (button == null) {
            return;
        }

        Button clone = new Button(newItem, button.getFunction());
        this.buttons[slot] = clone;

        if (sendUpdate && parent != null) {
            parent.sendUpdate();
        }
    }

    public final void setItem(ItemStack item, int... slots) {
        this.setItem(item, false, slots);
    }

    public final void setSlotFlag(SlotFlag slotFlag, int... slots) {
        this.setSlotFlag(slotFlag, false, slots);
    }

    public final void removeButton(int... slots) {
        this.setButton(null, slots);
    }

    public final void removeItem(int... slots) {
        this.setItem(null, slots);
    }

    public final void removeSlotFlag(int... slots) {
        this.setSlotFlag(null, slots);
    }

    public final void clearButtons(boolean sendUpdate) {
        for (int i = 0; i < this.buttons.length; i++) {
            buttons[i] = null;
        }
        if (sendUpdate && parent != null) {
            parent.sendUpdate();
        }
    }

    public final void clearItems(boolean sendUpdate) {
        for (int i = 0; i < this.items.length; i++) {
            items[i] = null;
        }
        if (sendUpdate && parent != null) {
            parent.sendUpdate();
        }
    }

    public final void clearSlotFlags(boolean sendUpdate) {
        for (int i = 0; i < this.slotFlags.length; i++) {
            slotFlags[i] = null;
        }
        if (sendUpdate && parent != null) {
            parent.sendUpdate();
        }
    }

    public final void clearButtons() {
        this.clearButtons(false);
    }

    public final void clearItems() {
        this.clearItems(false);
    }

    public final void clearSlotFlags() {
        this.clearSlotFlags(false);
    }

    public final void fill(ItemStack filler, int... slots) {
        this.setItem(filler, slots);
    }

    public final void fill(int... slots) {
        this.fill(FILLER, slots);
    }

    public final void fillAllExcept(ItemStack filler, int... slots) {

        if (slots.length == 0) {
            for (int i = 0; i < (this.rows * 9); i++) {
                this.fill(i);
            }
            return;
        }

        int[] base = new int[this.rows * 9];
        for (int i = 0; i < base.length; i++) {
            base[i] = i;
        }

        for (int slot : slots) {
            if (slot <= (base.length - 1) && slot >= 0) {
                base[slot] = -1;
            }
        }

        int[] toFill = new int[base.length - slots.length + 1];
        int index = 0;
        for (int i : base) {
            if (i != -1) {
                toFill[index] = i;
                index++;
            }
        }

        this.fill(filler, toFill);
    }

    public final void fillAllExcept(int... slots) {
        this.fillAllExcept(FILLER, slots);
    }

    public final void fillAllExcept(int x, int y, int width, int height) {
        fillAllExcept(IntStream
                .range(y, y + height) // range from y -> y + height
                .map(i -> i * 9) // multiply by 9 to get the indices
                .flatMap(i -> // flat map it to compute all the row coordinates
                        IntStream.range(x, x + width) // range from x -> x + width (row width)
                                .map(j -> i + j))  // simply add each row offset to the base row slot index
                .toArray());
    }
}
