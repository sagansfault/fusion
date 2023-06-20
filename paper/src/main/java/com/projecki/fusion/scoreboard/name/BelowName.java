package com.projecki.fusion.scoreboard.name;

import com.projecki.fusion.object.DisableByDefault;
import com.projecki.fusion.scoreboard.ScoreboardModule;
import com.projecki.fusion.user.UserModuleId;
import com.projecki.fusion.util.concurrent.RefreshTask;
import com.projecki.unversioned.scoreboard.DisplaySlot;
import com.projecki.unversioned.scoreboard.ScoreboardService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.function.Predicate.not;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

/**
 * A {@link ScoreboardModule} that manages the score and text
 * displayed directly above a {@link Player Player's} head
 * beneath their display name.
 *
 * @since May 23, 2022
 * @author Andavin
 */
@DisableByDefault
@UserModuleId("scoreboard_below_name")
public class BelowName extends ScoreboardModule {

    private static final String OBJ_ID = "sb-below-name";
    private int score;
    private Component displayName;

    @Override
    protected void onEnable() {

        if (this.displayName == null) {
            this.displayName = empty();
        }

        Player player = this.object().reference();
        ScoreboardService.INSTANCE.createObjective(
                player, OBJ_ID, displayName, DisplaySlot.BELOW_NAME);
        if (score != 0) {
            this.score(score); // Set the score to the current value to update it
        }
    }

    @Override
    protected void onDisable() {
        Player player = this.object().reference();
        ScoreboardService.INSTANCE.deleteObjective(player, OBJ_ID);
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
     *         <li>{@link #score(int)}</li>
     *     </ul>
     * </p>
     *
     * @param action The action that should be executed on each
     *               refresh cycle.
     */
    public void autoRefresh(Consumer<BelowName> action) {
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
     *         <li>{@link #score(int)}</li>
     *     </ul>
     * </p>
     *
     * @param period The period of units to wait between each refresh.
     * @param unit The {@link TimeUnit} to use to convert {@code period}.
     * @param action The action that should be executed on each
     *               refresh cycle.
     */
    public void autoRefresh(long period, TimeUnit unit, Consumer<BelowName> action) {
        this.refreshTask(new RefreshTask(unit.toNanos(period), () -> action.accept(this)));
    }

    /**
     * Set the {@link Component name} of the score.
     *
     * @param displayName The display name to set to.
     */
    public void displayName(@NotNull String displayName) {
        this.displayName(text(displayName));
    }

    /**
     * Set the {@link Component name} of the score.
     *
     * @param displayName The display name to set to.
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
                    player, OBJ_ID, displayName, DisplaySlot.BELOW_NAME);
        } else {
            ScoreboardService.INSTANCE.updateObjectiveName(player, OBJ_ID, displayName);
        }
    }

    /**
     * Set the score that displays prior to the display name.
     *
     * @param score The score to set to.
     */
    public void score(int score) {

        this.score = score;
        if (this.isDisabled()) {
            return;
        }

        Player player = this.object().reference();
        List<Player> players = (List<Player>) Bukkit.getOnlinePlayers().stream()
                .filter(not(player::equals))
                .toList();
        //noinspection deprecation
        ScoreboardService.INSTANCE.updateScore(players, OBJ_ID, player.getDisplayName(), score);
    }

    /**
     * Update the display name and score for the {@link Player}
     * to another specified online {@link Player}.
     *
     * @param other The other player to send the update to
     */
    public void update(Player other) {

        if (this.isEnabled()) {
            Player player = this.object().reference();
            //noinspection deprecation
            ScoreboardService.INSTANCE.updateScore(List.of(other), OBJ_ID, player.getDisplayName(), score);
        }
    }
}
