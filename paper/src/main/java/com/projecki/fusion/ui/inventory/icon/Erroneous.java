package com.projecki.fusion.ui.inventory.icon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * @since April 08, 2022
 * @author Andavin
 */
public interface Erroneous {

    /**
     * The default {@link Style} for the title of an error.
     * <p>
     *     This will be used if a color is not specified.
     * </p>
     */
    Style TITLE_STYLE = Style.style(NamedTextColor.RED, TextDecoration.BOLD);

    /**
     * Show an error in response to an action for {@code 3}
     * seconds and then revert to the previous item.
     *
     * @param title The title of the error.
     */
    default void error(String title) {
        this.error(title, (List<String>) null);
    }

    /**
     * Show an error in response to an action for {@code 3}
     * seconds and then revert to the previous item.
     *
     * @param title The title of the error.
     * @param desc The description of the error.
     */
    default void error(String title, String... desc) {
        this.error(title, Arrays.asList(desc));
    }

    /**
     * Show an error in response to an action for {@code 3}
     * seconds and then revert to the previous item.
     *
     * @param title The title of the error.
     * @param desc The description of the error.
     */
    default void error(String title, @Nullable List<String> desc) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        this.error(
                serializer.deserialize(title).style(TITLE_STYLE),
                desc != null ?
                        desc.stream()
                                .map(serializer::deserialize)
                                .toArray(Component[]::new) :
                        null
        );
    }

    /**
     * Show an error in response to an action for {@code 3}
     * seconds and then revert to the previous item.
     *
     * @param title The title of the error.
     */
    default void error(Component title) {
        this.error(title, (Component[]) null);
    }

    /**
     * Show an error in response to an action for {@code 3}
     * seconds and then revert to the previous item.
     *
     * @param title The title of the error.
     * @param desc The description of the error.
     */
    void error(Component title, @Nullable Component... desc);
}
