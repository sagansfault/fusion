package com.projecki.fusion.menu.deprecated.function;

import com.projecki.fusion.menu.deprecated.Menu;
import com.projecki.fusion.menu.deprecated.Page;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated use new v2 package
 */
@Deprecated
@FunctionalInterface
public interface SlotAction {

    /**
     * A function that is called when a player does an action on a non-button slot.
     *
     * @param info Info related to the slot action
     */
    void onSlotAction(ActionInfo info);

    /**
     * Action info related to this slot action
     *
     * @param player The player that did the action
     * @param menu The menu this action happened in
     * @param page The page this action happened in
     * @param slot The slot this action happened in. (0 if this was a drag/spreading of items)
     * @param item The item in the slot, possibly null if this was a result of a drag/spread or the slot was empty
     * @param inventory The actual inventory
     */
    record ActionInfo(Player player, Menu menu, Page page, int slot, @Nullable ItemStack item, Inventory inventory) {}
}
