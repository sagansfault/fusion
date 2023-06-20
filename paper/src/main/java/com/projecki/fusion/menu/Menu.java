package com.projecki.fusion.menu;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Menu extends AbstractMenu {

    /**
     * Construct a new menu with the given pages
     *
     * @param pages The pages to construct the menu from.
     */
    public Menu(Page... pages) {
        super.setPages(new ArrayList<>(List.of(pages)));
        for (Page page : super.getPages()) {
            page.attachParent(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void render(Collection<? extends Player> players, int pageIndex) {
        super.render(players, pageIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void render(Player player, int pageIndex) {
        super.render(player, pageIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void render(Player player) {
        super.render(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void render(Collection<? extends Player> players) {
        super.render(players);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void sendUpdate() {
        super.sendUpdate();
    }
}
