package com.projecki.fusion.chat.channel;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ChatChannel {

    private static final String PERMISSION_BASE = "fusion.chat.";

    private final String id;
    private final String permission;
    private final String defaultFormat;

    // Note, all target groups are lower case for internal comparison, they will be compared case-insensitive
    private final Set<String> targetGroup = new HashSet<>();
    private final Map<String, String> customFormats = new HashMap<>();

    private final Set<UUID> hidden = new HashSet<>();

    public ChatChannel(String id, String defaultFormat) {
        this.id = id;
        this.permission = PERMISSION_BASE + id;
        this.defaultFormat = defaultFormat;
    }

    public String getId() {
        return id;
    }

    public void setCustomFormat(String serverName, String format) {
        this.customFormats.put(serverName, format);
    }

    public void setTargetGroup(Set<String> targetGroup) {
        this.targetGroup.clear();
        this.targetGroup.addAll(targetGroup.stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toSet()));
    }

    public boolean isAcceptableTargetGroup(String originServer, String targetServer) {

        String originLower = originServer.toLowerCase(Locale.ROOT);
        String targetLower = targetServer.toLowerCase(Locale.ROOT);

        if (originLower.equals(targetLower)) {
            return true;
        }

        // target groups are all lower case for these 'contains' checks to validate
        return targetGroup.contains(originLower) && targetGroup.contains(targetLower) || targetGroup.contains("*");
    }

    public String getFormat(String serverName) {
        for (Map.Entry<String, String> entry : this.customFormats.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(serverName)) {
                return entry.getValue();
            }
        }
        return defaultFormat;
    }

    public boolean canSee(Player player) {
        return player.hasPermission(this.permission) && !this.hidden.contains(player.getUniqueId());
    }

    /**
     * Toggles the state of whether the given player can see this channel or not
     *
     * @param player The player
     * @return True if the channel is now hidden from the player, false if not.
     */
    public boolean toggleHidden(UUID player) {
        boolean didNotContain = this.hidden.add(player);
        if (!didNotContain) {
            this.hidden.remove(player);
        }
        return didNotContain;
    }

    public Set<UUID> getHidden() {
        return new HashSet<>(this.hidden);
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatChannel that = (ChatChannel) o;
        return id.equalsIgnoreCase(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
