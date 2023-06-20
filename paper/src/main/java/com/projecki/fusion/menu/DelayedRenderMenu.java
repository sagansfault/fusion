package com.projecki.fusion.menu;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * This menu is the same as the regular Menu implementation: {@link Menu} in every way except how it behaves behind the
 * scenes. This menu is used for the purpose of wanting to pull data from an external source and show it in the menu
 * every time you want to render or update it (if for some reason you can't cache it). This menu, on all render and
 * update functions, will run the supplier you provide in an abstract method to get a list of {@link Page}s to render.
 * I'll say it again, it runs every time. Once the pages have been received, the menu is rendered to the player, hence the
 * name "DelayedRender". The speed at which this menu is rendered entirely depends on the time it takes your future to
 * complete.
 */
public abstract class DelayedRenderMenu extends AbstractMenu {

    private final JavaPlugin plugin;
    private final Supplier<CompletableFuture<List<Page>>> delayedRenderRunnable;

    /**
     * Constructs a new delayed render menu with the given future supplier of a list of pages
     */
    public DelayedRenderMenu(JavaPlugin plugin) {
        this.plugin = plugin;
        this.delayedRenderRunnable = getDelayedRenderRunnable();
    }

    /**
     * Returns the delayed render runnable function for the implementing class
     *
     * @return The delayed render function
     */
    public abstract Supplier<CompletableFuture<List<Page>>> getDelayedRenderRunnable();

    /**
     * Collects pages from the future supplier and renders them once complete
     *
     * {@inheritDoc}
     */
    @Override
    public final void render(Collection<? extends Player> players, int pageIndex) {
        delayedRenderRunnable.get().thenAccept(pages -> new BukkitRunnable(){
            @Override
            public void run() {
                setPages(pages);
                DelayedRenderMenu.super.render(players, pageIndex);
            }
        }.runTask(plugin));
    }

    /**
     * Collects pages from the future supplier and renders them once complete
     *
     * {@inheritDoc}
     */
    @Override
    public final void render(Player player, int pageIndex) {
        this.render(new ArrayList<>(List.of(player)), pageIndex);
    }

    /**
     * Collects pages from the future supplier and renders them once complete
     *
     * {@inheritDoc}
     */
    @Override
    public final void render(Player player) {
        this.render(player, 0);
    }

    /**
     * Collects pages from the future supplier and renders them once complete
     *
     * {@inheritDoc}
     */
    @Override
    public final void render(Collection<? extends Player> players) {
        this.render(players, 0);
    }

    /**
     * Collects pages from the future supplier and renders them once complete
     *
     * {@inheritDoc}
     */
    @Override
    public final void sendUpdate() {
        delayedRenderRunnable.get().thenAccept(pages -> new BukkitRunnable(){
            @Override
            public void run() {
                setPages(pages);
                DelayedRenderMenu.super.sendUpdate();
            }
        }.runTask(plugin));
    }
}
