package com.projecki.fusion.menu.lib;

import com.projecki.fusion.menu.button.ButtonFunction;

import java.util.UUID;

/**
 * Holds all implementation regarding what should happen when a user chooses 1 of the 2 (3) options: confirm, deny, or
 * close the menu.
 */
public interface ConfirmAction {

    /**
     * The implementation that should run if the action is confirmed
     *
     * @param info The info relating to the click
     */
    void onConfirm(ButtonFunction.ClickInfo info);

    /**
     * The implementation that should run if the action is denied.
     *
     * @param info The info relating to the click
     */
    void onDeny(ButtonFunction.ClickInfo info);

    /**
     * The implementation that should run if the menu is closed
     */
    default void onClose(UUID playerId) {}
}
