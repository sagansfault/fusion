package com.projecki.fusion.ui.inventory;

import com.projecki.fusion.ui.inventory.GUIMenu.RealIcon;
import com.projecki.fusion.ui.inventory.icon.Icon;
import com.projecki.fusion.util.concurrent.RefreshTask;
import com.projecki.fusion.util.concurrent.Refreshable;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.IntPredicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static net.kyori.adventure.text.Component.text;

/**
 * @since April 09, 2022
 * @author Andavin
 */
public class GUI implements Refreshable {

    private final GUIMenu menu;
    private RefreshTask refreshTask;

    /**
     * Create a new GUI of the given size of the type
     * {@link InventoryType#CHEST}.
     *
     * @param title The title of the GUI.
     * @param size The size of the GUI to create.
     */
    public GUI(String title, int size) {
        this(text(checkNotNull(title, "GUI title")), size);
    }

    /**
     * Create a new GUI of the given {@link InventoryType} and
     * using the {@link InventoryType#getDefaultSize() default size}.
     *
     * @param title The title of the GUI.
     * @param type The type of GUI to create.
     */
    public GUI(String title, InventoryType type) {
        this(text(checkNotNull(title, "GUI title")), type);
    }

    /**
     * Create a new GUI of the given {@link InventoryType} and
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
     */
    public GUI(String title, InventoryType type, int size) {
        this(text(checkNotNull(title, "GUI title")), type, size);
    }

    /**
     * Create a new GUI of the given size of the type
     * {@link InventoryType#CHEST}.
     *
     * @param title The title of the GUI.
     * @param size The size of the GUI to create.
     */
    public GUI(Component title, int size) {
        checkNotNull(title, "GUI title");
        checkArgument(size > 0, "size is not greater than zero: %s", size);
        this.menu = GUIManager.INSTANCE.createMenu(this, InventoryType.CHEST, title, size);
    }

    /**
     * Create a new GUI of the given {@link InventoryType} and
     * using the {@link InventoryType#getDefaultSize() default size}.
     *
     * @param title The title of the GUI.
     * @param type The type of GUI to create.
     */
    public GUI(Component title, InventoryType type) {
        checkNotNull(title, "GUI title");
        checkNotNull(type, "inventory type");
        this.menu = GUIManager.INSTANCE.createMenu(this, type, title, type.getDefaultSize());
    }

    /**
     * Create a new GUI of the given {@link InventoryType} and
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
     */
    public GUI(Component title, InventoryType type, int size) {
        checkNotNull(title, "GUI title");
        checkNotNull(type, "inventory type");
        checkArgument(size > 0, "size is not greater than zero: %s", size);
        this.menu = GUIManager.INSTANCE.createMenu(this, type, title, size);
    }

    @Override
    public final @Nullable RefreshTask refreshTask() {
        return refreshTask;
    }

    /**
     * Get the size of this GUI. This does not include
     * the player inventory size only the slots in the
     * actual inventory.
     *
     * @return The GUI size.
     */
    public final int size() {
        return this.menu.size();
    }

    /**
     * Get the current {@link Icon} that the player is
     * holding on their cursor.
     * <p>
     *     Returns an {@link Icon} with no {@link Icon#item()}
     *     if the player does not currently have anything on
     *     their cursor.
     * </p>
     *
     * @return The cursor.
     */
    @NotNull
    public final Icon cursor() {
        return this.menu.cursor();
    }

    /**
     * Get the player that is viewing this GUI.
     *
     * @return The viewing player.
     */
    public final Player player() {
        return this.menu.player();
    }

    /**
     * Allow movement while this GUI is open.
     */
    public final GUI allowMovement() {
        this.menu.allowMovement = true;
        return this;
    }

    /**
     * Get the {@link ManagementState} for this GUI.
     * <p>
     * Default {@link ManagementState#IGNORE}.
     *
     * @return The current {@link ManagementState}.
     */
    public final ManagementState managementState() {
        return menu.managementState();
    }

