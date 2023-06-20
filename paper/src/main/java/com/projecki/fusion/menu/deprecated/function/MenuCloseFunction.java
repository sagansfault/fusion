package com.projecki.fusion.menu.deprecated.function;

import com.projecki.fusion.menu.deprecated.Menu;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

/**
 * @deprecated use new v2 package
 */
@Deprecated
@FunctionalInterface
public interface MenuCloseFunction {

    /**
     * The function to run when the menu is closed. Types of closes excluded are
     * {@link org.bukkit.event.inventory.InventoryCloseEvent.Reason} {@code PLUGIN, OPEN_NEW}.
     *
     * @param closeInfo Info relating to this close function
     */
    void onClose(CloseInfo closeInfo);

    /**
     * Info relating to a close function
     *
     * @param player The player originally viewing the inventory
     * @param menu The menu this player was viewing
     * @param pageIndex The index of the page this player was viewing
     * @param inventory The actual inventory object
     */
    record CloseInfo(UUID player, Menu menu, Integer pageIndex, Inventory inventory) {}
}
