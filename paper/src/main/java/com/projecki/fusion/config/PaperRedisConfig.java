package com.projecki.fusion.config;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.config.redis.RedisConfig;
import com.projecki.fusion.config.serialize.Serializer;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Deprecated in favor of {@link PaperHermesConfig}. This has almost no use outside the realm of hermes' distribution.
 */
@Deprecated
public class PaperRedisConfig<T> extends RedisConfig<T> {

    public PaperRedisConfig(Serializer<T> deserializer, JavaPlugin plugin, String configName) {
        super(deserializer, plugin.getName() + ":" + configName, FusionPaper.getMessageClient(), FusionPaper.getRedisCommands());
    }
}