    /**
     * Set the {@link ManagementState} for this GUI.
     * <p>
     * Default {@link ManagementState#IGNORE}.
     *
     * @param state The {@link ManagementState} to set to.
     * @return This GUI.
     * @see ManagementState
     */
    public GUI managementState(ManagementState state) {
        this.menu.managementState(state);
        return this;
    }

    /**
     * Get all the icons that are currently set to be
     * displayed in the GUI window excluding the player's
     * inventory.
     * <p>
     * This creates a copy of the icon array.
     *
     * @return The icons in the window.
     */
    public final Icon[] icons() {
        return menu.icons().clone();
    }

    /**
     * Get all the icons that are currently set to be
     * displayed in the player's inventory.
     * <p>
     * This creates a copy of the icon array.
     *
     * @return The icons set to display in the player's inventory.
     */
    public final Icon[] playerIcons() {
        return menu.playerIcons();
    }

    /**
     * Get the icon in the given slot index (i.e. {@code 0}
     * is the first slot etc.).
     *
     * @param slot The slot index to get the icon from.
     * @return The icon in the slot.
     */
    public final Optional<Icon> icon(int slot) {
        return Optional.ofNullable(menu.icon(slot));
    }

    /**
     * Get the icon in the given slot index specifically
     * within the player inventory.
     *
     * @param slot The slot index within the player
     *             inventory to get the icon from.
     * @return The icon in the slot in the player inventory.
     */
    public final Optional<Icon> playerIcon(int slot) {
        return Optional.ofNullable(menu.playerIcon(slot));
    }

    /**
     * Set an {@link Icon} into the given slot in this GUI.
     *
     * @param slot The slot to set the icon at.
     * @param icon The icon to set in the slot.
     * @return This GUI.
     */
    public final GUI set(int slot, @Nullable Icon icon) {
        checkNotPlayerSlot(slot, menu.size());
        this.menu.icon(icon, slot);
        return this;
    }

    /**
     * Set an {@link Icon} into a slot that is within the
     * player's inventory.
     *
     * @param slot The slot within the player's inventory to
     *             set the icon at.
     * @param icon The icon to set in the slot.
     * @return This GUI.
     * @see ManagementState
     * @see #managementState(ManagementState)
     */
    public final GUI setPlayer(int slot, @Nullable Icon icon) {
        this.menu.playerIcon(icon, slot);
        return this;
    }

    /**
     * Set the given {@link Icon Icons} within the GUI starting
     * at slot {@code 0} for the first icon.
     *
     * @param icons The icons to set.
     * @return This GUI.
     */
    public final GUI set(Icon... icons) {
        return this.set(0, icons);
    }

    /**
     * Set the given {@link Icon Icons} within the GUI starting
     * at the slot specified by the {@code offset} for the first icon.
     *
     * @param offset The slot to start setting the icons at.
     * @param icons The icons to set.
     * @return This GUI.
     */
    public final GUI set(int offset, Icon... icons) {
        checkNotPlayerSlot(offset + icons.length - 1, menu.size());
        this.menu.icons(icons, offset);
        return this;
    }

    /**
     * Set the given {@link Icon Icons} with the GUI starting
     * at slot {@code 0} within the player's inventory for
     * the first icon.
     *
     * @param icons The icons to set.
     * @return This GUI.
     */
    public final GUI setPlayer(Icon... icons) {
        this.menu.playerIcons(icons);
        return this;
    }

    /**
     * Set the given {@link Icon Icons} with the GUI starting
     * at the slot specified within the player's inventory for
     * the first icon.
     *
     * @param offset The slot to start setting the icons at
     *               within the player's inventory.
     * @param icons The icons to set.
     * @return This GUI.
     */
    public final GUI setPlayer(int offset, Icon... icons) {
        this.menu.icons(icons, offset + this.menu.size());
        return this;
    }

