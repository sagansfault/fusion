package com.projecki.fusion.scoreboard.tab;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.object.DisableByDefault;
import com.projecki.fusion.scoreboard.ScoreboardModule;
import com.projecki.fusion.user.UserModuleId;
import com.projecki.fusion.util.concurrent.RefreshTask;
import com.projecki.unversioned.PlayerInfo;
import com.projecki.unversioned.PlayerInfoAction;
import com.projecki.unversioned.scoreboard.ScoreboardService;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * A {@link ScoreboardModule} that allows showing custom {@link PlayerInfo}
 * to the tab list of a {@link Player} in addition to the info that already
 * shows by default.
 *
 * @since July 21, 2022
 * @author Andavin
 */
@DisableByDefault
@UserModuleId("scoreboard_player_tab_list")
public class PlayerTabList extends ScoreboardModule {

    static {
        ScoreboardService.INSTANCE.registerInfoModifier(PlayerTabList::applyModifier);
    }

    private final List<PlayerInfo> info = new ArrayList<>();
    private final List<BiFunction<PlayerTabList, PlayerInfo, PlayerInfo>> modifiers = new ArrayList<>(1);

    @Override
    protected void onEnable() {

        if (!info.isEmpty()) {
            Player player = this.object().reference();
            ScoreboardService.INSTANCE.tabInfo(player, PlayerInfoAction.ADD_PLAYER, info);
        }
    }

    @Override
    protected void onDisable() {

        if (!info.isEmpty()) {
            Player player = this.object().reference();
            ScoreboardService.INSTANCE.tabInfo(player, PlayerInfoAction.REMOVE_PLAYER, info);
        }
    }

    /**
     * Create an auto refresh task that automatically refreshes
     * the values for this module every 1 second.
     * <p>
     *     The {@code action} is intended to make use of the
     *     following methods:
     *     <ul>
     *         <li>{@link #add(PlayerInfo)}</li>
     *         <li>{@link #addAll(Collection)}</li>
     *         <li>{@link #remove(PlayerInfo)}</li>
     *         <li>{@link #removeAll(Collection)}</li>
     *         <li>{@link #replace(Collection)}</li>
     *         <li>{@link #clear()}</li>
     *     </ul>
     * </p>
     *
     * @param action The action that should be executed on each
     *               refresh cycle.
     */
    public void autoRefresh(Consumer<PlayerTabList> action) {
        this.autoRefresh(1, TimeUnit.SECONDS, action);
    }

    /**
     * Create an auto refresh task that automatically refreshes
     * the values for this module.
     * <p>
     *     The {@code action} is intended to make use of the
     *     following methods:
     *     <ul>
     *         <li>{@link #add(PlayerInfo)}</li>
     *         <li>{@link #addAll(Collection)}</li>
     *         <li>{@link #remove(PlayerInfo)}</li>
     *         <li>{@link #removeAll(Collection)}</li>
     *         <li>{@link #replace(Collection)}</li>
     *         <li>{@link #clear()}</li>
     *     </ul>
     * </p>
     *
     * @param period The period of units to wait between each refresh.
     * @param unit The {@link TimeUnit} to use to convert {@code period}.
     * @param action The action that should be executed on each
     *               refresh cycle.
     */
    public void autoRefresh(long period, TimeUnit unit, Consumer<PlayerTabList> action) {
        this.refreshTask(new RefreshTask(unit.toNanos(period), () -> action.accept(this)));
    }

    /**
     * Add the specified {@link PlayerInfo} to this tab list.
     *
     * @param info The {@link PlayerInfo} to add.
     */
    public void add(PlayerInfo info) {

        this.info.add(info);
        if (this.isEnabled()) {
            Player player = this.object().reference();
            ScoreboardService.INSTANCE.tabInfo(player, PlayerInfoAction.ADD_PLAYER, List.of(info));
        }
    }

    /**
     * Add all the specified {@link PlayerInfo} to this tab list.
     *
     * @param info The {@link PlayerInfo} to add.
     */
    public void addAll(Collection<PlayerInfo> info) {

        if (info.isEmpty()) {
            return;
        }

        this.info.addAll(info);
        if (this.isEnabled()) {
            Player player = this.object().reference();
            ScoreboardService.INSTANCE.tabInfo(player, PlayerInfoAction.ADD_PLAYER, info);
        }
    }

