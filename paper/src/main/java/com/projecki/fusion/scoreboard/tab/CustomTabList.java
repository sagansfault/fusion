package com.projecki.fusion.scoreboard.tab;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.object.DisableByDefault;
import com.projecki.fusion.scoreboard.ScoreboardModule;
import com.projecki.fusion.user.UserModuleId;
import com.projecki.fusion.util.ProfileUtil;
import com.projecki.fusion.util.concurrent.RefreshTask;
import com.projecki.unversioned.PlayerInfo;
import com.projecki.unversioned.PlayerInfoAction;
import com.projecki.unversioned.scoreboard.ScoreboardService;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * A {@link ScoreboardModule} that completely fills out the
 * tab list of a {@link Player} in order to show completely
 * custom info.
 * <p>
 *     Completely filling out the tab list is required to prevent
 *     new {@link Player players} that login from being displayed and
 *     can only be overcome by enabling {@link #dynamicSize(boolean)}.
 * </p>
 *
 * @since May 23, 2022
 * @author Andavin
 */
@DisableByDefault
@UserModuleId("scoreboard_custom_tab_list")
public class CustomTabList extends ScoreboardModule {

    static {
        ScoreboardService.INSTANCE.registerInfoModifier(CustomTabList::applyModifier);
    }

    /**
     * A list of {@link PlayerInfo} that is ordered alphabetically
     * by the name in the {@link PlayerProfile}.
     * <p>
     *     The alphabetical ordering is required in order to display these
     *     {@link PlayerInfo} objects before any others on the client.
     * </p>
     */
    public static final List<PlayerInfo> BLANK_INFO;

    /**
     * The maximum amount of columns that can be referenced
     * in the {@link CustomTabList}.
     */
    public static final int COLUMNS = 4;

    /**
     * The maximum amount of rows in each column that can be
     * referenced in the {@link CustomTabList}.
     */
    public static final int ROWS = 20;

    /**
     * The total amount of rows that exist in the entire {@link CustomTabList}.
     */
    public static final int TOTAL_ROWS = COLUMNS * ROWS;

    private static final Set<UUID> BLANK_INFO_IDS;
    private static final int DYNAMIC_DISABLED = -1;

    static {

        String zero = String.valueOf((char) 0);
        List<PlayerInfo> info = new ArrayList<>(TOTAL_ROWS);
        for (char c = 0; c < TOTAL_ROWS; c++) {
            info.add(PlayerInfo.of(
                    ProfileUtil.createProfile(
                            UUID.randomUUID(),
                            zero + c + "-slot",
                            "eyJ0aW1lc3RhbXAiOjE1NzY5NzkzMDg3OTAsInByb2ZpbGVJZCI6Ijc1MTQ0NDgxOTFlNjQ1NDY4Yzk3MzlhNmUzOTU3YmViIiwicHJvZmlsZU5hbWUiOiJUaGFua3NNb2phbmciLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2FlZjMxODdjM2E0ZTc0MTE5NGIwYmU0OGI2NGY5ZmU0NWRjM2JmNWNhOWFhN2FjOTg3ZTkwMDkwZjg2Y2NiN2YifX19",
                            "A83VYINY0FKggLWGnFwyvGEVdQ9fWcQcDvie7FJNIPTgB/LLxCO5L+gGRwKuu5S6IZOmr8xjIUIXvi9LfrVOJYfsI0CVDmNfV/n+7Hs8B+Nxb+ibKsoi4mC2kPUiSr27yDB78ZOchvI/gJhOZwCMnBXsBNaPZRnhqte6xIvxMCX1q0qGBySI7f8coj+z7C+mBeNeUKI20Xs0RJcCc3eMwK5IOlg5vYlKw8HpkO/H8XBkmL1QpKUHGrFVg4keHDnQZYHPklfKZP7xGIqNuQZPDkvuzbi4PG9Rnt8mPI3YOcs4UGNNBORLprrPc69McaUPqwYVBo8r09TbRkyr//JkJmS5a2sbpB4ujoAe4VFxPccSDvU9oJla01p0jtIW1fbi74JXdKH1ku7Xbkx/lpO1tZEq4isqKBDqZ8kRTrZqJof9C+nJRhuNKvHsWXHDQMn4CI2Ri2ZSt1xLfXOF9vn1+7eEYeC9b74xNMzW/PRzxBZEDOSlzPTlkJ6xI3lApyfjlFCvP4JSG+35SRvrPlMjAor0/ur5Ce26wsGqvC4QcuGI/11DmvNMIJz34f8IQqup3JiWxonDZKfkIIQF3vsof/sKx0qkSr5pPHlwBX4bCgyUDiMHbE29EXrZ0XAegy7F0ZYseVIlS3UT5uq5xEfibWJ2LOXzRH3uqR2GwT8ztbM="
                    ),
                    "                ",
                    GameMode.ADVENTURE,
                    1
            ));
        }

        BLANK_INFO = List.copyOf(info);
        BLANK_INFO_IDS = BLANK_INFO.stream()
                .map(PlayerInfo::profile)
                .map(PlayerProfile::getId)
                .filter(Objects::nonNull)
                .collect(toUnmodifiableSet());
    }

    private int index = DYNAMIC_DISABLED;
    private final List<PlayerInfoList> lists = new ArrayList<>(COLUMNS);
    private final List<PlayerInfo> info = Arrays.asList(BLANK_INFO.toArray(PlayerInfo[]::new)); // No size modifications

    @Override
    protected void onEnable() {
        Player player = this.object().reference();
        ScoreboardService.INSTANCE.tabInfo(player, PlayerInfoAction.ADD_PLAYER, info);
    }

    @Override
    protected void onDisable() {
        Player player = this.object().reference();
        ScoreboardService.INSTANCE.tabInfo(player, PlayerInfoAction.REMOVE_PLAYER, info);
    }

    /**
     * Enable the list to only show the slots that are populated
     * with a {@link PlayerInfo}.
     * <p>
     *     In other words, the maximum row (up to {@link #TOTAL_ROWS})
     *     that is populated with a {@link PlayerInfo value}, will be
     *     the last row show.
     * </p>
     * <p>
     *     Note that this allows unwanted info to show in the list at
     *     the end such as player's logging in and NPCs.
     *     <br>
     *     This unwanted info, will be removed roughly 1 second after it
     *     appears, but on a best-effort basis.
     * </p>
     */
    void dynamicSize(boolean enable) {

        if (enable) {
            this.index = 0;
            this.determineDynamicIndex(TOTAL_ROWS - 1);
        } else {
            this.index = DYNAMIC_DISABLED;
        }
    }

    /**
     * Create an auto refresh task that automatically refreshes
     * the values for this module every 1 second.
     * <p>
     *     The {@code action} is intended to make use of the
     *     following methods:
     *     <ul>
     *         <li>{@link #set(int, PlayerInfo)}</li>
     *         <li>{@link #set(int, int, PlayerInfo)}</li>
     *         <li>{@link #set(int, List)}</li>
     *         <li>{@link #link(PlayerInfoList)}</li>
     *         <li>{@link #remove(PlayerInfoList)}</li>
     *     </ul>
     *     In addition, to the aforementioned methods, methods in
     *     stored {@link PlayerInfoList PlayerInfoLists} may be used
     *     to update this list by proxy.
     * </p>
     *
     * @param action The action that should be executed on each
     *               refresh cycle.
     */
    public void autoRefresh(Consumer<CustomTabList> action) {
        this.autoRefresh(1, TimeUnit.SECONDS, action);
    }

    /**
     * Create an auto refresh task that automatically refreshes
     * the values for this module.
     * <p>
     *     The {@code action} is intended to make use of the
     *     following methods:
     *     <ul>
     *         <li>{@link #set(int, PlayerInfo)}</li>
     *         <li>{@link #set(int, int, PlayerInfo)}</li>
     *         <li>{@link #set(int, List)}</li>
     *         <li>{@link #link(PlayerInfoList)}</li>
     *         <li>{@link #remove(PlayerInfoList)}</li>
     *     </ul>
     *     In addition, to the aforementioned methods, methods in
     *     stored {@link PlayerInfoList PlayerInfoLists} may be used
     *     to update this list by proxy.
     * </p>
     *
     * @param period The period of units to wait between each refresh.
     * @param unit The {@link TimeUnit} to use to convert {@code period}.
     * @param action The action that should be executed on each
     *               refresh cycle.
     */
    public void autoRefresh(long period, TimeUnit unit, Consumer<CustomTabList> action) {
        this.refreshTask(new RefreshTask(unit.toNanos(period), () -> action.accept(this)));
    }

    /**
     * Set a single {@link PlayerInfo} into this tab list.
     *
     * @param row The row index to set the info in.
     * @param info The info to set.
     * @throws IllegalArgumentException If the {@code row} is outside
     *                                  the bounds of this tab list.
     */
    public void set(int row, PlayerInfo info) throws IllegalArgumentException {

        checkArgument(0 <= row && row < TOTAL_ROWS, "out of bounds row: %s", row);
        PlayerInfo ordered = ensureOrdered(row, info);
        PlayerInfo previous = this.info.set(row, ordered);
        this.determineDynamicIndex(row);
        if (this.isDisabled()) {
            return;
        }

        Set<PlayerInfoAction> actions = calcUpdates(ordered, previous);
        if (actions.isEmpty()) {
            return;
        }

        Player player = this.object().reference();
        List<PlayerInfo> updates = List.of(ordered);
        ScoreboardService scoreboardService = ScoreboardService.INSTANCE;
        actions.forEach(a -> scoreboardService.tabInfo(player, a, updates));
    }

    /**
     * Set a single {@link PlayerInfo} into this tab list.
     *
     * @param x The column index to set the info in.
     * @param y The row index within the {@code column} to set the info in.
     * @param info The info to set.
     * @throws IllegalArgumentException If the {@code column} or {@code row}
     *                                  are outside the bounds of this tab list.
     */
    public void set(int x, int y, PlayerInfo info) throws IllegalArgumentException {
        checkArgument(0 <= x && x < COLUMNS, "out of bounds column: %s", x);
        checkArgument(0 <= y && y < ROWS, "out of bounds row: %s", y);
        this.set(x * ROWS + y, info);
    }

    /**
     * Set a collection of {@link PlayerInfo} into this tab list
     * starting at the initial row and progressing till either
     * the end of the collection is reached or the end of this
     * tab list is reached.
     *
     * @param row The row to start at.
     * @param info The collection of info to set.
     * @throws IllegalArgumentException If the {@code row} is outside
     *                                  the bounds of this tab list.
     */
    public void set(int row, List<PlayerInfo> info) throws IllegalArgumentException {

        if (info.isEmpty()) {
            return;
        }

        int size = info.size();
        if (size == 1) {
            this.set(row, info.get(0));
            this.determineDynamicIndex(row);
            return;
        }

        boolean enabled = this.isEnabled();
        checkArgument(0 <= row && row < TOTAL_ROWS, "out of bounds row: %s", row);
        Map<PlayerInfoAction, Set<PlayerInfo>> actions = new EnumMap<>(PlayerInfoAction.class);
        for (int i = 0; i < size; i++) {

            PlayerInfo ordered = ensureOrdered(i + row, info.get(i));
            PlayerInfo previous = this.info.set(i + row, ordered);
            if (enabled) {
                calcUpdates(ordered, previous, actions);
            }
        }

        this.determineDynamicIndex(row + size - 1);
        if (enabled) {
            Player player = this.object().reference();
            ScoreboardService scoreboardService = ScoreboardService.INSTANCE;
            actions.forEach((a, i) -> scoreboardService.tabInfo(player, a, i));
        }
    }

    /**
     * Set a collection of {@link PlayerInfo} into each
     * of the rows that it is keyed to.
     * <p>
     * Note that if any given {@link PlayerInfo} is {@code null},
     * then that row will be set to default.
     *
     * @param info The collection of info to set to.
     * @throws IllegalArgumentException If any keyed row is outside
     *                                  the bounds of this tab list.
     */
    public void set(Int2ObjectMap<PlayerInfo> info) throws IllegalArgumentException {

        int maxRow = 0;
        boolean enabled = this.isEnabled();
        Map<PlayerInfoAction, Set<PlayerInfo>> actions = new EnumMap<>(PlayerInfoAction.class);
        for (Entry<PlayerInfo> entry : info.int2ObjectEntrySet()) {

            int row = entry.getIntKey();
            maxRow = Math.max(row, maxRow);
            checkArgument(0 <= row && row < TOTAL_ROWS, "out of bounds row: %s", row);
            PlayerInfo ordered = ensureOrdered(row, entry.getValue());
            PlayerInfo previous = this.info.set(row, ordered);
            if (enabled) {
                calcUpdates(ordered, previous, actions);
            }
        }

        this.determineDynamicIndex(maxRow);
        if (enabled) {
            Player player = this.object().reference();
            ScoreboardService scoreboardService = ScoreboardService.INSTANCE;
            actions.forEach((a, i) -> scoreboardService.tabInfo(player, a, i));
        }
    }

    /**
     * Link the specified {@link PlayerInfoList} to this {@link CustomTabList}
     * and display its contained info.
     * <p>
     * Note that this also binds the specified list to this {@link CustomTabList}
     * so that it cannot be linked again to any other {@link CustomTabList}.
     *
     * @param list The {@link PlayerInfoList} to link.
     * @throws IllegalStateException If the specified {@link PlayerInfoList} is already
     *                               linked to a {@link CustomTabList}.
     * @throws IllegalArgumentException If the specified {@link PlayerInfoList} overlaps another
     *                                  {@link PlayerInfoList} within this {@link CustomTabList}.
     * @deprecated Maintained for compatibility. Use {@link #link(PlayerInfoList)}.
     */
    @Deprecated
    public void add(PlayerInfoList list) throws IllegalStateException, IllegalArgumentException {
        this.link(list);
    }

    /**
     * Link the specified {@link PlayerInfoList} to this {@link CustomTabList}
     * and display its contained info.
     * <p>
     * Note that this also binds the specified list to this {@link CustomTabList}
     * so that it cannot be linked again to any other {@link CustomTabList}.
     *
     * @param list The {@link PlayerInfoList} to link.
     * @throws IllegalStateException If the specified {@link PlayerInfoList} is already
     *                               linked to a {@link CustomTabList}.
     * @throws IllegalArgumentException If the specified {@link PlayerInfoList} overlaps another
     *                                  {@link PlayerInfoList} within this {@link CustomTabList}.
     */
    public void link(PlayerInfoList list) throws IllegalStateException, IllegalArgumentException {

        checkNotNull(list);
        if (lists.isEmpty()) {
            // Bind then, if successful, display and add
            list.bind(this); // Throws state exception
            list.updateDisplay();
            this.lists.add(list);
            return;
        }
        // Ensure none overlap
        for (PlayerInfoList other : lists) {
            checkArgument(other.compareTo(list) != 0,
                    "overlapping lists: %s overlaps %s",
                    list, other);
        }
        // Bind and display (if successful)
        list.bind(this); // Throws state exception
        list.updateDisplay();
        // Add and sort
        this.lists.add(list);
        this.lists.sort(null);
    }

    /**
     * Remove a {@link PlayerInfoList} from this tab list
     * and clear all of its display contents.
     * <p>
     * Note that this fully clears the {@link PlayerInfoList}
     * that is being removed via {@link #clear()}.
     *
     * @param list The list to remove and clear.
     */
    public void remove(PlayerInfoList list) {

        if (lists.remove(list)) {
            // Clear if it was removed to clear
            // the display from this tab list
            list.clear();
        }
    }

    /**
     * Clear all info from this tab list and replace it with
     * completely blank info.
     */
    public void clear() {

        this.lists.clear();
        for (int i = 0; i < TOTAL_ROWS; i++) {
            this.info.set(i, BLANK_INFO.get(i));
        }

        this.determineDynamicIndex(0);
        if (this.isEnabled()) {
            Player player = this.object().reference();
            ScoreboardService.INSTANCE.tabInfo(player, PlayerInfoAction.ADD_PLAYER, this.dynamicInfo());
        }
    }

    private List<PlayerInfo> dynamicInfo() {
        return index == DYNAMIC_DISABLED ? info :
                index > 0 ? info.subList(0, index) : List.of();
    }

    private void determineDynamicIndex(int referenceIndex) {

        if (index == DYNAMIC_DISABLED) {
            return;
        }

        for (int i = Math.max(referenceIndex, index); i >= 0; i--) {

            if (!BLANK_INFO.get(i).equals(info.get(i))) {
                this.index = i;
                return;
            }
        }

        this.index = 0;
    }

    @NotNull
    private static PlayerInfo ensureOrdered(int row, @Nullable PlayerInfo info) {

        if (info == null) {
            return BLANK_INFO.get(row);
        }

        PlayerInfo current = BLANK_INFO.get(row);
        if (info.profile() != null) {
            PlayerProfile profile = ProfileUtil.createProfile(
                    current.profile().getId(), current.profile().getName());
            profile.setProperties(info.profile().getProperties());
            return info.profile(profile);
        }

        return info.profile(current.profile());
    }

    private static Set<PlayerInfoAction> calcUpdates(PlayerInfo i, PlayerInfo displayed) {
        // If there is no profile on either one then all the data needs to be updated
        if (displayed == null || displayed.profile() == null || i.profile() == null ||
            !i.profile().getProperties().equals(displayed.profile().getProperties())) {
            return EnumSet.of(PlayerInfoAction.ADD_PLAYER);
        }

        Set<PlayerInfoAction> actions = EnumSet.noneOf(PlayerInfoAction.class);
        if (!displayed.displayName().equals(i.displayName())) {
            actions.add(PlayerInfoAction.UPDATE_DISPLAY_NAME);
        }

        if (displayed.gameMode() != i.gameMode()) {
            actions.add(PlayerInfoAction.UPDATE_GAME_MODE);
        }

        if (displayed.latency() != i.latency()) {
            actions.add(PlayerInfoAction.UPDATE_LATENCY);
        }

        return actions;
    }

    private static void calcUpdates(PlayerInfo i, PlayerInfo displayed, Map<PlayerInfoAction, Set<PlayerInfo>> updates) {
        // If there is no profile on either one then all the data needs to be updated
        if (displayed == null || displayed.profile() == null || i.profile() == null ||
            !i.profile().getProperties().equals(displayed.profile().getProperties())) {
            updates.computeIfAbsent(PlayerInfoAction.ADD_PLAYER, __ -> new HashSet<>()).add(i);
            return;
        }

        if (!displayed.displayName().equals(i.displayName())) {
            updates.computeIfAbsent(PlayerInfoAction.UPDATE_DISPLAY_NAME, __ -> new HashSet<>()).add(i);
        }

        if (displayed.gameMode() != i.gameMode()) {
            updates.computeIfAbsent(PlayerInfoAction.UPDATE_GAME_MODE, __ -> new HashSet<>()).add(i);
        }

        if (displayed.latency() != i.latency()) {
            updates.computeIfAbsent(PlayerInfoAction.UPDATE_LATENCY, __ -> new HashSet<>()).add(i);
        }
    }

    private static PlayerInfo applyModifier(Player player, PlayerInfo info) {

        CustomTabList tabList = FusionPaper.getUsers().get(player).get(CustomTabList.class);
        if (tabList.isDisabled() || tabList.index == DYNAMIC_DISABLED) {
            return info;
        }

        UUID id = info.profile().getId();
        return id == null || BLANK_INFO_IDS.contains(id) ? info : null;
    }
}