    /**
     * Fill the given amount of rows in this GUI with
     * the given {@link Icon}.
     *
     * @param icon The icon to fill this GUI with.
     * @param rows The amount of rows to fill in the GUI.
     * @param excludedSlots The slots to exclude from being filled.
     * @return This GUI.
     */
    public final GUI fill(Icon icon, int rows, int... excludedSlots) {
        return this.fill(icon, rows, excludeSlots(excludedSlots));
    }

    /**
     * Fill the given amount of rows in this GUI with
     * the given {@link Icon}.
     *
     * @param icon The icon to fill this GUI with.
     * @param rows The amount of rows to fill in the GUI.
     * @param slotTest The predicate to use to see if a
     *                 slot should be filled.
     * @return This GUI.
     */
    public final GUI fill(Icon icon, int rows, IntPredicate slotTest) {
        return this.fill(0, rows * 9, icon, slotTest);
    }

    /**
     * Fill the slots between the given slots in this
     * GUI with the given {@link Icon}.
     *
     * @param from The slot to fill from (inclusive).
     * @param to The slot to fill to (exclusive).
     * @param icon The icon to fill this GUI with.
     * @return This GUI.
     */
    public final GUI fill(int from, int to, Icon icon) {
        return this.fill(from, to, icon, slot -> true);
    }

    /**
     * Fill the slots between the given slots in this
     * GUI with the given {@link Icon}.
     *
     * @param from The slot to fill from (inclusive).
     * @param to The slot to fill to (exclusive).
     * @param icon The icon to fill this GUI with.
     * @param excludedSlots The slots to exclude from being filled.
     * @return This GUI.
     */
    public final GUI fill(int from, int to, Icon icon, int... excludedSlots) {
        return this.fill(from, to, icon, excludeSlots(excludedSlots));
    }

    /**
     * Fill the slots between the given slots in this
     * GUI with the given {@link Icon}.
     *
     * @param from The slot to fill from (inclusive).
     * @param to The slot to fill to (exclusive).
     * @param icon The icon to fill this GUI with.
     * @param slotTest The predicate to use to see if a
     *                 slot should be filled.
     * @return This GUI.
     */
    public final GUI fill(int from, int to, Icon icon, IntPredicate slotTest) {

        int size = size();
        checkArgument(from >= 0, "cannot be negative: %s", from);
        checkArgument(from < size, "cannot be more than or size: %s >= %s", from, size);
        checkArgument(to >= 0, "cannot be negative: %s", to);
        checkArgument(to <= size, "cannot be more than size: %s > %s", to, size);
        for (int slot = from; slot < to; slot++) {

            if (slotTest.test(slot)) {
                this.set(slot, icon);
            }
        }

        return this;
    }

    /**
     * Fill the slots along the row with the specified {@link Icon}
     * starting at the specified slot and moving left to the last
     * slot of the row.
     *
     * @param first The first slot of the row.
     * @param icon The {@link Icon} to fill with.
     * @return This GUI.
     */
    public final GUI fillRow(int first, Icon icon) {
        return this.fillRow(first, icon, slot -> true);
    }

    /**
     * Fill the slots along the row with the specified {@link Icon}
     * starting at the specified slot and moving left to the last
     * slot of the row.
     *
     * @param first The first slot of the row.
     * @param icon The {@link Icon} to fill with.
     * @param excludedSlots The slots to exclude from being filled.
     * @return This GUI.
     */
    public final GUI fillRow(int first, Icon icon, int... excludedSlots) {
        return this.fillRow(first, icon, excludeSlots(excludedSlots));
    }

