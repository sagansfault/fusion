package com.projecki.fusion.menu.deprecated;

import com.projecki.fusion.item.ItemBuilder;
import com.projecki.fusion.menu.deprecated.function.ButtonFunction;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

/**
 * @deprecated use new v2 package
 */
@Deprecated
public class Button {

    private static final NamespacedKey BUTTON_KEY = new NamespacedKey("fusion", "menu-button");

    private final ItemStack item;
    private final ButtonFunction function;

    public Button(ItemStack item, ButtonFunction function) {
        this.function = function;
        this.item = ItemBuilder.from(item).nbtBoolean(BUTTON_KEY, true).build();
    }

    public Button(ItemStack item) {
        this(item, ((info) -> {}));
    }

    public ItemStack getItem() {
        return item;
    }

    public ButtonFunction getFunction() {
        return function;
    }

    public static boolean isButton(ItemStack item) {
        return item != null && item.getType() != Material.AIR && item.getItemMeta().getPersistentDataContainer().getKeys().contains(BUTTON_KEY);
    }
}
