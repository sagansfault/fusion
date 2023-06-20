package com.projecki.fusion.scoreboard.name;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.object.DisableByDefault;
import com.projecki.fusion.scoreboard.ScoreboardModule;
import com.projecki.fusion.user.UserModuleId;
import com.projecki.fusion.util.concurrent.RefreshTask;
import com.projecki.unversioned.scoreboard.ScoreboardService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

/**
 * A {@link ScoreboardModule} that manages the text displayed
 * before and after (i.e. prefix and suffix) a {@link Player Player's}
 * name displayed over their head.
 *
 * @author Andavin
 * @since May 23, 2022
 */
@DisableByDefault
@UserModuleId("scoreboard_player_name")
public class PlayerName extends ScoreboardModule {

    private static final PlayerNameFunction IDENTITY = PlayerNameFunction.identity();
    private final String teamId;
    private Component prefix = empty(), suffix = empty();
    private PlayerNameFunction prefixMod = IDENTITY, suffixMod = IDENTITY;

    public PlayerName() {
        this.teamId = "a-team-" + OBJ_IDS.getAndIncrement();
    }

    @Override
    protected void onEnable() {
        // Send team to this player also
        Player player = this.object().reference();
        //noinspection deprecation
        String name = player.getDisplayName();
        ScoreboardService scoreboardService = ScoreboardService.INSTANCE;
        scoreboardService.createTeam(player, teamId, name, prefix, suffix);
        FusionPaper.getUsers().getOnline().forEach(other -> {

            Player online = other.reference();
            if (player.equals(online)) {
                return;
            }
            // Update the other player with this player's info
            scoreboardService.deleteTeam(online, teamId); // Remove the team just to be sure there's no conflicts
            scoreboardService.createTeam(
                    online,
                    teamId,
                    name,
                    prefixMod.apply(online, prefix),
                    suffixMod.apply(online, suffix)
            );
            // Update this player with the other player's info
            other.getOptional(PlayerName.class).ifPresent(playerName -> {
                scoreboardService.deleteTeam(player, playerName.teamId);
                //noinspection deprecation
                scoreboardService.createTeam(
                        player,
                        playerName.teamId,
                        online.getDisplayName(),
                        prefixMod.apply(player, playerName.prefix),
                        suffixMod.apply(player, playerName.suffix)
                );
            });
        });
    }

    @Override
    protected void onDisable() {
        ScoreboardService scoreboardService = ScoreboardService.INSTANCE;
        Bukkit.getOnlinePlayers().forEach(player -> scoreboardService.deleteTeam(player, teamId));
    }

    /**
     * Create an auto refresh task that automatically refreshes
     * the values for this module every 1 second.
     * <p>
     *     This is intended to be used in combination with the
     *     following methods:
     *     <ul>
     *         <li>{@link #update(String, String)}</li>
     *         <li>{@link #update(Component, Component)}</li>
     *     </ul>
     * </p>
     *
     * @param action The action that should be executed on each
     *               refresh cycle.
     */
    public void autoRefresh(Consumer<PlayerName> action) {
        this.autoRefresh(1, TimeUnit.SECONDS, action);
    }

    /**
     * Create an auto refresh task that automatically refreshes
     * the values for this module.
     * <p>
     *     This is intended to be used in combination with the
     *     following methods:
     *     <ul>
     *         <li>{@link #update(String, String)}</li>
     *         <li>{@link #update(Component, Component)}</li>
     *     </ul>
     * </p>
     *
     * @param period The period of units to wait between each refresh.
     * @param unit   The {@link TimeUnit} to use to convert {@code period}.
     * @param action The action that should be executed on each
     *               refresh cycle.
     */
    public void autoRefresh(long period, TimeUnit unit, Consumer<PlayerName> action) {
        this.refreshTask(new RefreshTask(unit.toNanos(period), () -> action.accept(this)));
    }

    /**
     * Set the modifier for the players prefix from
     * the perspective of another player.
     *
     * @param mod The mod to set to.
     */
    public void prefixMod(PlayerNameSupplier mod) {
        this.prefixMod = mod != null ? (p, t) -> mod.apply(p) : IDENTITY;
    }

    /**
     * Set the modifier for the players prefix from
     * the perspective of another player.
     *
     * @param mod The mod to set to.
     */
    public void prefixMod(PlayerNameFunction mod) {
        this.prefixMod = mod != null ? mod : IDENTITY;
    }

    /**
     * Set the modifier for the players suffix from
     * the perspective of another player.
     *
     * @param mod The mod to set to.
     */
    public void suffixMod(PlayerNameSupplier mod) {
        this.suffixMod = mod != null ? (p, t) -> mod.apply(p) : IDENTITY;
    }

    /**
     * Set the modifier for the players suffix from
     * the perspective of another player.
     *
     * @param mod The mod to set to.
     */
    public void suffixMod(PlayerNameFunction mod) {
        this.suffixMod = mod != null ? mod : IDENTITY;
    }

    /**
     * Update the prefix and suffix for the player to
     * every player that is currently online.
     *
     * @param prefix The prefix to update to.
     * @param suffix The suffix to update to.
     */
    public void update(@Nullable String prefix, @Nullable String suffix) {
        this.update(
                prefix != null ? text(prefix) : empty(),
                suffix != null ? text(suffix) : empty()
        );
    }

    /**
     * Update the prefix and suffix for the player to
     * every player that is currently online.
     *
     * @param prefix The prefix to update to.
     * @param suffix The suffix to update to.
     */
    public void update(@NotNull Component prefix, @NotNull Component suffix) {

        this.prefix = prefix;
        this.suffix = suffix;
        if (this.isDisabled()) {
            return;
        }

        Player player = this.object().reference();
        //noinspection deprecation
        String name = player.getDisplayName();
        ScoreboardService scoreboardService = ScoreboardService.INSTANCE;
        Bukkit.getOnlinePlayers().forEach(other -> {

            if (!other.equals(player)) {
                // Update the other player with this player's info
                scoreboardService.deleteTeam(other, teamId); // Delete the team just to be sure there's no conflicts
                scoreboardService.createTeam(
                        other, teamId, name,
                        prefixMod.apply(other, prefix),
                        suffixMod.apply(other, suffix)
                );
            }
        });
    }

    public interface PlayerNameSupplier extends Function<Player, Component> {

        /**
         * {@inheritDoc}
         *
         * @param player The player that the text is being
         *               shown to; this is <b>not</b> the player
         *               that the name of which is being shown.
         * @return The text to show.
         */
        @Override
        Component apply(Player player);
    }

    public interface PlayerNameFunction extends BiFunction<Player, Component, Component> {

        /**
         * {@inheritDoc}
         *
         * @param player  The player that the text is being
         *                shown to; this is <b>not</b> the player
         *                that the name of which is being shown.
         * @param current The current text. May be {@link Component#empty()}.
         * @return The new text to show.
         */
        @Override
        Component apply(Player player, @NotNull Component current);

        /**
         * Analogous with {@link Function#identity()} creates
         * a function that simply returns the argument.
         *
         * @return An identity {@link PlayerNameFunction}.
         */
        static PlayerNameFunction identity() {
            return (p, t) -> t;
        }
    }
}
