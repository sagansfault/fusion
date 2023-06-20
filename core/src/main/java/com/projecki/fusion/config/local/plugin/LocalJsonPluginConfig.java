package com.projecki.fusion.config.local.plugin;


import com.projecki.fusion.config.serialize.JsonSerializer;

import java.nio.file.Path;

public class LocalJsonPluginConfig<T> extends LocalPluginConfig<T> {

    public LocalJsonPluginConfig(Class<T> targetType, Object plugin, Path dataDir, String fileName) {
        super(new JsonSerializer<>(targetType), plugin, dataDir, fileName);
    }

    public LocalJsonPluginConfig(Class<T> targetType, Object plugin, Path dataDir) {
        super(new JsonSerializer<>(targetType), plugin, dataDir, "config.yml");
    }
}
