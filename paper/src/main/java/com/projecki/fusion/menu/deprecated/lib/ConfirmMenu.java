package com.projecki.fusion.menu.deprecated.lib;

import com.projecki.fusion.item.ItemBuilder;
import com.projecki.fusion.menu.deprecated.Button;
import com.projecki.fusion.menu.deprecated.Menu;
import com.projecki.fusion.menu.deprecated.Page;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

/**
 * @deprecated use new v2 package
 */
@Deprecated
public final class ConfirmMenu extends Menu {

    public ConfirmMenu(Component title, ConfirmAction action) {
        Page page = new Page(9, title);
        Button confirm = new Button(
                ItemBuilder.of(Material.GREEN_STAINED_GLASS_PANE)
                        .name(Component.text("Confirm", NamedTextColor.GREEN).decorate(TextDecoration.BOLD))
                        .build(),
                action::onConfirm);
        Button cancel = new Button(
                ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
                        .name(Component.text("Cancel", NamedTextColor.RED).decorate(TextDecoration.BOLD))
                        .build(),
                action::onConfirm);
        page.setButton(0, confirm);
        page.setButton(1, confirm);
        page.setButton(2, confirm);
        page.setButton(3, confirm);
        page.setItem(4, ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(Component.text("")).build());
        page.setButton(5, cancel);
        page.setButton(6, cancel);
        page.setButton(7, cancel);
        page.setButton(8, cancel);
        super.addPage(page);
    }

    public ConfirmMenu(ConfirmAction action) {
        this(Component.text("Confirm"), action);
    }
}
