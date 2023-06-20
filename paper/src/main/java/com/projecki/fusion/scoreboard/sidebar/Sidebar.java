package com.projecki.fusion.scoreboard.sidebar;

import com.projecki.fusion.object.DisableByDefault;
import com.projecki.fusion.scoreboard.ScoreboardModule;
import com.projecki.fusion.user.UserModuleId;
import com.projecki.fusion.util.concurrent.RefreshTask;
import com.projecki.unversioned.scoreboard.DisplaySlot;
import com.projecki.unversioned.scoreboard.ScoreboardService;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkElementIndex;
import static java.util.stream.Collectors.toList;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

/**
 * A {@link ScoreboardModule} that manages the text displayed
 * on the {@link DisplaySlot#SIDEBAR} for a {@link Player}.
 *
 * @since May 23, 2022
 * @author Andavin
 */
@DisableByDefault
@UserModuleId("scoreboard_sidebar")
public class Sidebar extends ScoreboardModule {

    private static final List<String> KEYS;
    private static final char COLOR_CHAR = '§';
    private static final String RESET = COLOR_CHAR + "r";

    static {

        String[] keys = new String[30];
        for (int i = 0; i < 30; i++) {

            int colors = i / 0x10 + 1;
            StringBuilder sb = new StringBuilder(colors * 2);
            for (int i1 = 0; i1 < colors; i1++) {
                sb.append(COLOR_CHAR);
                sb.append(Integer.toHexString(i % 0x10));
            }

            sb.append(RESET);
            keys[i] = sb.toString();
        }

        KEYS = List.of(keys);
    }

    private final String objId;
    private Component displayName;
    private RefreshTask displayNameRefreshTask, linesRefreshTask;
    private final List<Component> oldLines = new ArrayList<>(19);

    public Sidebar() {
        this.objId = "obj-" + OBJ_IDS.getAndIncrement();
    }

    @Override
    protected void onEnable() {

        if (this.displayName == null) {
            this.displayName = empty();
        }

        Player player = this.object().reference();
        ScoreboardService.INSTANCE.createObjective(
                player, objId, displayName, DisplaySlot.SIDEBAR);
    }

    @Override
    protected void onDisable() {
        this.clear();
        this.displayName = null;
        Player player = this.object().reference();
        ScoreboardService.INSTANCE.deleteObjective(player, objId);
    }

    /**
     * Create an auto refresh task that automatically refreshes
     * the values for this module every 1 second.
     * <p>
     *     The {@code action} is intended to make use of the
     *     following methods:
     *     <ul>
     *         <li>{@link #displayName(String)}</li>
     *         <li>{@link #displayName(Component)}</li>
     *         <li>{@link #display(String...)}</li>
     *         <li>{@link #display(Component...)}</li>
     *         <li>{@link #display(List)}</li>
     *     </ul>
     * </p>
     *
     * @param action The action that should be executed on each
     *               refresh cycle.
     */
    public void autoRefresh(Consumer<Sidebar> action) {
        this.autoRefresh(1, TimeUnit.SECONDS, action);
    }

    /**
     * Create an auto refresh task that automatically refreshes
     * the values for this module.
     * <p>
     *     The {@code action} is intended to make use of the
     *     following methods:
     *     <ul>
     *         <li>{@link #displayName(String)}</li>
     *         <li>{@link #displayName(Component)}</li>
     *         <li>{@link #display(String...)}</li>
     *         <li>{@link #display(Component...)}</li>
     *         <li>{@link #display(List)}</li>
     *     </ul>
     * </p>
     *
     * @param period The period of units to wait between each refresh.
     * @param unit The {@link TimeUnit} to use to convert {@code period}.
     * @param action The action that should be executed on each
     *               refresh cycle.
     */
    public void autoRefresh(long period, TimeUnit unit, Consumer<Sidebar> action) {
        this.refreshTask(new RefreshTask(unit.toNanos(period), () -> action.accept(this)));
    }

    /**
     * Set the display name of this sidebar to the specified text.
     *
     * @param displayName The new display name.
     */
    public void displayName(@NotNull String displayName) {
        this.displayName(text(displayName));
    }

    /**
     * Set the display name of this sidebar to the
     * specified {@link Component}.
     *
     * @param displayName The new display name.
     */
    public void displayName(@NotNull Component displayName) {

        boolean first = this.displayName == null;
        this.displayName = displayName;
        if (this.isDisabled()) {
            return;
        }

        Player player = this.object().reference();
        if (first) {
            ScoreboardService.INSTANCE.createObjective(
                    player, objId, displayName, DisplaySlot.SIDEBAR);
        } else {
            ScoreboardService.INSTANCE.updateObjectiveName(player, objId, displayName);
        }
    }

    /**
     * Display lines of text in the sidebar for the player.
     *
     * @param lines The lines to send to the player.
     */
    public void display(String... lines) {
        this.display(
                Stream.of(lines)
                        .map(Component::text)
                        .collect(toList())
        );
    }

    /**
     * Display lines of text in the sidebar for the player.
     *
     * @param lines The lines to send to the player.
     */
    public void display(Component... lines) {
        this.display(Arrays.asList(lines));
    }

    /**
     * Display lines of text in the sidebar for the player.
     *
     * @param lines The lines to send to the player.
     */
    public void display(List<Component> lines) {

        if (this.isDisabled() || lines.equals(oldLines)) {
            return;
        }

        Player player = this.object().reference();
        List<Player> players = List.of(player);
        ScoreboardService scoreboardService = ScoreboardService.INSTANCE;
        int i = 0;
        int size = lines.size();
        List<Runnable> scoreChanges = new ArrayList<>(size);
        for (; i < size; i++) {

            String key = key(i);
            String team = "team-" + i;
            Component newLine = lines.get(i);
            Component old = i < oldLines.size() ? oldLines.get(i) : null;
            if (old != null) {

                if (!old.equals(newLine)) {
                    this.oldLines.set(i, newLine);
                    scoreboardService.updateTeam(player, team, newLine, empty());
                    scoreChanges.add(() -> scoreboardService.deleteScore(players, objId, key));
                }
            } else {
                // If there was no old line for the index
                // that means that lines were added
                this.oldLines.add(newLine);
                scoreboardService.createTeam(player, team, key, newLine, empty());
            }
            // We're always adding/updating a line...
            int score = size - i;
            scoreChanges.add(() -> scoreboardService.updateScore(players, objId, key, score));
        }
        // If say they removed some lines from last time
        // we need to account for those and remove them
        int currentLines = i;
        for (i = this.oldLines.size() - 1; currentLines <= i; i--) {
            this.oldLines.remove(i); // Remove the excess
            scoreboardService.deleteTeam(player, "team-" + i); // Make sure to delete the team
            scoreboardService.deleteScore(players, objId, key(i));
        }
        // Execute all the score changes after everything else
        scoreChanges.forEach(Runnable::run);
    }

    /**
     * Clear all lines of text displayed on the sidebar.
     */
    public void clear() {

        int size = oldLines.size();
        this.oldLines.clear(); // Clear all the lines

        List<Object> packets = new ArrayList<>(size);
        Player player = this.object().reference();
        List<Player> players = List.of(player);
        ScoreboardService scoreboardService = ScoreboardService.INSTANCE;
        for (int i = size - 1; 0 <= i; i--) {
            scoreboardService.deleteTeam(player, "team-" + i);
            scoreboardService.deleteScore(players, objId, key(i));
        }
    }

    private static String key(int index) {
        checkElementIndex(index, KEYS.size(), "too many lines");
        return KEYS.get(index);
    }
}
