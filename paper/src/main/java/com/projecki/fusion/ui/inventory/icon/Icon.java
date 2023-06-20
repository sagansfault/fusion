package com.projecki.fusion.ui.inventory.icon;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.projecki.fusion.item.ItemBuilder;
import com.projecki.fusion.ui.inventory.GUI;
import com.projecki.fusion.ui.inventory.GUIMenu;
import com.projecki.fusion.ui.inventory.icon.click.Action;
import com.projecki.fusion.ui.inventory.icon.click.Click;
import com.projecki.fusion.util.concurrent.RefreshTask;
import com.projecki.fusion.util.concurrent.Refreshable;
import com.projecki.unversioned.craft.CraftService;
import com.projecki.unversioned.window.WindowIcon;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * An object that allows the binding of {@link ItemStack}
 * as an icon that, when interacted with, will execute an
 * {@link Action} for the specific icon.
 *
 * @since April 09, 2022
 * @author Andavin
 */
public class Icon implements WindowIcon, Erroneous, Refreshable {

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
        return new Icon().createItem(item);
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
        return new Icon().createItem(type);
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
        return new Icon().createItem(profile);
    }

    private Action action;
    private ItemStack item, errorItem;
    private RefreshTask refreshTask, errorTask;
    private BiConsumer<GUI, Player> closeAction;

    private long syncTimeout = 100;
    private boolean sync, cancel = true, queueUpdate, queued;

    final Object mutex = new Object();
    final Map<GUIMenu, IntSet> itemUpdateListeners;

    public Icon() {
        this(new HashMap<>(3));
    }

    Icon(Map<GUIMenu, IntSet> itemUpdateListeners) {
        this.itemUpdateListeners = itemUpdateListeners;
    }

    /**
     * Get the {@link RefreshTask} that is scheduled for
     * {@link #error(Component, Component...) errors}
     * that occur in this click.
     *
     * @return The current error task.
     */
    public RefreshTask errorTask() {
        return errorTask;
    }

    @Override
    public @Nullable RefreshTask refreshTask() {
        return refreshTask;
    }

    /**
     * Determine whether this {@link Icon} does not contain
     * a {@link ItemStack}.
     *
     * @return {@code true} if this {@link Icon} does not contain
     *         a {@link ItemStack}.
     */
    public boolean isEmpty() {
        return item == null || item.getType() == Material.AIR;
    }

    /**
     * Get the item that is currently being displayed
     * for this icon.
     *
     * @return The item.
     */
    @Override
    public ItemStack item() {
        return errorItem == null ? item : errorItem;
    }

    /**
     * Set the item that should be displayed for this
     * icon in a {@link GUI}.
     *
     * @param item The item to set to.
     * @return This Icon.
     */
    public Icon item(@Nullable ItemStack item) {
        return this.itemNoUpdate(item).sendUpdate();
    }

    /**
     * Set the item that should be displayed for this
     * icon in a {@link GUI} without updating the GUI
     * with the new item.
     * <p>
     * Prefer {@link #item(ItemStack)} over this method
     * unless there is a specific use case.
     *
     * @param item The item to set to.
     * @return This Icon.
     */
    public Icon itemNoUpdate(@Nullable ItemStack item) {
        this.item = item != null ? CraftService.INSTANCE.ensureCraftItem(item) : null;
        return this;
    }

    /**
     * Create a new item as a copy of the item that should
     * be set as the item to be displayed for this icon
     * in a {@link GUI}.
     *
     * @param item The {@link ItemStack} to copy and build on top of.
     * @return A new {@link IconItemCreator} to create the item with.
     */
    public IconItemCreator createItem(@NotNull ItemStack item) {
        return new IconItemCreator(this, item);
    }

    /**
     * Create a new item of the given type that should
     * be set as the item to be displayed for this icon
     * in a {@link GUI}.
     *
     * @param type The type of material for the item.
     * @return A new {@link IconItemCreator} to create the item with.
     */
    public IconItemCreator createItem(@NotNull Material type) {
        return new IconItemCreator(this, type);
    }

    /**
     * Create a new item that is a player head that uses
     * the given {@link PlayerProfile} to create the item
     * that should be displayed for this icon in a {@link GUI}.
     *
     * @param profile The {@linkplain PlayerProfile} that
     *                should be used to create the item.
     * @return A new {@link IconItemCreator} to create the item with.
     */
    public IconItemCreator createItem(@NotNull PlayerProfile profile) {
        return new IconItemCreator(this, profile);
    }

    /**
     * Alter the current that is being displayed and
     * set the altered version as the new item that
     * should be displayed for this icon in a {@link GUI}.
     *
     * @return A new {@link ItemBuilder} to alter the item with.
     * @throws IllegalStateException If there is no item already
     *                               set on this icon.
     */
    public IconItemCreator alterItem() throws IllegalStateException {
        checkState(item != null, "no item to alter");
        return new IconItemCreator(this, item);
    }

    @Override
    public void error(Component title, @Nullable Component... desc) {

        ItemBuilder builder = ItemBuilder.of(Material.BARRIER).name(title);
        if (desc != null && desc.length > 0) {
            builder.lore(desc);
        }

        this.errorItem = builder.build();
        this.sendUpdate();
        this.errorTask = new RefreshTask(TimeUnit.SECONDS.toNanos(3), () -> {
            this.errorTask = null;
            this.errorItem = null;
            this.sendUpdate();
        });
    }

    /**
     * Send an update for the item stored within
     * this icon to the {@link GUI}.
     *
     * @return This Icon.
     */
    public Icon update() {
        return this.sendUpdate();
    }

    /**
     * Get the {@link Action} that should be executed when
     * this icon is clicked or interacted with in a {@link GUI}.
     *
     * @return The action that is set.
     */
    public Action action() {
        return action;
    }

    /**
     * Get whether this icon syncs to the main Minecraft
     * thread when a click occurs.
     *
     * @return If this icon is synchronized.
     */
    public boolean isSync() {
        return sync;
    }

    /**
     * Synchronize any {@link Click clicks} that occur on
     * the icon to the main Minecraft thread using
     * {@link BukkitScheduler#runTask(Plugin, Runnable)}.
     *
     * @return This Icon.
     * @see BukkitScheduler#runTask(Plugin, Runnable)
     */
    public Icon sync() {
        return this.sync(100, TimeUnit.MILLISECONDS);
    }

    /**
     * Synchronize any {@link Click clicks} that occur on
     * the icon to the main Minecraft thread using
     * {@link BukkitScheduler#runTask(Plugin, Runnable)}.
     *
     * @param timeout The maximum amount of time that the sync
     *                task should wait for the click to complete.
     * @param unit The {@link TimeUnit} to use for the {@code timeout}.
     * @return This Icon.
     * @throws IllegalArgumentException If the total timeout is less than 51ms.
     * @see BukkitScheduler#runTask(Plugin, Runnable)
     */
    public Icon sync(long timeout, TimeUnit unit) {
        this.sync = true;
        this.syncTimeout = unit.toMillis(timeout);
        checkArgument(syncTimeout > 50, "invalid timeout: %s %s", timeout, unit);
        return this;
    }

    /**
     * Set the {@link Action} that should be executed when
     * the icon is clicked or interacted with in a {@link GUI}.
     *
     * @param action The action to set to.
     * @return This Icon.
     */
    public Icon action(Action action) {
        this.action = action;
        return this;
    }

    /**
     * Set the {@link Action} that should be executed when
     * the icon is clicked or interacted with in a {@link GUI}.
     *
     * @param type The {@link ClickType} to use as a filter
     *             before executing the {@link Action}.
     * @param action The action to set to.
     * @return This Icon.
     */
    public Icon action(ClickType type, Action action) {
        return this.action(t -> t == type, action);
    }

    /**
     * Set the {@link Action} that should be executed when
     * the icon is clicked or interacted with in a {@link GUI}.
     *
     * @param first The {@link ClickType} to use as a filter
     *              before executing the {@link Action}.
     * @param second The second {@link ClickType} to filter by.
     * @param action The action to set to.
     * @return This Icon.
     */
    public Icon action(ClickType first, ClickType second, Action action) {
        return this.action(t -> t == first || t == second, action);
    }

    /**
     * Set the {@link Action} that should be executed when
     * the icon is clicked or interacted with in a {@link GUI}.
     *
     * @param first The {@link ClickType} to use as a filter
     *              before executing the {@link Action}.
     * @param second The second {@link ClickType} to filter by.
     * @param third The third {@link ClickType} to filter by.
     * @param action The action to set to.
     * @return This Icon.
     */
    public Icon action(ClickType first, ClickType second, ClickType third, Action action) {
        return this.action(t -> t == first || t == second || t == third, action);
    }

    /**
     * Set the {@link Action} that should be executed when
     * the icon is clicked or interacted with in a {@link GUI}.
     *
     * @param filter The filter to require before executing
     *               the {@link Action}.
     * @param action The action to set to.
     * @return This Icon.
     */
    public Icon action(Predicate<ClickType> filter, Action action) {
        return this.action(click -> {
            if (filter.test(click.clickType())) {
                action.accept(click);
            }
        });
    }

    /**
     * Set the {@link Action} that should be executed when
     * the icon is clicked or interacted with in a {@link GUI}.
     * <p>
     *     Each filtered {@link ClickType} is matched to an {@link Action}
     *     that will be executed if the {@link Click#clickType()} matches.
     *     <br>
     *     This is useful for things like executing one action for
     *     left clicks and another action for right clicks.
     * </p>
     *
     * @param firstType The {@link ClickType} that, if present, will
     *                  allow the {@code firstAction} to execute.
     * @param firstAction The {@link Action} to execute if
     *                    {@link Click#clickType()} matches {@code firstType}
     * @param secondType The {@link ClickType} that, if present, will
     *                   allow the {@code secondAction} to execute.
     * @param secondAction The {@link Action} to execute if
     *                     {@link Click#clickType()} matches {@code secondType}
     * @return This Icon.
     */
    public Icon action(ClickType firstType, Action firstAction, ClickType secondType, Action secondAction) {
        return this.action(click -> {

            if (click.clickType() == firstType) {
                firstAction.accept(click);
            } else if (click.clickType() == secondType) {
                secondAction.accept(click);
            }
        });
    }

    /**
     * Set the {@link Action} that should be executed when
     * the icon is clicked or interacted with in a {@link GUI}.
     * <p>
     * Each filtered {@link ClickType} is matched to an {@link Action}
     * that will be executed if the {@link Click#clickType()} matches.
     * <br>
     * This is useful for things like executing one action for
     * left clicks and another action for right clicks.
     * </p>
     *
     * @param firstType    The {@link ClickType} that, if present, will
     *                     allow the {@code firstAction} to execute.
     * @param firstAction  The {@link Action} to execute if
     *                     {@link Click#clickType()} matches {@code firstType}
     * @param secondType   The {@link ClickType} that, if present, will
     *                     allow the {@code secondAction} to execute.
     * @param secondAction The {@link Action} to execute if
     *                     {@link Click#clickType()} matches {@code secondType}
     * @param thirdType    The {@link ClickType} that, if present, will
     *                     allow the {@code thirdAction} to execute.
     * @param thirdAction  The {@link Action} to execute if
     *                     {@link Click#clickType()} matches {@code thirdType}
     * @return This Icon.
     */
    public Icon action(ClickType firstType, Action firstAction,
                       ClickType secondType, Action secondAction,
                       ClickType thirdType, Action thirdAction) {
        return this.action(click -> {

            if (click.clickType() == firstType) {
                firstAction.accept(click);
            } else if (click.clickType() == secondType) {
                secondAction.accept(click);
            } else if (click.clickType() == thirdType) {
                thirdAction.accept(click);
            }
        });
    }

    /**
     * Set the {@link BiConsumer action} to be executed when
     * a {@link GUI} containing the icon is closed by the player.
     *
     * @param action The action to execute on close.
     * @return This Icon.
     * @see #close(GUI, Player)
     */
    public Icon closeAction(BiConsumer<GUI, Player> action) {
        this.closeAction = action;
        return this;
    }

    /**
     * Set whether this {@link Icon} should cancel all interactions
     * by default or whether interactions should process as the
     * inventory expects.
     *
     * @param cancel If interactions should be cancelled by default.
     * @return This Icon.
     */
    public Icon cancelByDefault(boolean cancel) {
        this.cancel = cancel;
        return this;
    }

    /**
     * Create an auto refresh task that refreshes the item
     * in the icon immediately and then 1 second after that.
     * <br>
     * In other words, the {@code itemFunction} will be immediately
     * called and the {@link #item(ItemStack)} method executed with
     * the result. The {@code itemFunction} will then be called again
     * periodically.
     * <p>
     * Note that, if the item is {@link Object#equals(Object) equal}
     * to the current item, then it will not be set.
     *
     * @param itemFunction The function to use to get the
     *                     item for each refresh.
     * @return This Icon.
     */
    public Icon autoRefresh(Function<Icon, ItemStack> itemFunction) {
        return this.autoRefresh(1, TimeUnit.SECONDS, itemFunction);
    }

    /**
     * Create an auto refresh task that refreshes the item
     * in the icon immediately and then again every specified
     * period of time after that.
     * <br>
     * In other words, the {@code itemFunction} will be immediately
     * called and the {@link #item(ItemStack)} method executed with
     * the result. The {@code itemFunction} will then be called again
     * periodically.
     * <p>
     * Note that, if the item is {@link Object#equals(Object) equal}
     * to the current item, then it will not be set.
     *
     * @param period The period of units to wait between each refresh.
     * @param unit The {@link TimeUnit} to use to convert {@code period}.
     * @param itemFunction The function to use to get the
     *                     item for each refresh.
     * @return This Icon.
     */
    public Icon autoRefresh(long period, TimeUnit unit, Function<Icon, ItemStack> itemFunction) {
        checkState(refreshTask == null, "duplicate task");
        this.item(itemFunction.apply(this));
        this.refreshTask = new RefreshTask(unit.toNanos(period), () -> {

            ItemStack item = itemFunction.apply(this);
            if (!Objects.equals(this.item, item)) {
                this.item(item);
            }
        });
        return this;
    }

    /**
     * Called when this icon is clicked or interacted with in
     * a {@link GUI}.
     *
     * @param plugin The {@link Plugin} to sync with if required.
     * @param click The {@link Click} that occurred.
     * @return If the click should be cancelled.
     */
    boolean click(Plugin plugin, @NotNull Click click) {
        return this.click(plugin, click, sync);
    }

    /**
     * Called after a player closed the {@link GUI}.
     *
     * @param gui The GUI that was closed.
     * @param player The player that closed the GUI.
     */
    void close(GUI gui, Player player) {

        if (this.closeAction != null) {
            this.closeAction.accept(gui, player);
        }
    }

    /**
     * Copy the currently executing error from the
     * specified {@link Icon} to this one.
     *
     * @param icon The {@link Icon} to copy from.
     */
    void copyErrorFrom(Icon icon) {

        if (this.errorItem == null && icon.errorItem != null && icon.errorTask != null) {
            this.errorItem = icon.errorItem;
            this.errorTask = new RefreshTask(icon.errorTask, () -> {
                this.errorTask = null;
                this.errorItem = null;
                this.sendUpdate();
            });
        }
    }

    private boolean click(Plugin plugin, Click click, boolean sync) {

        if (sync) {

            CompletableFuture<Boolean> future = new CompletableFuture<>();
            Bukkit.getScheduler().runTask(plugin, () -> future.complete(this.click(plugin, click, false)));
            try {
                return future.get(syncTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {

                Throwable cause = e.getCause();
                if (cause != null) {
                    throw new RuntimeException(cause);
                } else {
                    plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
                    return true; // Always cancel
                }
            }
        }

        this.queueUpdate = true;
        if (this.action != null) {
            this.action.accept(click);
        }

        this.queueUpdate = false;
        if (this.queued) {
            this.sendUpdate();
        }

        return cancel;
    }

    private Icon sendUpdate() {

        if (this.queueUpdate) {
            this.queued = true;
            return this;
        }

        this.queued = false;
        synchronized (mutex) {

            for (Entry<GUIMenu, IntSet> entry : itemUpdateListeners.entrySet()) {

                GUIMenu inventory = entry.getKey();
                IntIterator itr = entry.getValue().iterator();
                while (itr.hasNext()) {
                    inventory.update(itr.nextInt());
                }
            }
        }

        return this;
    }
}
