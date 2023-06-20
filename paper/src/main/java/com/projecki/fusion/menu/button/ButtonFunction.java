package com.projecki.fusion.menu.button;

import com.projecki.fusion.menu.AbstractMenu;
import com.projecki.fusion.menu.Page;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

@FunctionalInterface
public interface ButtonFunction {

    /**
     * The function to run when a button is successfully clicked
     *
     * @param clickInfo The information related to the click
     */
    void onClick(ClickInfo clickInfo);

    /**
     * Info relating to the click function
     *
     * @param menu The menu the clicked button is in
     * @param page The page the clicked button is in
     * @param pageIndex The index of the page the clicked button is in
     * @param button The button that was clicked
     * @param clickType The click type
     * @param player The player who clicked
     */
    record ClickInfo(AbstractMenu menu, Page page, int pageIndex, Button button, ClickType clickType, Player player) {}
}