    /**
     * Fill the slots along the row with the specified {@link Icon}
     * starting at the specified slot and moving left to the last
     * slot of the row.
     *
     * @param first The first slot of the row.
     * @param icon The {@link Icon} to fill with.
     * @param slotTest The {@link IntPredicate} to use to determine
     *                 whether the row should be filled.
     * @return This GUI.
     */
    public final GUI fillRow(int first, Icon icon, IntPredicate slotTest) {

        checkArgument(0 <= first, "cannot be negative: %s", first);
        int size = this.size();
        int slot = first;
        do {

            if (slotTest.test(slot)) {
                this.set(slot, icon);
            }
        } while (++slot % 9 != 0);
        return this;
    }

    /**
     * Fill the slots along the column with the specified {@link Icon}
     * starting at the specified slot and moving downward to the bottom
     * of the {@link GUI}.
     *
     * @param column The top slot of the column.
     * @param icon The {@link Icon} to fill with.
     * @return This GUI.
     */
    public final GUI fillColumn(int column, Icon icon) {
        return this.fillColumn(column, icon, slot -> true);
    }

    /**
     * Fill the slots along the column with the specified {@link Icon}
     * starting at the specified slot and moving downward to the bottom
     * of the {@link GUI}.
     *
     * @param column The top slot of the column.
     * @param icon The {@link Icon} to fill with.
     * @param excludedSlots The slots to exclude from being filled.
     * @return This GUI.
     */
    public final GUI fillColumn(int column, Icon icon, int... excludedSlots) {
        return this.fillColumn(column, icon, excludeSlots(excludedSlots));
    }

    /**
     * Fill the slots along the column with the specified {@link Icon}
     * starting at the specified slot and moving downward to the bottom
     * of the {@link GUI}.
     *
     * @param column The top slot of the column.
     * @param icon The {@link Icon} to fill with.
     * @param slotTest The {@link IntPredicate} to use to determine
     *                 whether the slot should be filled.
     * @return This GUI.
     */
    public final GUI fillColumn(int column, Icon icon, IntPredicate slotTest) {

        checkArgument(0 <= column, "cannot be negative: %s", column);
        int size = this.size();
        for (int slot = column; slot < size; slot += 9) {

            if (slotTest.test(slot)) {
                this.set(slot, icon);
            }
        }

        return this;
    }

    /**
     * Fill the slots along the parameter of the specified
     * slot with the specified {@link Icon}.
     *
     * @param min The minimum slot i.e. top left (inclusive).
     * @param max The maximum slot i.e. bottom right (inclusive).
     * @param icon The {@link Icon} to outline with.
     * @return This GUI.
     */
    public final GUI outline(int min, int max, Icon icon) {
        return this.outline(min, max, icon, slot -> true);
    }

    /**
     * Fill the slots along the parameter of the specified
     * slot with the specified {@link Icon}.
     *
     * @param min  The minimum slot i.e. top left (inclusive).
     * @param max  The maximum slot i.e. bottom right (inclusive).
     * @param icon The {@link Icon} to outline with.
     * @param excludedSlots The slots to exclude from being filled.
     * @return This GUI.
     */
    public final GUI outline(int min, int max, Icon icon, int... excludedSlots) {
        return this.outline(min, max, icon, excludeSlots(excludedSlots));
    }

    /**
     * Fill the slots along the parameter of the specified
     * slot with the specified {@link Icon}.
     *
     * @param min The minimum slot i.e. top left (inclusive).
     * @param max The maximum slot i.e. bottom right (inclusive).
     * @param icon The {@link Icon} to outline with.
     * @param slotTest The {@link IntPredicate} to use to determine
     *                 whether the slot should be filled.
     * @return This GUI.
     */
    public final GUI outline(int min, int max, Icon icon, IntPredicate slotTest) {

        checkArgument(min <= max, "min > max");
        int minX = min % 9, maxX = max % 9;
        int minY = min / 9, maxY = max / 9;
        for (int x = minX; x <= maxX; x++) {

            for (int y = minY; y <= maxY; y++) {

                if (x == minX || x == maxX || y == minY || y == maxY) {

                    int slot = x + y * 9;
                    if (slotTest.test(slot)) {
                        this.set(slot, icon);
                    }
                }
            }
        }

        return this;
    }

