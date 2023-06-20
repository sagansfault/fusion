package com.projecki.fusion.menu.deprecated.function;

import com.projecki.fusion.menu.deprecated.Menu;
import com.projecki.fusion.menu.deprecated.Page;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * @deprecated use new v2 package
 */
@Deprecated
public class RenderPaginationButtonFunction {
    @FunctionalInterface
    public interface NextPage {
        /**
         * @return A map from the slot this next-page button will be to the item representing it
         */
        Map<Integer, ItemStack> getNextPageButton(FunctionInfo info);
    }

    @FunctionalInterface
    public interface BackPage {
        /**
         * @return A map from the slot this back-page button will be to the item representing it
         */
        Map<Integer, ItemStack> getBackPageButton(FunctionInfo info);
    }

    /**
     * Contains info regarding the current page/menu state for rendering next/previous page buttons
     *
     * @param player The player viewing the menu
     * @param menu The menu containing the pages these items will be rendered on
     * @param page The page these items will be rendered on
     * @param pageIndex The index of the page these items will be rendered on
     */
    public static final record FunctionInfo(Player player, Menu menu, Page page, int pageIndex) {}
}
