package com.projecki.fusion.ui.inventory;

import com.projecki.fusion.ui.inventory.icon.Icon;
import com.projecki.fusion.ui.inventory.icon.click.Action;
import com.projecki.fusion.ui.inventory.icon.click.Click;
import com.projecki.fusion.ui.inventory.icon.click.ClickMode;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

/**
 * A level of management for the {@link PlayerInventory}
 * while they have an {@link GUI} open.
 * <p>
 * This will mandate the behavior when interactions
 * are detected within the a {@link Player Player's}
 * own inventory.
 *
 * @since April 08, 2022
 * @author Andavin
 */
public enum ManagementState {

    /**
     * Ignore all interaction with the {@link PlayerInventory}
     * simply allowing them to pass through to the server
     * to be handled normally as if the player clicked
     * their inventory without a {@link GUI} open.
     * <p>
     * For {@link ClickMode#THROW throwing items}:
     * <ul>
     *     <li>When detected within the {@link GUI} (e.g. the 
     *     player pressed {@code Q} while hovering over a {@link Icon}
     *     in the {@link GUI}), the interaction will be handled
     *     as any other {@link Click} (no item will be dropped).</li>
     *     <li>When detected on the cursor (e.g. the player is
     *     holding an {@link Icon} and presses {@code Q} or clicks
     *     outside the {@link GUI} to drop it):
     *     <ul>
     *         <li>If the {@link Icon} originated from the {@link PlayerInventory},
     *         the {@link Icon} will be dropped as a vanilla {@link Item}.</li>
     *         <li>If the {@link Icon} originated from the {@link GUI},
     *         the interaction will be handled as any other {@link Click}
     *         (no item will be dropped).</li>
     *     </ul>
     *     </li>
     *     <li>When detected within the {@link PlayerInventory}
     *     (e.g. the player pressed {@code Q} while hovering over a 
     *     {@link Icon} in their {@link PlayerInventory}), the 
     *     {@link Icon} will be dropped as a vanilla {@link Item}</li>
     * </ul>
     */
    IGNORE,

    /**
     * Listen to all interactions with the {@link PlayerInventory},
     * but also pass through the interactions to the server.
     * <p>
     * When closing the {@link GUI} all modifications to the
     * {@link PlayerInventory} will be saved.
     * <p>
     * For {@link ClickMode#THROW throwing items}, interactions
     * will always be handled as normal {@link Click Clicks}.
     * However, if the {@link Click} does not cause the interaction
     * to be cancelled, either via a listener {@link Action} or no
     * {@link Action} being present, then the {@link Icon} will
     * be dropped as a vanilla {@link Item}.
     * <p>
     * If this level is set and all {@link Icon Icons} with the
     * open {@link GUI} are set to a listener {@link Action} or
     * no {@link Action}, then the {@link GUI} will act as if it is
     * a normal {@link Inventory}.
     *
     * @see Action
     * @see Icon#cancelByDefault(boolean)
     */
    PASS_THROUGH,

    /**
     * Listen to all interactions with the {@link PlayerInventory}
     * and do not pass through any interactions to the server.
     * <p>
     * When closing the {@link GUI} the {@link PlayerInventory}
     * will revert to the state at which it was before the {@link GUI}
     * was opened.
     * <p>
     * For {@link ClickMode#THROW throwing items}, interactions
     * will always be handled as normal {@link Click Clicks} and
     * no item will be dropped unless a drop is performed by the
     * underlying {@link Action}.
     */
    FULL
}