    /**
     * Clear the {@link Icon} located at the specified slot
     * within this GUI.
     *
     * @param slot The slot to clear the {@link Icon} from.
     * @return This GUI.
     */
    public final GUI clear(int slot) {
        return menu.icon(slot) != null ? this.set(slot, (Icon) null) : this;
    }

    /**
     * Set this GUI to automatically refresh every 1 second
     * calling the {@link #populate(Player)} methods.
     * <p>
     * If the {@link ManagementState} of this GUI is set to
     * a state other than {@link ManagementState#IGNORE}, then
     * the {@link #populate(Player, ItemStack[])} will also
     * be called.
     *
     * @return This GUI.
     */
    public final GUI autoRefresh() {
        return this.autoRefresh(1, TimeUnit.SECONDS);
    }

    /**
     * Set this GUI to automatically refresh every period
     * of time calling the {@link #populate(Player)} methods.
     * <p>
     * If the {@link ManagementState} of this GUI is set to
     * a state other than {@link ManagementState#IGNORE}, then
     * the {@link #populate(Player, ItemStack[])} will also
     * be called.
     *
     * @param period The period of units to wait between each refresh.
     * @param unit The {@link TimeUnit} to use to convert {@code period}.
     * @return This GUI.
     */
    public final GUI autoRefresh(long period, TimeUnit unit) {
        return this.autoRefresh(period, unit, false);
    }

    /**
     * Set this GUI to automatically refresh every period
     * of time calling the {@link #populate(Player)} methods.
     * <p>
     * If the {@link ManagementState} of this GUI is set to
     * a state other than {@link ManagementState#IGNORE}, then
     * the {@link #populate(Player, ItemStack[])} will also
     * be called.
     *
     * @param period The period of units to wait between each refresh.
     * @param unit The {@link TimeUnit} to use to convert {@code period}.
     * @param playerOnly If only the {@link #populate(Player, ItemStack[])}
     *                   should be called.
     * @return This GUI.
     * @throws IllegalArgumentException If the {@link ManagementState}
     *                                  is set to {@link ManagementState#IGNORE}
     *                                  while {@code playerOnly} is {@code true}.
     */
    public final GUI autoRefresh(long period, TimeUnit unit, boolean playerOnly) {

        ManagementState state = menu.managementState();
        checkArgument(!playerOnly || state != ManagementState.IGNORE,
                "invalid management state: %s", state);
        this.refreshTask = new RefreshTask(unit.toNanos(period), () -> {

            if (menu.player() != null) {
                this.populate(playerOnly);
            } else {
                Bukkit.getLogger().warning("Task in unopened GUI: " + this.getClass().getSimpleName());
            }
        });
        return this;
    }

    /**
     * Open this GUI for the given player.
     *
     * @param player The player to open the GUI for.
     * @throws IllegalStateException If the GUI has already been
     *                               opened for another player.
     */
    public void open(Player player) throws IllegalStateException {

        ManagementState state = menu.managementState();
        ItemStack[] contents = state == ManagementState.FULL ?
                getContents(player.getInventory()) :
                this.addPlayerContents(player, (slot, item) -> new RealIcon().item(item));
        this.populate(player);
        if (state != ManagementState.IGNORE) {
            this.populate(player, contents);
        }

        this.menu.open(player);
    }

    /**
     * Open this GUI for the given player while creating
     * {@link Icon Icons} for each of the items that are currently
     * in the storage contents of the player's inventory.
     *
     * @param player The player to open the GUI for.
     * @param createFunction The {@link Function} to use to create the
     *                       icons for the items in the player's inventory.
     * @throws IllegalStateException If the GUI has already been
     *                               opened for another player.
     */
    public void open(Player player, Function<ItemStack, Icon> createFunction) throws IllegalStateException {
        this.open(player, (slot, item) -> createFunction.apply(item));
    }

