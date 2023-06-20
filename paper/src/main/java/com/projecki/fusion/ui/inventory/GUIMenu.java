package com.projecki.fusion.ui.inventory;

import com.projecki.fusion.ui.inventory.icon.Icon;
import com.projecki.fusion.ui.inventory.icon.IconAccess;
import com.projecki.fusion.ui.inventory.icon.click.Click;
import com.projecki.unversioned.window.Window;
import com.projecki.unversioned.window.WindowService;
import com.projecki.unversioned.window.slot.ClickMode;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntFunction;

import static com.google.common.base.Preconditions.*;

/**
 * An inventory used exclusively for handling {@code GUIs}.
 * Implementations are responsible for handling all packet
 * interaction with the player.
 *
 * @since April 09, 2022
 * @author Andavin
 */
public class GUIMenu implements Window {

    /**
     * The slot that can be sent in order to update
     * the cursor (i.e. carried) item in an inventory.
     */
    public static final int CURSOR_SLOT = -1;

    /**
     * The ID of the window that signifies the player's
     * own inventory window.
     */
    public static final int PLAYER_WINDOW_ID = 0;

    /**
     * The slot that will be given when a player clicked
     * outside of their inventory window.
     */
    public static final int OUTSIDE_WINDOW_SLOT = -999;

    /**
     * The size of a player's inventory when they have
     * another inventory (e.g. a chest) currently open.
     *
     * @see <a href="https://wiki.vg/Inventory">Inventory Wiki</a>
     */
    public static final int PLAYER_OPEN_INVENTORY_SIZE = 36;

    /**
     * The amount of slots that make up the player armor
     * and crafting inventory. These slots are not seen
     * when a player has another inventory open and are
     * not included in the index.
     *
     * @see <a href="https://wiki.vg/Inventory">Inventory Wiki</a>
     * @see <a href="https://wiki.vg/File:Inventory-slots.png">Player Inventory Image</a>
     */
    public static final int PLAYER_ARMOR_CRAFTING_SIZE = 9;

    /**
     * The slot in which the player holds their offhand item.
     *
     * @see <a href="https://wiki.vg/Inventory">Inventory Wiki</a>
     * @see <a href="https://wiki.vg/File:Inventory-slots.png">Player Inventory Image</a>
     */
    public static final int PLAYER_OFFHAND_SLOT = 45;

    private static final AtomicLong INCREMENTAL_ID = new AtomicLong();
    private final Object lock = new Object();

    private int id;
    private final int size;
    private final Plugin plugin;
    private final InventoryType type;
    private final Component title;
    private final long uniqueId = INCREMENTAL_ID.getAndIncrement();

    Location location;
    boolean allowMovement;
    private Player player;
    private final GUI gui;
    private final Icon[] icons;
    private final Icon cursor = new RealIcon(true);
    private ManagementState state = ManagementState.IGNORE;

