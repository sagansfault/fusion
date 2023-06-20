package com.projecki.fusion.config.local.plugin;


import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.projecki.fusion.config.serialize.JacksonSerializer;

import java.nio.file.Path;

public class LocalYamlPluginConfig<T> extends LocalPluginConfig<T> {

    public LocalYamlPluginConfig(Class<T> configClass, Object plugin, Path dataDir, String fileName) {
        super(new JacksonSerializer<>(configClass, YAMLFactory::new), plugin, dataDir, fileName);
    }

    public LocalYamlPluginConfig(Class<T> configClass, Object plugin, Path dataDir) {
        super(new JacksonSerializer<>(configClass, YAMLFactory::new), plugin, dataDir, "config.yml");
    }
}
