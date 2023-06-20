package com.projecki.fusion.ui.inventory.icon.click;

import com.projecki.fusion.ui.inventory.GUI;
import com.projecki.fusion.ui.inventory.GUIMenu;
import com.projecki.fusion.ui.inventory.icon.Erroneous;
import com.projecki.fusion.ui.inventory.icon.Icon;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.sound.Sound.Type;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.sound.Sound.sound;

/**
 * The contextual information for when a click occurs
 * within a {@link GUI}.
 *
 * @since April 08, 2022
 * @author Andavin
 */
public record Click(GUI gui, Player player, Icon icon, ItemStack cursorItem, ItemStack currentItem,
                    int slot, int numberKey, boolean inPlayerInventory, ClickType clickType) implements Erroneous {

    public Click(GUI gui, Player player, Icon icon, ItemStack cursorItem,
                 int slot, int numberKey, ClickType clickType) {
        this(gui, player, icon, cursorItem, icon.item(), slot, numberKey, gui.size() <= slot, clickType);
    }

    /**
     * The {@link GUI} that the click occurred within.
     *
     * @return The GUI.
     */
    @Override
    public GUI gui() {
        return gui;
    }

    /**
     * The {@link Player} that is clicking within the {@link GUI}.
     *
     * @return The player.
     */
    @Override
    public Player player() {
        return player;
    }

    /**
     * The {@link Icon} that is being clicked.
     *
     * @return The icon.
     */
    @Override
    public Icon icon() {
        return icon;
    }

    /**
     * The index of the slot that was clicked within the
     * {@link GUI}.
     * <p>
     *     If the click occurred within the player's inventory,
     *     then the player index may be determined by subtracting
     *     {@link GUIMenu#PLAYER_OPEN_INVENTORY_SIZE}
     *     from this.
     * </p>
     *
     * @return The slot index of the click.
     */
    @Override
    public int slot() {
        return slot;
    }

    /**
     * The number key that was pressed.
     * <p>
     *     Note that this value will always be between {@code 0}
     *     and {@code 8}. Each index representing an index on the
     *     player's hotbar.
     * </p>
     * Default: {@code -1}
     *
     * @return The applicable number key.
     */
    @Override
    public int numberKey() {
        return numberKey;
    }

    /**
     * The current {@link ItemStack} held on the cursor
     * of the {@link GUI}.
     *
     * @return The cursor item.
     */
    @Override
    public ItemStack cursorItem() {
        return cursorItem;
    }

    /**
     * The {@link ItemStack} that current resides in the
     * slot being clicked within the {@link GUI}.
     * <p>
     *     If the item in the slot is updated at any point
     *     (e.g. {@link Icon#item(ItemStack)}), this value
     *     will not be updated.
     *     <br>
     *     <i>This is the state of slot before the click occurred.</i>
     * </p>
     *
     * @return The current item.
     */
    @Override
    public ItemStack currentItem() {
        return currentItem;
    }

    /**
     * The {@link ClickType} that occurred.
     *
     * @return The click type.
     */
    @Override
    public ClickType clickType() {
        return clickType;
    }

    /**
     * Whether the click occurred within the inventory of
     * the {@link Player} rather than within the {@link GUI}
     * inventory itself.
     *
     * @return If the click occurred within the player's inventory.
     */
    @Override
    public boolean inPlayerInventory() {
        return inPlayerInventory;
    }

    /**
     * Play the specified {@link Sound} to the {@link #player()}.
     * <p>
     *     Volume and pitch will both default to {@code 1}.
     * </p>
     *
     * @param sound The {@link Sound} to play.
     */
    public void playSound(Sound sound) {
        this.playSound(sound, 1, 1);
    }

    /**
     * Play the specified {@link Sound} to the {@link #player()}
     * at the specified volume and pitch.
     *
     * @param sound The {@link Sound} to play.
     * @param volume The volume to play the {@link Sound} at.
     * @param pitch The pitch to play the {@link Sound} at.
     */
    public void playSound(Sound sound, float volume, float pitch) {
        this.playSound(sound(sound, Source.MASTER, volume, pitch));
    }

    /**
     * Play the specified {@link Type sound} to the {@link #player()}.
     * <p>
     *     Volume and pitch will both default to {@code 1}.
     * </p>
     *
     * @param sound The {@link Type sound} to play.
     */
    public void playSound(Type sound) {
        this.playSound(sound, 1, 1);
    }

    /**
     * Play the specified {@link Type sound} to the {@link #player()}
     * at the specified volume and pitch.
     *
     * @param sound The {@link Type sound} to play.
     * @param volume The volume to play the {@link Type sound} at.
     * @param pitch The pitch to play the {@link Type sound} at.
     */
    public void playSound(Type sound, float volume, float pitch) {
        this.playSound(sound(sound, Source.MASTER, volume, pitch));
    }

    /**
     * Play the specified {@link Key sound} to the {@link #player()}.
     * <p>
     *     Volume and pitch will both default to {@code 1}.
     * </p>
     *
     * @param sound The {@link Key sound} to play.
     */
    public void playSound(Key sound) {
        this.playSound(sound, 1, 1);
    }

    /**
     * Play the specified {@link Key sound} to the {@link #player()}
     * at the specified volume and pitch.
     *
     * @param sound The {@link Key sound} to play.
     * @param volume The volume to play the {@link Key sound} at.
     * @param pitch The pitch to play the {@link Key sound} at.
     */
    public void playSound(Key sound, float volume, float pitch) {
        this.playSound(sound(sound, Source.MASTER, volume, pitch));
    }

    /**
     * Play the specified {@link net.kyori.adventure.sound.Sound}
     * to the {@link #player()}.
     *
     * @param sound The {@link net.kyori.adventure.sound.Sound} to play.
     */
    public void playSound(net.kyori.adventure.sound.Sound sound) {
        this.player.playSound(sound);
    }

    @Override
    public void error(Component title, @Nullable Component... desc) {
        this.icon.error(title, desc);
    }

    /**
     * Forcibly close the {@link #gui() GUI} for any
     * {@link Player} that currently has it open.
     */
    public void close() {
        this.gui.close();
    }
}
