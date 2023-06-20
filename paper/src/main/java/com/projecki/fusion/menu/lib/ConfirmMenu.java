package com.projecki.fusion.menu.lib;

import com.projecki.fusion.item.ItemBuilder;
import com.projecki.fusion.menu.Menu;
import com.projecki.fusion.menu.Page;
import com.projecki.fusion.menu.button.Button;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

/**
 * A menu that holds a simple purpose of confirming an action you design with outcomes you handle all in lambda functions.
 */
public class ConfirmMenu extends Menu {

    private static final int[] CONFIRM_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int[] DENY_SLOTS = {14, 15, 16, 23, 24, 25, 32, 33, 34};
    private static final int[] ALL_BUTTON_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30, 14, 15, 16, 23, 24, 25, 32, 33, 34};

    private final ConfirmAction confirmAction;

    public final Button confirmButton;
    public final Button denyButton;

    public ConfirmMenu(Component title, ConfirmAction confirmAction) {
        this.confirmAction = confirmAction;

        Page mainPage = new Page(5, title);
        mainPage.fillAllExcept(ALL_BUTTON_SLOTS);

        this.confirmButton = new Button(
                ItemBuilder.of(Material.GREEN_STAINED_GLASS_PANE).name(Component.text("Confirm", NamedTextColor.GREEN)).build(),
                confirmAction::onConfirm);
        this.denyButton = new Button(
                ItemBuilder.of(Material.RED_STAINED_GLASS_PANE).name(Component.text("Deny", NamedTextColor.RED)).build(),
                confirmAction::onConfirm);

        mainPage.setButton(this.confirmButton, CONFIRM_SLOTS);
        mainPage.setButton(this.denyButton, DENY_SLOTS);

        super.addPage(mainPage);
    }

    @Override
    public final void onClose(UUID player, int pageIndex, Inventory inventory) {
        confirmAction.onClose(player);
    }
}