    /**
     * Remove the specified {@link PlayerInfo} from this tab list.
     *
     * @param info The {@link PlayerInfo} to remove.
     */
    public void remove(PlayerInfo info) {

        if (this.info.remove(info) && this.isEnabled()) {
            Player player = this.object().reference();
            ScoreboardService.INSTANCE.tabInfo(player, PlayerInfoAction.REMOVE_PLAYER, List.of(info));
        }
    }

    /**
     * Remove all the specified {@link PlayerInfo} from this tab list.
     *
     * @param info The {@link PlayerInfo} to remove.
     */
    public void removeAll(Collection<PlayerInfo> info) {

        if (this.info.addAll(info) && this.isEnabled()) {
            Player player = this.object().reference();
            ScoreboardService.INSTANCE.tabInfo(player, PlayerInfoAction.REMOVE_PLAYER, info);
        }
    }

    /**
     * Replace all the {@link PlayerInfo} in this tab list with the
     * specified {@link PlayerInfo}.
     *
     * @param info The {@link PlayerInfo} to replace with (i.e. this is the
     *             {@link PlayerInfo} that will remain after this method
     *             is called).
     */
    public void replace(Collection<PlayerInfo> info) {

        if (this.info.isEmpty()) {
            this.addAll(info);
            return;
        }

        if (this.isDisabled()) { // No need to collect removed info just replace it
            this.info.clear();
            this.info.addAll(info);
            return;
        }
        // NOTE: this is a bit roundabout and could probably be improved
        // Create a copy of the info that is not contained
        List<PlayerInfo> add = new ArrayList<>(info);
        add.removeAll(this.info);
        // Add all the new info and retain
        this.info.addAll(add);
        this.retainAll(info);
        // Send all the info
        Player player = this.object().reference();
        ScoreboardService.INSTANCE.tabInfo(player, PlayerInfoAction.ADD_PLAYER, add);
    }

    /**
     * Remove all the {@link PlayerInfo} in this tab list that is
     * <b>not</b> contained within the specified {@link PlayerInfo}.
     *
     * @param info The {@link PlayerInfo} to retain.
     */
    public void retainAll(Collection<PlayerInfo> info) {

        if (this.info.isEmpty()) {
            return;
        }

        if (this.isDisabled()) { // No need to collect removed info
            this.info.retainAll(info);
            return;
        }

        List<PlayerInfo> removed = new ArrayList<>(this.info.size());
        Iterator<PlayerInfo> itr = this.info.iterator();
        while (itr.hasNext()) {

            PlayerInfo i = itr.next();
            if (!info.contains(i)) {
                removed.add(i);
                itr.remove();
            }
        }

        if (!removed.isEmpty()) {
            Player player = this.object().reference();
            ScoreboardService.INSTANCE.tabInfo(player, PlayerInfoAction.REMOVE_PLAYER, removed);
        }
    }

    /**
     * Clear all the {@link PlayerInfo} in this tab list.
     */
    public void clear() {

        if (info.isEmpty()) {
            return;
        }

        if (this.isEnabled()) {
            Player player = this.object().reference();
            ScoreboardService.INSTANCE.tabInfo(player, PlayerInfoAction.REMOVE_PLAYER, info);
        }
        // Clear the list after removing
        this.info.clear();
    }

    /**
     * Register a modifier {@link BiFunction} that can be used to
     * modify {@link PlayerInfo} as it is sent to the {@link Player}.
     *
     * @param modifier The modifier function to register.
     */
    public void modify(BiFunction<PlayerTabList, PlayerInfo, PlayerInfo> modifier) {
        this.modifiers.add(modifier);
    }

    private static PlayerInfo applyModifier(Player player, PlayerInfo info) {

        PlayerTabList tabList = FusionPaper.getUsers().get(player).get(PlayerTabList.class);
        if (tabList.isDisabled() || tabList.modifiers.isEmpty()) {
            return info;
        }

        for (BiFunction<PlayerTabList, PlayerInfo, PlayerInfo> modifier : tabList.modifiers) {

            PlayerInfo modified = modifier.apply(tabList, info);
            if (modified != null) {
                info = modified;
            } else {
                return null; // Null means it's cancelled
            }
        }

        return info;
    }
}
