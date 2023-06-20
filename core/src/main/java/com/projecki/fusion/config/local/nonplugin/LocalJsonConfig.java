package com.projecki.fusion.config.local.nonplugin;

import com.projecki.fusion.config.local.LocalConfig;
import com.projecki.fusion.config.serialize.JsonSerializer;

import java.io.File;

public class LocalJsonConfig<T> extends LocalConfig<T> {

    public LocalJsonConfig(Class<T> targetType, File file) {
        super(new JsonSerializer<>(targetType), file);
    }
}
