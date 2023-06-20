package com.projecki.fusion.placeholder;

import com.projecki.fusion.config.impl.PlatformMessageConfig;
import com.projecki.fusion.util.PlatformSpecificMessage;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

/**
 * Placeholder that takes messages from config and sends a different one
 * depending on if the player viewing it is on bedrock of on java
 */
public class PlatformMessagePlaceholder extends PlaceholderExpansion {

    private final PlatformMessageConfig config;

    public PlatformMessagePlaceholder(PlatformMessageConfig config) {
        this.config = config;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "promsg";
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
    public boolean canRegister() {
        return Bukkit.getPluginManager().isPluginEnabled("floodgate");
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        final PlatformSpecificMessage message = config.getMessages().get(params.toLowerCase());

        if (message == null) {
            Bukkit.getLogger().warning("Missing messages configuration for key '" + params + '\'');
            return ChatColor.RED + "ERROR";
        }

        if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            return message.getBedrockMessage();
        } else {
            return message.getJavaMessage();
        }
    }
}