    /**
     * Open this GUI for the given player while creating
     * {@link Icon Icons} for each of the items that are currently
     * in the storage contents of the player's inventory.
     *
     * @param player The player to open the GUI for.
     * @param createFunction The {@link InventoryIconFunction} to use to create
     *                       the icons for the items in the player's inventory.
     * @throws IllegalStateException If the GUI has already been
     *                               opened for another player.
     */
    public void open(Player player, InventoryIconFunction createFunction) throws IllegalStateException {

        ItemStack[] contents = this.addPlayerContents(player, createFunction);
        this.populate(player);
        if (menu.managementState() != ManagementState.IGNORE) {
            this.populate(player, contents);
        }

        this.menu.open(player);
    }

    /**
     * Called directly before a {@link Player} opens
     * this GUI or whenever the refresh task for this
     * GUI is executed to allow the GUI to be populated
     * with {@link Icon Icons}.
     * <p>
     * This method is always called on open, regardless
     * of {@link ManagementState} set.
     *
     * @param player The player that is opening the GUI.
     */
    protected void populate(Player player) {
    }

    /**
     * Called directly before a {@link Player} opens
     * this GUI or whenever the refresh task for this
     * GUI is executed to allow the GUI to be populated
     * with {@link Icon Icons}.
     * <p>
     * This method is only called if this GUI's
     * {@link ManagementState} is set to a state other
     * than {@link ManagementState#IGNORE}.
     *
     * @param player The player that is opening the GUI.
     * @param contents The {@link Inventory} storage contents
     *                 of the player ordered from left to right,
     *                 top to bottom.
     */
    protected void populate(Player player, ItemStack[] contents) {
    }

    /**
     * Call {@link #populate(Player)} and {@link #populate(Player, ItemStack[])},
     * if applicable, with the correct contents via {@link #getContents(PlayerInventory)}
     * using the player that this GUI was opened for.
     * <p>
     * This GUI must be opened before calling this method.
     *
     * @throws IllegalStateException If this GUI has not yet been opened.
     * @see GUIMenu#player() The player used
     */
    protected final void populate() {
        this.populate(false);
    }

    /**
     * Call {@link #populate(Player)} and {@link #populate(Player, ItemStack[])},
     * if applicable, with the correct contents via {@link #getContents(PlayerInventory)}
     * using the player that this GUI was opened for.
     * <p>
     * This GUI must be opened before calling this method.
     *
     * @param playerOnly If only the {@link #populate(Player, ItemStack[])} should be called.
     * @throws IllegalStateException If this GUI has not yet been opened.
     * @see GUIMenu#player() The player used
     */
    protected final void populate(boolean playerOnly) {

        Player player = menu.player();
        checkState(player != null, "not opened");
        if (playerOnly) {
            this.populate(player, getContents(player.getInventory()));
        } else {

            this.populate(player);
            if (menu.managementState() != ManagementState.IGNORE) {
                this.populate(player, getContents(player.getInventory()));
            }
        }
    }

    /**
     * Forcibly close this GUI for any player
     * that currently has it open.
     */
    public final void close() {
        // Store the player before closing to prevent null
        Player player = menu.player();
        this.menu.close();
        if (player != null) {
            // Update the player inventory to restore items
            player.updateInventory();
        }
    }

    /**
     * Called whenever this GUI is closed for whatever reason.
     * <p>
     *     A class that inherits this one may override this method
     *     to receive the close notification.
     * </p>
     *
     * @param player The player that had the GUI open.
     * @return If the GUI should be reopened.
     */
    protected boolean close(Player player) {
        return false;
    }

    private ItemStack[] addPlayerContents(Player player, InventoryIconFunction createIcons) {

        ItemStack[] contents = getContents(player.getInventory());
        int length = contents.length;
        Icon[] icons = new Icon[length];
        for (int slot = 0; slot < length; slot++) {
            icons[slot] = createIcons.apply(slot, contents[slot]);
        }

        this.menu.playerIcons(icons);
        return contents;
    }

