package com.projecki.fusion.ui.inventory.icon.click;

import com.projecki.fusion.ui.inventory.icon.Icon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static com.projecki.fusion.ui.inventory.UIConstants.DRAG_ADD_SLOT;
import static com.projecki.fusion.ui.inventory.UIConstants.DRAG_END;
import static com.projecki.fusion.ui.inventory.UIConstants.DRAG_MASK;
import static com.projecki.fusion.ui.inventory.UIConstants.DRAG_START;
import static com.projecki.fusion.ui.inventory.UIConstants.DRAG_TYPE_BITS;
import static com.projecki.fusion.ui.inventory.UIConstants.LEFT_CLICK;
import static com.projecki.fusion.ui.inventory.UIConstants.MIDDLE_CLICK;
import static com.projecki.fusion.ui.inventory.UIConstants.RIGHT_CLICK;

/**
 * @since April 09, 2022
 * @author Andavin
 */
public final class Drag {

    private DragAction action;
    private final DragType type;
    private final Set<Icon> icons;

    public Drag(DragType type) {
        this.type = type;
        this.icons = new HashSet<>();
        this.action = DragAction.START;
    }

    /**
     * Get the {@link DragType type} of this drag.
     *
     * @return The {@link DragType}.
     */
    public DragType type() {
        return type;
    }

    /**
     * Handle the drag action that is received from the specified
     * {@link Player} involving the given slot.
     *
     * @param player The player that is performing the action.
     * @param action The {@link DragAction} that is being performed.
     * @param icon The applicable {@link Icon} to the action.
     * @param cursor The {@link ItemStack} that is carried on the cursor.
     * @return {@code true} if the drag action was handled or is invalid
     *         and the drag should be reset; {@code false} if the drag
     *         should continue to be handled.
     */
    public boolean handle(Player player, DragAction action, Icon icon, ItemStack cursor) {

        DragAction prev = this.action;
        this.action = action;
        if (prev != action && (prev != DragAction.ADD || action != DragAction.END)) {
            return true; // Reset
        }

        return switch (action) {
            case START -> {
                // Everything is validated before this method call
                // Simply proceed to the next step
                this.action = DragAction.ADD;
                yield false;
            }
            case ADD -> {

                if (canItemQuickReplace(icon, cursor, true) &&
                    (type == DragType.MIDDLE || cursor.getAmount() > icons.size())) {
                    this.icons.add(icon);
                }

                yield false;
            }
            case END -> {
                yield true;
            }
        };
    }

    public static boolean canItemQuickReplace(@Nullable Icon icon, ItemStack item, boolean allowOverflow) {
        boolean empty = icon == null || icon.isEmpty();
        return !empty && item.isSimilar(icon.item()) ?
                icon.item().getAmount() + (allowOverflow ? 0 : item.getAmount()) <= item.getMaxStackSize() :
                empty;
    }

    /**
     * Get the {@link DragType} for the given button.
     *
     * @param button The button that was used.
     * @return The applicable {@link DragType}.
     */
    public static DragType typeOf(int button) {
        int typeId = button >> DRAG_TYPE_BITS & DRAG_MASK;
        return switch (typeId) {
            case LEFT_CLICK -> DragType.LEFT;
            case RIGHT_CLICK -> DragType.RIGHT;
            case MIDDLE_CLICK -> DragType.MIDDLE;
            default -> throw new IllegalArgumentException("unknown drag type: " + typeId);
        };
    }

    /**
     * Get the {@link DragAction} for the given button.
     *
     * @param button The button that was used.
     * @return The applicable {@link DragAction}.
     */
    public static DragAction actionOf(int button) {
        int actionId = button & DRAG_MASK;
        return switch (actionId) {
            case DRAG_START -> DragAction.START;
            case DRAG_ADD_SLOT -> DragAction.ADD;
            case DRAG_END -> DragAction.END;
            default -> throw new IllegalArgumentException("unknown drag action: " + actionId);
        };
    }

    public enum DragType {
        LEFT,
        RIGHT,
        MIDDLE
    }

    public enum DragAction {
        START,
        ADD,
        END
    }
}
