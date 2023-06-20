package com.projecki.fusion.ui.inventory.icon;

import com.projecki.fusion.ui.inventory.GUI;
import com.projecki.fusion.ui.inventory.GUIMenu;
import com.projecki.fusion.ui.inventory.icon.click.Click;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * @since April 09, 2022
 * @author Andavin
 */
public final class IconAccess {

    /**
     * Run the {@link Icon#click(Plugin, Click)} method on the
     * specified {@link Icon}.
     * <p>
     *     This method is for internal use only.
     * </p>
     *
     * @param icon The {@link Icon} to run the click on.
     * @param plugin The {@link Plugin} to sync with if required.
     * @param click The {@link Click} that occurred.
     * @return If the click should be cancelled.
     */
    @Internal
    public static boolean click(Icon icon, Plugin plugin, Click click) {
        return icon.click(plugin, click);
    }

    /**
     * Run the {@link Icon#close(GUI, Player)} method on the
     * specified {@link Icon}.
     * <p>
     *     This method is for internal use only.
     * </p>
     *
     * @param icon The {@link Icon} to run the close on.
     * @param gui The {@link GUI} that was closed.
     * @param player The player that closed the GUI.
     */
    @Internal
    public static void close(Icon icon, GUI gui, Player player) {
        icon.close(gui, player);
    }

    /**
     * Add a listener to the given icon. This method
     * is for internal use only.
     *
     * @param icon The icon to add the listener to.
     * @param menu The menu that the listener is for.
     * @param slot The slot in the inventory to update.
     */
    @Internal
    public static void addListener(Icon icon, GUIMenu menu, int slot) {

        if (icon != null && !(icon instanceof UnmodifiableIcon)) {

            synchronized (icon.mutex) {
                icon.itemUpdateListeners.computeIfAbsent(menu,
                        __ -> new IntOpenHashSet(2)).add(slot);
            }
        }
    }

    /**
     * Remove a listener from the given icon.
     * This method is for internal use only.
     *
     * @param icon The icon to remove the listener from.
     * @param replacement The icon that is replacing the icon.
     * @param menu The menu to remove the listener for.
     * @param slot The slot in the inventory.
     */
    @Internal
    public static void removeListener(Icon icon, Icon replacement, GUIMenu menu, int slot) {

        if (icon != null && replacement != null) {
            replacement.copyErrorFrom(icon);
        }

        if (icon != null && !(icon instanceof UnmodifiableIcon)) {

            synchronized (icon.mutex) {

                IntSet slots = icon.itemUpdateListeners.get(menu);
                if (slots != null && slots.remove(slot) && slots.isEmpty()) {
                    icon.itemUpdateListeners.remove(menu);
                }
            }
        }
    }
}
