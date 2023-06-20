package com.projecki.fusion.menu.slot;

/**
 * Specific flags that apply to a specific slot in an inventory
 */
public enum SlotFlag {
    /**
     * Marks that the player should be able to put items in this slot
     */
    PLACE_INTO,
    /**
     * Marks that the player should be able to take items from this slot
     */
    TAKE_FROM,
    /**
     * Marks that the player should be able to take items from this slot as well as place items into this slot
     */
    TAKE_AND_PLACE
}
