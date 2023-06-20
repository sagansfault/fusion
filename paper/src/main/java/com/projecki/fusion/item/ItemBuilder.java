package com.projecki.fusion.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * A builder that helps construct complex {@link ItemStack items}.
 *
 * @author Andavin
 * @since April 24, 2022
 */
public class ItemBuilder extends AbstractItemBuilder<ItemBuilder> {

    /**
     * Create a skull item for the given {@link PlayerProfile}.
     *
     * @param profile The profile that the skin should be retrieved from.
     * @return The newly created {@link ItemBuilder}.
     */
    public static ItemBuilder of(PlayerProfile profile) {
        return new ItemBuilder(profile);
    }

    /**
     * Create a new item from the attributes of
     * the given item.
     *
     * @param item The items to copy.
     * @return The newly created {@link ItemBuilder}.
     */
    public static ItemBuilder of(ItemStack item) {
        return new ItemBuilder(item);
    }

    /**
     * Create a new item of the given type.
     *
     * @param type The type of item.
     * @return The newly created {@link ItemBuilder}.
     */
    public static ItemBuilder of(Material type) {
        return new ItemBuilder(type);
    }

    public static ItemBuilder from(ItemStack item) {
        return new ItemBuilder(item);
    }

    private ItemBuilder(PlayerProfile profile) {
        super(profile);
    }

    private ItemBuilder(ItemStack item) {
        super(item);
    }

    private ItemBuilder(Material type) {
        super(type);
    }

    @Override
    public ItemStack build() { // Expose
        return super.build();
    }
}
