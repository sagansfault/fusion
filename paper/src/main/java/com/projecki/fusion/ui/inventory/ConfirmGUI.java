package com.projecki.fusion.ui.inventory;

import com.projecki.fusion.ui.inventory.icon.Icon;
import com.projecki.fusion.ui.inventory.icon.UnmodifiableIcon;
import com.projecki.fusion.ui.inventory.icon.click.Action;
import com.projecki.fusion.ui.inventory.icon.click.Click;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static java.util.Objects.requireNonNullElseGet;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

/**
 * A simple {@link GUI} that allows for confirming or denying
 * an action.
 */
public class ConfirmGUI extends GUI {

    private static final int[] CONFIRM_SLOTS = { 10, 11, 12, 19, 20, 21, 28, 29, 30 };
    private static final int[] DENY_SLOTS = { 14, 15, 16, 23, 24, 25, 32, 33, 34 };
    private static final Icon FILLER = UnmodifiableIcon.of(Material.GRAY_STAINED_GLASS_PANE)
            .name(empty()).buildIcon();
    private static final Icon CONFIRM = UnmodifiableIcon.of(Material.LIME_STAINED_GLASS_PANE)
            .name(GREEN, BOLD, "Confirm")
            .lore(text("Click", GOLD, BOLD)
                    .append(text(" to ", WHITE))
                    .append(text("confirm", GREEN)))
            .buildIcon();
    private static final Icon DENY = UnmodifiableIcon.of(Material.RED_STAINED_GLASS_PANE)
            .name(RED, BOLD, "Deny")
            .lore(text("Click", GOLD, BOLD)
                    .append(text(" to ", WHITE))
                    .append(text("deny", RED)))
            .buildIcon();

    private final Action confirm, deny;

    protected ConfirmGUI(Builder builder) {

        super(requireNonNullElseGet(builder.title, () -> text("Please Confirm")), 45);
        if (builder.closeOnClick) {
            this.confirm = requireNonNullElseGet(builder.confirm, () -> __ -> {})
                    .andThen(Click::close);
            this.deny = requireNonNullElseGet(builder.deny, () -> __ -> {})
                    .andThen(Click::close);
        } else {
            this.confirm = requireNonNullElseGet(builder.confirm, () -> __ -> {});
            this.deny = requireNonNullElseGet(builder.deny, () -> __ -> {});
        }
    }

    @Override
    protected void populate(Player player) {

        this.outline(0, 45, FILLER);
        this.fillColumn(4, FILLER);

        Icon confirmIcon = CONFIRM.action(confirm);
        Icon denyIcon = DENY.action(deny);
        for (int slot : CONFIRM_SLOTS) {
            this.set(slot, confirmIcon);
        }

        for (int denySlot : DENY_SLOTS) {
            this.set(denySlot, denyIcon);
        }
    }

    /**
     * Create a new {@link Builder} for a {@link ConfirmGUI}.
     *
     * @return The new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    public interface ConfirmAction {

        /**
         * The action that is executed when the GUI inquiry
         * is confirmed.
         *
         * @param click The {@link Click} that occurred
         */
        default void confirm(Click click) {
            this.confirm();
        }

        /**
         * The action that is executed when the GUI inquiry
         * is confirmed.
         */
        default void confirm() {
        }

        /**
         * The action that is executed when the GUI inquiry
         * is denied.
         *
         * @param click The {@link Click} that occurred
         */
        default void deny(Click click) {
            this.deny();
        }

        /**
         * The action that is executed when the GUI inquiry
         * is denied.
         */
        default void deny() {
        }
    }

    public static final class Builder {

        private Component title;
        private boolean closeOnClick;
        private Action confirm, deny;

        Builder() {
        }

        /**
         * Set the title of the GUI.
         *
         * @param title The title.
         * @return This Builder.
         */
        public Builder title(String title) {
            return this.title(Component.text(title));
        }

        /**
         * Set the title of the GUI.
         *
         * @param title The title.
         * @return This Builder.
         */
        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        /**
         * Set whether the GUI should be closed after the
         * user has given an input.
         *
         * @param closeOnClick Whether the UI should close on click.
         * @return This Builder.
         */
        public Builder closeOnClick(boolean closeOnClick) {
            this.closeOnClick = closeOnClick;
            return this;
        }

        /**
         * Set the {@link Runnable action} that is executed when the
         * user selects confirm.
         *
         * @param action The {@link Runnable} to execute.
         * @return This Builder.
         */
        public Builder confirm(Runnable action) {
            return this.confirm(__ -> action.run());
        }

        /**
         * Set the {@link Action} that is executed when the
         * user selects confirm.
         *
         * @param action The {@link Action} to execute.
         * @return This Builder.
         */
        public Builder confirm(Action action) {
            this.confirm = action;
            return this;
        }

        /**
         * Set the {@link Runnable action} that is executed when the
         * user selects deny.
         *
         * @param action The {@link Runnable} to execute.
         * @return This Builder.
         */
        public Builder deny(Runnable action) {
            return this.deny(__ -> action.run());
        }

        /**
         * Set the {@link Action} that is executed when the
         * user selects deny.
         *
         * @param action The {@link Action} to execute.
         * @return This Builder.
         */
        public Builder deny(Action action) {
            this.deny = action;
            return this;
        }

        /**
         * Set the {@link ConfirmAction} that is executed when the
         * user selects a response.
         *
         * @param action The {@link ConfirmAction} to execute.
         * @return This Builder.
         */
        public Builder action(ConfirmAction action) {
            this.confirm = action::confirm;
            this.deny = action::deny;
            return this;
        }

        /**
         * Create and open the {@link ConfirmGUI} for the specified
         * {@link Player} by calling {@link GUI#open(Player)}.
         *
         * @param player The {@link Player} to open the GUI for.
         * @return The newly created {@link ConfirmGUI}.
         */
        public ConfirmGUI open(Player player) {
            ConfirmGUI gui = new ConfirmGUI(this);
            gui.open(player);
            return gui;
        }
    }
}
