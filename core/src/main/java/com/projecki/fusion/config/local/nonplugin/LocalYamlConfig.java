package com.projecki.fusion.config.local.nonplugin;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.projecki.fusion.config.local.LocalConfig;
import com.projecki.fusion.config.serialize.JacksonSerializer;

import java.io.File;

public class LocalYamlConfig<T> extends LocalConfig<T> {

    public LocalYamlConfig(Class<T> targetType, File file) {
        super(new JacksonSerializer<>(targetType, YAMLFactory::new), file);
    }
}
