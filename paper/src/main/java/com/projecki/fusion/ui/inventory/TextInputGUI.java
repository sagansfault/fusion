package com.projecki.fusion.ui.inventory;

import com.projecki.fusion.ui.inventory.icon.Icon;
import com.projecki.fusion.ui.inventory.icon.UnmodifiableIcon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link GUI} of the type {@link InventoryType#ANVIL} that
 * is intended for a user to input text and handled by handlers
 * provided to the UI.
 *
 * @since April 11, 2022
 * @author Andavin
 */
public class TextInputGUI extends GUI {

    private static final int RESULT_SLOT = 2;
    private static final Material VALID = Material.GREEN_CONCRETE, INVALID = Material.BARRIER;
    private static final Icon ERROR_ICON = UnmodifiableIcon.of(INVALID)
            .name(NamedTextColor.RED, TextDecoration.BOLD, "Invalid Input")
            .buildIcon();

    private boolean confirmed;
    private String lastText;
    private String currentText;
    private final Icon initIcon, acceptIcon, errorIcon;

    private final boolean closeOnClick;
    private final Consumer<String> resultHandler;
    private final Predicate<String> validator;
    private final BooleanSupplier exitHandler;
    private final BiFunction<String, Icon, Icon> errorHandler;

    protected TextInputGUI(Builder builder) {
        super(builder.title, InventoryType.ANVIL);
        this.lastText = builder.initialText;
        this.closeOnClick = builder.closeOnClick;
        this.resultHandler = builder.resultHandler;
        this.validator = builder.validator;
        this.exitHandler = builder.exitHandler;
        this.errorHandler = builder.errorHandler;
        this.initIcon = Icon.of(VALID).name(lastText).buildIcon();
        this.acceptIcon = Icon.of(VALID)
                .name(NamedTextColor.GREEN, TextDecoration.BOLD, "Confirm")
                .buildIcon();
        this.errorIcon = ERROR_ICON.action(click -> initIcon.itemNoUpdate(
                initIcon.alterItem().name(lastText).buildItem()));
    }

    /**
     * Update the text field from the anvil GUI
     * menu to the specified text.
     *
     * @param text The text to update to.
     */
    public void update(String text) {

        this.lastText = text;
        if (validator.test(text)) {
            this.currentText = text;
            this.set(RESULT_SLOT, acceptIcon);
        } else {
            this.set(RESULT_SLOT, errorHandler.apply(text, errorIcon));
        }
    }

    @Override
    protected void populate(Player player) {

        this.initIcon.action(click -> click.icon().itemNoUpdate(
                click.icon()
                        .alterItem()
                        .name(lastText)
                        .buildItem()
        ));

        this.set(0, initIcon);
        this.acceptIcon.action(click -> {

            click.playSound(Sound.UI_BUTTON_CLICK);
            this.confirmed = true;
            this.resultHandler.accept(currentText);
            if (closeOnClick) {
                click.close();
            }
        });
    }

    @Override
    protected boolean close(Player player) {
        return !confirmed && exitHandler.getAsBoolean();
    }

    /**
     * Create a new {@link Builder} for an {@link TextInputGUI}.
     *
     * @param initialText The text to initially show in the blank GUI.
     * @return The new {@link Builder}.
     */
    public static Builder input(@NotNull String initialText) {
        checkNotNull(initialText);
        return new Builder(initialText);
    }

    public static final class Builder {

        private Component title;
        private final String initialText;

        private boolean closeOnClick;
        private Consumer<String> resultHandler;
        private Predicate<String> validator = s -> true;
        private BooleanSupplier exitHandler = () -> false;
        private BiFunction<String, Icon, Icon> errorHandler = (s, i) -> i;

        Builder(String initialText) {
            this.initialText = initialText;
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
         * user has successfully accepted a valid result.
         *
         * @param closeOnClick Whether the UI should close on click.
         * @return This Builder.
         */
        public Builder closeOnClick(boolean closeOnClick) {
            this.closeOnClick = closeOnClick;
            return this;
        }

        /**
         * Set the {@link Consumer handler} that should handle the
         * result accepted by the user.
         *
         * @param resultHandler The handler.
         * @return This Builder.
         */
        public Builder resultHandler(Consumer<String> resultHandler) {
            this.resultHandler = resultHandler;
            return this;
        }

        /**
         * Set the {@link Predicate validator} that should validate
         * the result before it is accepted.
         * <p>
         *     If a result is not valid, an error icon will be shown
         *     in place of the accept icon.
         * </p>
         *
         * @param validator The validator.
         * @return This Builder.
         */
        public Builder validator(Predicate<String> validator) {
            this.validator = validator;
            return this;
        }

        /**
         * Set the {@link BooleanSupplier handler} that should handle
         * when the GUI is exited without a result being accepted.
         * <p>
         *     The return value of this handler, if {@code true}, will
         *     determine whether the UI should be automatically reopened.
         * </p>
         *
         * @param exitHandler The handler.
         * @return This Builder.
         */
        public Builder exitHandler(BooleanSupplier exitHandler) {
            this.exitHandler = exitHandler;
            return this;
        }

        /**
         * Set the {@link BiFunction handler} that should handle when
         * a result is invalid and return an icon that signifies an
         * erroneous input.
         * <p>
         *     By default, the icon is a generic one signifying an
         *     invalid result, however, details can be given on the
         *     modified icon to express what exactly is the issue with
         *     the input provided.
         * </p>
         *
         * @param errorHandler The handler.
         * @return This Builder.
         */
        public Builder errorHandler(BiFunction<String, Icon, Icon> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        /**
         * Create and open the {@link TextInputGUI} for the specified
         * {@link Player} by calling {@link GUI#open(Player)}.
         *
         * @param player The {@link Player} to open the GUI for.
         * @return The newly created {@link TextInputGUI}.
         */
        public TextInputGUI open(Player player) {
            checkNotNull(title, "missing title");
            checkNotNull(resultHandler, "missing result handler");
            TextInputGUI gui = new TextInputGUI(this);
            gui.open(player);
            return gui;
        }
    }
}
