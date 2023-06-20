package com.projecki.fusion.config;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.config.redis.HermesConfig;
import com.projecki.fusion.config.serialize.Serializer;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperHermesConfig<T> extends HermesConfig<T> {

    public PaperHermesConfig(Serializer<T> deserializer, JavaPlugin plugin, String configName) {
        super(deserializer, FusionPaper.getServerInfo().get().getHermesOrganization(), plugin.getName(), configName,
                FusionPaper.getMessageClient(), FusionPaper.getRedisCommands());
    }
}
