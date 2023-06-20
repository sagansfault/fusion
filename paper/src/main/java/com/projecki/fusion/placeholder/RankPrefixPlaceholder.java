package com.projecki.fusion.placeholder;

import com.google.common.base.Strings;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.platform.PlayerAdapter;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;
import java.util.SortedMap;

/**
 * Placeholder for rank prefixes to send unicode prefixes to java players
 * and normal string prefixes to bedrock players
 */
public class RankPrefixPlaceholder extends PlaceholderExpansion {

    private static final String ICON_KEY = "icon";

    private final Comparator<Group> groupComparator = new TopPrefixGroupComparator();
    private PlayerAdapter<Player> playerAdapter;

    @Override
    public boolean canRegister() {
        return Bukkit.getPluginManager().isPluginEnabled("LuckPerms") &&
                Bukkit.getPluginManager().isPluginEnabled("floodgate");
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "prorank";
    }

    @Override
    public @NotNull String getAuthor() {
        return PlaceholderConstants.AUTHOR.toString();
    }

    @Override
    public @NotNull String getVersion() {
        return PlaceholderConstants.VERSION.toString();
    }


    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        final Player targetPlayer = Bukkit.getPlayer(params);
        if (targetPlayer == null) return null;

        final User permsPlayer = getPlayerAdapter().getUser(targetPlayer);

        //Get the group with the highest weight prefix
        Optional<Group> visibleGroup = permsPlayer.getInheritedGroups(permsPlayer.getQueryOptions()).stream()
                .filter(this::groupHasPrefix)
                .max(groupComparator);

        if (visibleGroup.isEmpty()) return "";

        Optional<String> groupIcon = getGroupIcon(visibleGroup.get());

        //Floodgate players cannot see custom unicode, so send them the prefix
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId()) && groupIcon.isPresent()) {
            return groupIcon.get();
        } else {
            return getGroupPrefix(visibleGroup.get()).orElse("");
        }
    }

    private Optional<String> getGroupPrefix(Group group) {
        return Optional.ofNullable(group.getCachedData().getMetaData().getPrefix());
    }

    private Optional<String> getGroupIcon(Group group) {
        return Optional.ofNullable(group.getCachedData().getMetaData().getMetaValue(ICON_KEY));
    }

    private boolean groupHasPrefix(Group group) {
        return !Strings.isNullOrEmpty(group.getCachedData().getMetaData().getPrefix());
    }

    private PlayerAdapter<Player> getPlayerAdapter() {
        if (playerAdapter == null) {
            playerAdapter = LuckPermsProvider.get().getPlayerAdapter(Player.class);
            Validate.notNull(playerAdapter, "Cannot get a player adapter from LuckPerms");
        }

        return playerAdapter;
    }

    private static class TopPrefixGroupComparator implements Comparator<Group> {

        @Override
        public int compare(Group o1, Group o2) {
            return Integer.compare(getTopPrefixWeight(o1), getTopPrefixWeight(o2));
        }

        private int getTopPrefixWeight(Group group) {
            SortedMap<Integer, String> prefixes = group.getCachedData().getMetaData().getPrefixes();

            return prefixes.isEmpty() ? Integer.MIN_VALUE : prefixes.firstKey();
        }
    }
}
