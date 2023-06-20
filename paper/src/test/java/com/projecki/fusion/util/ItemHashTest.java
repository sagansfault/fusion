package com.projecki.fusion.util;

import com.projecki.fusion.item.ItemHash;
import com.projecki.fusion.test.MockBukkitTest;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class ItemHashTest extends MockBukkitTest {

    @Test
    public void hashTest () {
        var itemStack = new ItemStack(Material.GOLD_INGOT);
        var hash = ItemHash.getHexItemHash(itemStack);

        assertEquals(hash, ItemHash.getHexItemHash(new ItemStack(Material.GOLD_INGOT)), "Hash isn't consistent for the same item");
        assertNotEquals(hash, ItemHash.getHexItemHash(new ItemStack(Material.GOLD_BLOCK)), "Hash is the same for different items");
    }

    @Test
    public void hashLengthTest () {
        for (int i = 0; i < 10; i++) {
            var itemStack = new ItemStack(Material.values()[i]);
            var itemMeta = itemStack.getItemMeta();
            assertNotNull(itemMeta, "Failed to create item meta for " + Material.values()[i]);

            itemMeta.addEnchant(Enchantment.values()[i], i, true);
            itemMeta.addItemFlags(ItemFlag.values()[Math.min(i, ItemFlag.values().length - 1)]);
            itemMeta.setDisplayName("name - " + i);
            itemMeta.setLore(IntStream
                    .range(0, i + 1)
                    .boxed()
                    .map(num -> String.format("lore - %s", num))
                    .toList()
            );

            itemStack.setItemMeta(itemMeta);

            assertEquals(32, ItemHash.hashItem(itemStack).length, "Hash length isn't consistent!");
        }
    }

    @Test
    public void itemMetaTest () {
        var itemStack = new ItemStack(Material.GOLD_INGOT);
        var itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(ChatColor.GOLD + "Butter");
        itemStack.setItemMeta(itemMeta);

        var clone = itemStack.clone();
        var hash = ItemHash.getHexItemHash(itemStack);
        assertEquals(hash, ItemHash.getHexItemHash(clone), "Hash isn't consistent for the same item");

        itemMeta = clone.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GRAY + "Margarine"); // ew
        clone.setItemMeta(itemMeta);

        assertNotEquals(hash, ItemHash.getHexItemHash(clone), "Hash is the same for different items!");
    }

    @Test
    public void flagOrderTest () {
        var itemStack = new ItemStack(Material.GOLD_INGOT);
        var itemMeta = itemStack.getItemMeta();

        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(itemMeta);

        var clone = itemStack.clone();
        var cloneMeta = clone.getItemMeta();
        cloneMeta.addItemFlags(ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_DYE);
        clone.setItemMeta(cloneMeta);

        itemMeta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_DESTROYS);
        itemStack.setItemMeta(itemMeta);

        assertEquals(ItemHash.getHexItemHash(itemStack), ItemHash.getHexItemHash(clone), "Hash is different for different order of item flags!");
    }

    @Test
    public void enchantOrderTest () {
        var itemStack = new ItemStack(Material.GOLD_INGOT);
        var itemMeta = itemStack.getItemMeta();

        itemMeta.addEnchant(Enchantment.DIG_SPEED, 1, true);
        itemStack.setItemMeta(itemMeta);

        var clone = itemStack.clone();
        var cloneMeta = clone.getItemMeta();
        cloneMeta.addEnchant(Enchantment.FIRE_ASPECT, 1, true);
        cloneMeta.addEnchant(Enchantment.WATER_WORKER, 1, true);
        clone.setItemMeta(cloneMeta);

        itemMeta.addEnchant(Enchantment.WATER_WORKER, 1, true);
        itemMeta.addEnchant(Enchantment.FIRE_ASPECT, 1, true);
        itemStack.setItemMeta(itemMeta);

        assertEquals(ItemHash.getHexItemHash(itemStack), ItemHash.getHexItemHash(clone), "Hash is different for different order of enchantments!");
    }
}
