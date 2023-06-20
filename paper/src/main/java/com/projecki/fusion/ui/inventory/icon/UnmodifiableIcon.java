package com.projecki.fusion.ui.inventory.icon;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.projecki.fusion.item.ItemBuilder;
import com.projecki.fusion.ui.inventory.GUI;
import com.projecki.fusion.ui.inventory.icon.click.Action;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;

/**
 * An item that is semi-unmodifiable in that it can have
 * a maximum of one item set for initialization. After which,
 * it may set more items or actions, but only returns a
 * new {@link Icon} that is fully modifiable.
 *
 * @since April 09, 2022
 * @author Andavin
 */
public class UnmodifiableIcon extends Icon {

    /**
     * Create a new {@link Icon} with the specified {@link ItemStack}
     * that should be set as the item to be displayed for this icon
     * in a {@link GUI}.
     * <p>
     *     This is a shortcut method for creating a new {@link Icon}
     *     and calling {@link Icon#createItem(ItemStack)}.
     * </p>
     *
     * @param item The {@link ItemStack} to copy and build on top of.
     * @return A new {@link IconItemCreator}.
     * @see ItemBuilder#of(ItemStack)
     */
    public static IconItemCreator of(@NotNull ItemStack item) {
        return new UnmodifiableIcon().createItem(item);
    }

    /**
     * Create a new {@link Icon} with the specified {@link Material}
     * that should be set as the item to be displayed for this icon
     * in a {@link GUI}.
     * <p>
     *     This is a shortcut method for creating a new {@link Icon}
     *     and calling {@link Icon#createItem(Material)}.
     * </p>
     *
     * @param type The {@link Material} to build on top of.
     * @return A new {@link IconItemCreator}.
     * @see ItemBuilder#of(Material)
     */
    public static IconItemCreator of(@NotNull Material type) {
        return new UnmodifiableIcon().createItem(type);
    }

    /**
     * Create a new {@link Icon} with the specified {@link PlayerProfile}
     * that should be set as the item to be displayed for this icon
     * in a {@link GUI}.
     * <p>
     *     This is a shortcut method for creating a new {@link Icon}
     *     and calling {@link Icon#createItem(PlayerProfile)}.
     * </p>
     *
     * @param profile The {@linkplain PlayerProfile} that
     *                should be used to create the item.
     * @return A new {@link IconItemCreator}.
     * @see ItemBuilder#of(PlayerProfile)
     */
    public static IconItemCreator of(@NotNull PlayerProfile profile) {
        return new UnmodifiableIcon().createItem(profile);
    }

    public UnmodifiableIcon() {
        super(Map.of());
    }

    @Override
    public Icon itemNoUpdate(@Nullable ItemStack item) {
        return this.item() != null ?
                new Icon().itemNoUpdate(item) :
                super.itemNoUpdate(item);
    }

    @Override
    public Icon action(Action action) {
        return this.createIcon(icon -> icon.action(action));
    }

    @Override
    public Icon closeAction(BiConsumer<GUI, Player> action) {
        return this.createIcon(icon -> icon.closeAction(action));
    }

    @Override
    public Icon autoRefresh(long period, TimeUnit unit, Function<Icon, ItemStack> itemFunction) {
        return this.createIcon(icon -> icon.autoRefresh(period, unit, itemFunction));
    }

    private Icon createIcon(Function<Icon, Icon> function) {
        ItemStack item = this.item();
        checkState(item != null);
        return function.apply(new Icon().itemNoUpdate(item));
    }
}