    GUIMenu(Plugin plugin, GUI gui, Component title, InventoryType type, int size) {
        this.gui = gui;
        this.size = size;
        this.title = title;
        this.type = type;
        this.plugin = plugin;
        int inventorySize = size + PLAYER_OPEN_INVENTORY_SIZE;
        this.icons = new Icon[inventorySize];
        IconAccess.addListener(this.cursor, this, CURSOR_SLOT);
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public void id(int id) {
        this.id = id;
    }

    @Override
    public Component title() {
        return title;
    }

    @Override
    public InventoryType type() {
        return type;
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Get the player that is viewing this inventory.
     *
     * @return The viewing player.
     */
    public Player player() {
        return player;
    }

    /**
     * Get the GUI that this inventory is the backer of.
     *
     * @return The GUI.
     */
    public GUI gui() {
        return gui;
    }

    @Override
    public boolean isFullyManaged() {
        return state == ManagementState.FULL;
    }

    /**
     * Get the {@link ManagementState} for the GUI.
     * <p>
     * Default {@link ManagementState#IGNORE}.
     *
     * @return The current {@link ManagementState}.
     */
    public ManagementState managementState() {
        return state;
    }

    /**
     * Set the {@link ManagementState} for the GUI.
     * <p>
     * Default {@link ManagementState#IGNORE}.
     *
     * @param state The {@link ManagementState} to set to.
     * @see ManagementState
     */
    public void managementState(ManagementState state) {
        checkState(player == null, "cannot modify an open GUI");
        this.state = state;
    }

    @Override
    public Icon cursor() {
        return cursor;
    }

    @Override
    public Icon[] icons() {
        return icons;
    }

    /**
     * Get all the icons that are currently set to be
     * displayed in the player's inventory.
     * <p>
     * This creates a copy of the icon array.
     *
     * @return The icons set to display in the player's inventory.
     */
    public Icon[] playerIcons() {
        Icon[] icons = new Icon[PLAYER_OPEN_INVENTORY_SIZE];
        System.arraycopy(this.icons, this.size, icons, 0, PLAYER_OPEN_INVENTORY_SIZE);
        return icons;
    }

    @Override
    public Icon icon(int slot) {
        checkElementIndex(slot, icons.length, "slot");
        return icons[slot];
    }

    /**
     * Get the icon in the given slot index specifically
     * within the player inventory.
     *
     * @param slot The slot index within the player
     *             inventory to get the icon from.
     * @return The icon in the slot in the player inventory.
     */
    public Icon playerIcon(int slot) {
        checkElementIndex(slot, PLAYER_OPEN_INVENTORY_SIZE, "player slot");
        return icons[size + slot];
    }

    /**
     * Set the icon in the given slot index (i.e. {@code 0}
     * is the first slot etc.) and immediately update it if
     * the inventory is currently open.
     *
     * @param icon The icon to set into the slot.
     * @param slot The slot to set the icon in.
     */
    public void icon(Icon icon, int slot) {
        checkElementIndex(slot, icons.length, "slot");
        IconAccess.removeListener(icons[slot], icon, this, slot);
        this.icons[slot] = icon;
        IconAccess.addListener(icon, this, slot);
        this.update(slot);
    }

    /**
     * Set the icon in the given slot index specifically
     * within the player inventory.
     *
     * @param icon The icon to set into the slot.
     * @param slot The slot index within the player
     *             inventory to set the icon to.
     */
    public void playerIcon(Icon icon, int slot) {
        this.icon(icon, size + slot);
    }

    /**
     * Set all the icons starting at index {@code 0}
     * and immediately update the slots if the inventory
     * is currently open.
     *
     * @param icons The icons to set.
     */
    public void icons(Icon[] icons) {
        this.icons(icons, 0);
    }

    /**
     * Set all the icons starting at index {@code 0}
     * of the player inventory and immediately update the
     * slots if the inventory is currently open.
     *
     * @param icons The icons to set.
     */
    public void playerIcons(Icon[] icons) {
        this.icons(icons, size);
    }

    /**
     * Set all the icons starting at the {@code offset}
     * index and immediately update the slots if the inventory
     * is currently open.
     *
     * @param icons The icons to set.
     * @param offset The index to start setting the icons at.
     */
    public void icons(Icon[] icons, int offset) {

        checkArgument(offset >= 0, "invalid offset: %s", offset);
        int length = icons.length;
        Icon[] display = this.icons;
        checkArgument(length + offset <= display.length,
                "too many icons with offset for this inventory: %s + %s > %s",
                length, offset, display.length);
        for (int i = 0; i < length; i++) {
            Icon icon = icons[i];
            int slot = i + offset;
            IconAccess.removeListener(display[slot], icon, this, slot);
            display[slot] = icon;
            IconAccess.addListener(icon, this, slot);
        }

        this.update(true, offset + length);
    }

    /**
     * Open this inventory and update it for the player.
     *
     * @param player The player to open the inventory for.
     * @throws IllegalStateException If the inventory is currently open
     *                               for another player.
     */
    public void open(Player player) throws IllegalStateException {

        synchronized (lock) {
            // Check that there is not more than one player opening this GUI
            checkArgument(this.player == null || this.player == player,
                    "cannot open for multiple players");
            this.player = player;
            this.location = player.getLocation();
            IconAccess.addListener(cursor, this, CURSOR_SLOT);
            int length = icons.length;
            for (int slot = 0; slot < length; slot++) {

                Icon icon = icons[slot];
                if (icon != null) {
                    IconAccess.addListener(icon, this, slot);
                }
            }
        }

        WindowService.INSTANCE.open(player, this);
        GUIManager.INSTANCE.cache(this);
    }

    @Override
    public boolean isTextWindow() {
        return gui instanceof TextInputGUI;
    }

    @Override
    public void updateText(String text) {
        ((TextInputGUI) gui).update(text);
    }

    @Override
    public void click(Player player, ClickMode clickMode, int slot, int button, ItemStack item) {

        if (this.click(player, slot, button, clickMode, item)) {
            // Must be in the player inventory
            checkState(slot == OUTSIDE_WINDOW_SLOT || slot >= this.size());
            switch (clickMode) {
                case DRAG, CLICK, THROW, CLONE -> {
                    // TODO: enable pass-through handling for non-full management states
                    if (slot != OUTSIDE_WINDOW_SLOT) {
                        this.update(slot);
                    }

                    this.update(CURSOR_SLOT);
                }
                case DOUBLE_CLICK -> {
                    this.update(true, this.size() + PLAYER_OPEN_INVENTORY_SIZE);
                    this.update(CURSOR_SLOT); // Update cursor
                }
                case KEY_PRESS -> {
                    // Update the number slot and the slot itself
                    this.update(slot);
                    if (button != 40) { // Not offhand
                        this.update(this.size() + 27 + button); // Reset the hotbar slot also
                    }
                }
                case SHIFT_CLICK -> {
                    // Update the whole GUI and the slot itself
                    this.update(true, this.size());
                    this.update(slot);
                }
                default -> {
                    this.update(slot);
                    this.update(CURSOR_SLOT);
                }
            }
        }
    }

    /**
     * Used to handle when a client clicks within this GUI inventory.
     *
     * @param player The client that is clicking.
     * @param slot The slot that the client is clicking.
     * @param button The button the client is using to click.
     * @param clickMode The type of click that is used.
     * @param item The item that the client has updated the slot to.
     * @return If the click should be allowed to proceed as it would
     *         normally if it wasn't handled within this GUI inventory.
     */
    private boolean click(Player player, int slot, int button, ClickMode clickMode, ItemStack item) {

        if (this.player != player) {
            return false;
        }

        // TODO handle drag start and adding items throughout the two inventories
        boolean inPlayerInventory = slot >= size;
        if (inPlayerInventory && this.state == ManagementState.IGNORE ||
            // Clicked outside the inventory
            slot == OUTSIDE_WINDOW_SLOT && cursor instanceof RealIcon) {
            // We're not handling player inventory interactions so let the
            // packet go through normally while changing the window ID and
            // offsetting the slot for the player inventory
            return true;
        }

        switch (clickMode) {
            case DRAG:
                // TODO handle this dragging by making a click on each item that's added
                return false;
            case THROW:
                update(slot); // Only update the slot
                return false;
            default:
                break;
        }

        if (slot == OUTSIDE_WINDOW_SLOT) {
            update(CURSOR_SLOT);
            return false;
        }

        Icon icon = this.icon(slot);
        if (icon == null) {
            update(slot);
            update(CURSOR_SLOT);
            return false;
        }

        ClickType clickType = clickMode.getType(button, slot);
        Click click = new Click(
                gui, player, icon, cursor.item(), item, slot,
                clickMode == ClickMode.KEY_PRESS ? button : -1,
                inPlayerInventory, clickType
        );

        long start = System.nanoTime();
        if (IconAccess.click(icon, plugin, click)) { // Should cancel

            long time = System.nanoTime() - start;
            if (time > 1000000000) {
                Bukkit.getLogger().warning("Took " + time / 1000000 + "ms to execute" +
                                           " click in slot " + slot + " of " + gui.getClass());
            }

            switch (clickMode) {
                case DOUBLE_CLICK -> {
                    // On double-click update the entire inventory including the player's
                    update(true, size + PLAYER_OPEN_INVENTORY_SIZE);
                    update(CURSOR_SLOT); // Update the cursor also
                }
                case KEY_PRESS -> {
                    update(slot);
                    if (button != 40) { // Not offhand
                        update(size + 27 + button); // Reset the hotbar slot also
                    }
                }
                case SHIFT_CLICK -> {
                    // Update the whole GUI and the slot itself
                    update(true, size + PLAYER_OPEN_INVENTORY_SIZE);
                    update(slot);
                }
                default -> {
                    update(slot);
                    update(CURSOR_SLOT);
                    if (type == InventoryType.ANVIL && slot == 2) {
                        update(0);
                        update(1);
                    }
                }
            }

            return false;
        }

        long time = System.nanoTime() - start;
        if (time > 1000000000) {
            Bukkit.getLogger().warning("Took " + time / 1000000 + "ms to execute" +
                                       " click in slot " + slot + " of " + gui.getClass());
        }

        /*
         * TODO:
         *  At this point the GUI should act as if it is a
         *  regular Minecraft inventory except that any
         *  items transferred from RealIcons to "fake" Icons
         *  are essentially deleted.
         *
         *  It should support all interactions that a regular
         *  inventory would support and the click action
         *  is expected to have handled the movement.
         */

        return false;
    }

    @Override
    public void updateInternal(int slot, ItemStack item) {
        // Lastly, update the real item in the GUI
        Icon icon = this.icon(slot);
        if (icon instanceof RealIcon) {
            icon.itemNoUpdate(item);
        } else {
            this.icon(new RealIcon().itemNoUpdate(item), slot);
        }
    }

    @Override
    public void updateInternal(int maxSlot, IntFunction<ItemStack> itemGetter) {
        // Should always exclude the offhand
        for (int slot = 0; slot < maxSlot; slot++) {
            // Refresh the items in the GUIs and set them so that they get updated
            ItemStack item = itemGetter.apply(slot);
            Icon icon = this.playerIcon(slot);
            if (icon instanceof RealIcon) {
                icon.item(item);
            } else {
                this.playerIcon(new RealIcon().item(item), slot);
            }
        }
    }

    /**
     * Force close this inventory for the currently open
     * player if it is currently open.
     */
    public void close() {

        if (player != null) {
            WindowService.INSTANCE.close(player, this);
            GUIManager.INSTANCE.clear(player);
        }

        this.cleanup();
    }

    /**
     * Used to handle when a client user closes a GUI inventory.
     *
     * @param player The player that closed the inventory.
     */
    @Override
    public void close(Player player) {

        if (this.player != player) {
            return;
        }

        GUI gui = this.gui;
        if (gui.close(player)) {
            this.open(player);
            return;
        }

        int len = icons.length;
        for (int slot = 0; slot < len; slot++) {

            Icon icon = icons[slot];
            if (icon != null) {
                IconAccess.close(icon, gui, player);
                IconAccess.removeListener(icon, icon, this, slot);
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            IconAccess.close(cursor, gui, player);
            IconAccess.removeListener(cursor, null, this, CURSOR_SLOT);
            if (GUI.gui(player) == null) {
                player.updateInventory();
            }
        }, 4L);
    }

    /**
     * Cleanup this GUI after it has been closed
     * cancelling tasks etc.
     * <p>
     * The only difference between this method and
     * {@link #close()} is that this method does
     * not get overridden and therefore only does
     * what is immediately apparent.
     */
    void cleanup() {

        Player player;
        synchronized (lock) {

            player = this.player;
            if (player == null) {
                return;
            }

            this.player = null;
        }

        GUI gui = this.gui;
        gui.close(player);
        IconAccess.close(cursor, gui, player);
        IconAccess.removeListener(cursor, null, this, CURSOR_SLOT);
        int len = icons.length;
        for (int slot = 0; slot < len; slot++) {

            Icon icon = icons[slot];
            if (icon != null) {
                IconAccess.close(icon, gui, player);
                IconAccess.removeListener(icon, icon, this, slot);
            }
        }
    }

    /**
     * Send updates to the player for the given slot.
     *
     * @param slot The slot to update.
     */
    public void update(int slot) {
        this.update(false, slot);
    }

    /**
     * Send updates to the player for the given slot optionally
     * sending all the slots up to the given slot.
     *
     * @param updateTo If the slot should be exclusive and all
     *                 of the slots before it be updated.
     * @param slot The slot to update or update to. Exclusive if
     *             {@code updateTo} is {@code true}.
     */
    public void update(boolean updateTo, int slot) {

        if (player != null) {
            WindowService.INSTANCE.update(player, updateTo, slot, this);
        }
    }

    @Override
    public int hashCode() {
        return Long.hashCode(uniqueId);
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof GUIMenu menu && menu.uniqueId == uniqueId;
    }

    public static class RealIcon extends Icon {

        private final boolean cursor;

        public RealIcon() {
            this.cursor = false;
        }

        RealIcon(boolean cursor) {
            this.cursor = cursor;
        }
    }
}
