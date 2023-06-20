package com.projecki.fusion.placeholder;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.network.PlayerStorage;
import com.projecki.fusion.network.redis.RedisPlayerStorage;
import com.projecki.fusion.server.BasicServerData;
import com.projecki.fusion.server.ServerDataStorage;
import com.projecki.fusion.util.ValueCache;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class PlayerCountPlaceholder extends PlaceholderExpansion {

    private final PlayerStorage playerStorage = new RedisPlayerStorage(FusionPaper.getRedisCommands());
    private final ValueCache<Long> networkCount = ValueCache.create(Duration.ofMillis(500), playerStorage::getPlayerCount);

    private final Map<String, ValueCache<Integer>> playerCounts = new HashMap<>();
    private final ServerDataStorage serverDataStorage;

    public PlayerCountPlaceholder(ServerDataStorage serverDataStorage) {
        this.serverDataStorage = serverDataStorage;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "playercount";
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
        //params = params.toLowerCase();

        switch (params.toLowerCase()) {
            case "all" -> {
                return String.valueOf(networkCount.getValue());
            }
            case "here" -> {
                return String.valueOf(Bukkit.getOnlinePlayers().size());
            }
            default -> {
                var cache = playerCounts.get(params);

                // if there is no player count cache for this server, we need to make one
                if (cache == null) {
                    @NotNull String finalParams = params;
                    cache = ValueCache.create(Duration.ofMillis(500),
                            () -> serverDataStorage.getInfo(finalParams, BasicServerData.class)
                                    .thenApply(opt -> opt.flatMap(BasicServerData::getPlayerCount))
                                    .thenApply(opt -> opt.orElse(-1)));
                    playerCounts.put(params, cache);
                    return "...";
                } else {
                    return String.valueOf(cache.getValue());
                }


            }
        }
    }
}