    /**
     * Get the GUI that the given player currently
     * is linked to or has opened.
     *
     * @param player The player to get the GUI for.
     * @return The GUI that the player is linked to
     *         or {@code null} if there is no GUI.
     */
    public static GUI gui(Player player) {
        GUIMenu menu = GUIManager.INSTANCE.window(player);
        return menu != null ? menu.gui() : null;
    }

    /**
     * Get the contents of the given {@link PlayerInventory}
     * in the proper order for a GUI.
     * <p>
     *     By default, a player's inventory is organized with
     *     the hotbar contents first, then the inventory from
     *     top to bottom.
     * </p>
     * <p>
     *     Instead, this method rearranges the hotbar contents
     *     to come after the inventory contents to match the
     *     network order and be properly ordered from top to
     *     bottom of the inventory.
     * </p>
     *
     * @param inventory The inventory to get the contents from.
     * @return The properly ordered contents of the inventory.
     */
    protected static ItemStack[] getContents(PlayerInventory inventory) {
        ItemStack[] contents = inventory.getStorageContents();
        // Network items are arranged differently than saved items
        int length = contents.length;
        ItemStack[] networkContents = new ItemStack[length];
        // Move the 9 slots of the hotbar to the end of the array
        System.arraycopy(contents, 0, networkContents, length - 9, 9);
        // Move the rest of the items back 9 slots
        System.arraycopy(contents, 9, networkContents, 0, length - 9);
        return networkContents;
    }

    /**
     * Offset the specified index to a player slot that is safe to use
     * in {@link PlayerInventory#setItem(int, ItemStack)}.
     * <p>
     * {@link #getContents(PlayerInventory)} always returns contents
     * in the network order (i.e. hotbar is last). However, the server
     * stores the inventory with the hotbar before the contents of the
     * inventory.
     * <p>
     * This method reverses an index to be a valid server player slot.
     * For example:
     * <ul>
     *     <li>{@code indexToSlot(0) = 9}</li>
     *     <li>{@code indexToSlot(38) = 2}</li>
     * </ul>
     * In this way, items can be set in the player inventory without
     * too much headache.
     * <pre>
     *
     *     for (int i = 0; i < contents.length; i++) {
     *
     *          if (condition) {
     *              PlayerInventory inv = player.getInventory();
     *              inv.setItem(indexToSlot(i), contents[i].etc(...));
     *          }
     *     }
     * </pre>
     *
     * @param index The index to convert into a slot.
     * @return The slot that can be used in a {@link PlayerInventory}.
     * @see PlayerInventory#setItem(int, ItemStack)
     */
    protected static int indexToSlot(int index) {
        return index >= 27 ? index - 27 : index + 9;
    }

    private static IntPredicate excludeSlots(int... slots) {
        return switch (slots.length) {
            case 0 -> slot -> true;
            case 1 -> slot -> slot != slots[0];
            default -> slot -> {

                for (int excludedSlot : slots) {

                    if (slot == excludedSlot) {
                        return false;
                    }
                }

                return true;
            };
        };
    }

    private static void checkNotPlayerSlot(int slot, int size) {
        checkArgument(slot < size, "unintentional player slot: %s > %s", slot, size);
    }

    public interface InventoryIconFunction {

        /**
         * Create a new {@link Icon} given the {@link ItemStack}
         * from the given slot.
         * <p>
         * The slot given is an index of where the given
         * {@link ItemStack} is located within the inventory
         * analogous with natural order starting at the top and
         * going left to right, top to bottom.
         *
         * @param slot The slot of the item in the inventory.
         * @param item The item in the slot.
         * @return The newly create {@link Icon}.
         */
        Icon apply(int slot, ItemStack item);
    }
}
