package com.projecki.fusion.ui.inventory.icon;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.projecki.fusion.item.AbstractItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * @author Andavin
 * @since April 09, 2022
 */
public class IconItemCreator extends AbstractItemBuilder<IconItemCreator> {

    private final Icon icon;

    IconItemCreator(Icon icon, PlayerProfile profile) {
        super(profile);
        this.icon = icon;
    }

    IconItemCreator(Icon icon, Material type) {
        super(type);
        this.icon = icon;
    }

    IconItemCreator(Icon icon, ItemStack item) {
        super(item);
        this.icon = icon;
    }

    /**
     * Build an {@link Icon} from this creator by creating the
     * {@link ItemStack} and setting it to the underlying {@link Icon}.
     * <p>
     * If the {@link Icon#item()} is equal to the {@link ItemStack}
     * returned by {@link IconItemCreator#buildItem()}, then this method
     * will return the underlying {@link Icon} without setting the item.
     * </p>
     *
     * @return The return value of {@link Icon#item(ItemStack)}
     * when called on the underlying icon.
     */
    public Icon buildIcon() {
        ItemStack item = super.build();
        return Objects.equals(icon.item(), item) ? icon : icon.item(item);
    }

    /**
     * Build the actual {@link ItemStack} with the specifications
     * specified by this builder.
     *
     * @return The newly created ItemStack.
     */
    public ItemStack buildItem() {
        return super.build();
    }
}
