package com.projecki.fusion.ui.inventory.icon.click;


import com.projecki.fusion.ui.inventory.UIConstants;
import org.bukkit.event.inventory.ClickType;

import static com.projecki.fusion.ui.inventory.UIConstants.DRAG_ADD_SLOT;
import static com.projecki.fusion.ui.inventory.UIConstants.DRAG_END;
import static com.projecki.fusion.ui.inventory.UIConstants.DRAG_MASK;
import static com.projecki.fusion.ui.inventory.UIConstants.DRAG_START;
import static com.projecki.fusion.ui.inventory.UIConstants.DROP_ALL;
import static com.projecki.fusion.ui.inventory.UIConstants.DROP_ONE;
import static com.projecki.fusion.ui.inventory.UIConstants.LEFT_CLICK;
import static com.projecki.fusion.ui.inventory.UIConstants.MIDDLE_CLICK;
import static com.projecki.fusion.ui.inventory.UIConstants.NUMBER_KEY_1;
import static com.projecki.fusion.ui.inventory.UIConstants.NUMBER_KEY_9;
import static com.projecki.fusion.ui.inventory.UIConstants.OUTSIDE_WINDOW_SLOT;
import static com.projecki.fusion.ui.inventory.UIConstants.RIGHT_CLICK;
import static com.projecki.fusion.ui.inventory.UIConstants.SWAP_OFFHAND_KEY;

/**
 * An enum to represent the mode that is sent in the click
 * packet from the client to direct what type of click was
 * intended by the client's click.
 *
 * @since April 08, 2022
 * @author Andavin
 */
public enum ClickMode {

    /**
     * A click that causes the item in the clicked slot
     * to be swapped with the item on the cursor.
     * <p>
     * This is also known as {@code PICKUP}
     */
    CLICK { // Pickup

        @Override
        public ClickType getType(int button, int slot) {
            // For some reason we need a checks here for 3 and 4 also even
            // though it should only ever be left or right click (i.e. 0 or 1)
            return switch (button) {
                case LEFT_CLICK -> slot == OUTSIDE_WINDOW_SLOT ?
                        ClickType.WINDOW_BORDER_LEFT : ClickType.LEFT;
                case RIGHT_CLICK -> slot == OUTSIDE_WINDOW_SLOT ?
                        ClickType.WINDOW_BORDER_RIGHT : ClickType.RIGHT;
                default -> ClickType.UNKNOWN;
            };
        }
    },

    /**
     * A click that causes the item in the clicked slot
     * to be moved to the alternate inventory or, in cases
     * such as armor, to equip the item.
     * <p>
     * This is also known as {@code QUICK_MOVE}
     */
    SHIFT_CLICK { // Quick Move

        @Override
        public ClickType getType(int button, int slot) {
            return switch (button) {
                case LEFT_CLICK -> ClickType.SHIFT_LEFT;
                case RIGHT_CLICK -> ClickType.SHIFT_RIGHT;
                default -> ClickType.UNKNOWN;
            };
        }
    },

    /**
     * A click caused by a number on the keyboard (1-9)
     * being pressed and causes the slot that the cursor
     * is over to be swapped with the hotbar slot associated
     * with the number key.
     * <p>
     * This is also known as {@code SWAP}
     */
    KEY_PRESS { // Swap

        @Override
        public ClickType getType(int button, int slot) {
            if (button == SWAP_OFFHAND_KEY) {
                return ClickType.SWAP_OFFHAND;
            } else if (NUMBER_KEY_1 <= button && button <= NUMBER_KEY_9) {
                return ClickType.NUMBER_KEY;
            } else {
                return ClickType.UNKNOWN;
            }
        }
    },

    /**
     * A creative only click that is causes the item
     * in the current slot to be copied onto the cursor.
     */
    CLONE { // Clone

        @Override
        public ClickType getType(int button, int slot) {
            // For some reason we need a check here for 0 also
            // even though it should only ever be middle click (i.e. 2)
            return switch (button) {
                case MIDDLE_CLICK, 0 -> ClickType.MIDDLE;
                default -> ClickType.UNKNOWN;
            };
        }
    },

    /**
     * A click caused by the drop keybinding being
     * activated on the current slot.
     */
    THROW { // Throw

        @Override
        public ClickType getType(int button, int slot) {
            return switch (button) {
                case DROP_ONE -> ClickType.DROP;
                case DROP_ALL -> ClickType.CONTROL_DROP;
                default -> ClickType.UNKNOWN;
            };
        }
    },

    /**
     * A click that is sent in succession with other
     * clicks indicating that the player dragged items
     * across the inventory and the items were split
     * in an appropriate manner into each slot.
     * <p>
     * This is also known as {@code QUICK_CRAFT}
     */
    DRAG { // Quick Craft

        @Override
        public ClickType getType(int button, int slot) {
            return switch (button & DRAG_MASK) { // Pretty sure this literally can't fail
                case DRAG_START, DRAG_ADD_SLOT, DRAG_END -> ClickType.UNKNOWN;
                default -> throw new IllegalArgumentException("unknown button " +
                                                              button + " for mode " + this);
            };
        }
    },

    /**
     * A click caused by quickly clicking a slot multiple
     * times and has effects on all items of the type in
     * the current slot.
     * <p>
     * This is also known as {@code PICKUP_ALL}
     */
    DOUBLE_CLICK { // Pickup All

        @Override
        public ClickType getType(int button, int slot) {
            return button == UIConstants.DOUBLE_CLICK ?
                    ClickType.DOUBLE_CLICK : ClickType.UNKNOWN;
        }
    };

    /**
     * Get the {@link ClickType} for the protocol button that
     * was clicked by the client.
     *
     * @param button The button clicked by the client.
     * @param slot The slot that was clicked by the client within the inventory.
     * @return The {@link ClickType} for the button and slot.
     */
    public abstract ClickType getType(int button, int slot);
}
