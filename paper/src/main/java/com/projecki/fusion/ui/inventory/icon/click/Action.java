package com.projecki.fusion.ui.inventory.icon.click;

import com.projecki.fusion.ui.inventory.GUI;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * An action to execute when a {@link Click} occurs within
 * a {@link GUI}.
 * <p>
 *     By default, all {@link Action actions} are cancelled
 *     immediately regardless of handling.
 * </p>
 *
 * @since April 08, 2022
 * @author Andavin
 */
@FunctionalInterface
public interface Action extends Consumer<Click> {

    /**
     * Handle the {@link Click} that occurred within
     * a {@link GUI}.
     *
     * @param click The {@linkplain Click} to handle.
     */
    @Override
    void accept(Click click);

    @NotNull
    @Override
    default Action andThen(@NotNull Consumer<? super Click> after) {
        return click -> {
            this.accept(click);
            after.accept(click);
        };
    }
}
