package com.projecki.fusion.command.base;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.projecki.fusion.item.ItemBuilder;
import com.projecki.fusion.menu.Menu;
import com.projecki.fusion.menu.Page;
import com.projecki.fusion.menu.button.Button;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@CommandAlias("testinv")
@CommandPermission("testinv")
public class InventoryTestCommand extends BaseCommand {

    private final TestMenu testMenu = new TestMenu();

    @Default
    public void onTest(Player player) {
        testMenu.render(player);
    }

    public static class TestMenu extends Menu {

        public TestMenu() {
            super.addPage(new TestPage("1"));
            super.addPage(new TestPage("2"));
        }

        public static class TestPage extends Page {

            public TestPage(String title) {
                super(4, Component.text(title));
                this.setButton(new Button(ItemBuilder.of(Material.DIRT).build(), clickInfo -> {
                    clickInfo.page().removeButton(0);
                    clickInfo.menu().sendUpdate();
                }), 0);
            }
        }
    }
}
