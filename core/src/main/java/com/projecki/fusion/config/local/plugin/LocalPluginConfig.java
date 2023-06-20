package com.projecki.fusion.config.local.plugin;


import com.projecki.fusion.config.local.LocalConfig;
import com.projecki.fusion.config.serialize.Serializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalPluginConfig<T> extends LocalConfig<T> {

    private final Object plugin;
    private final Path dataDir;
    private final String fileName;

    public LocalPluginConfig(Serializer<T> serializer, Object plugin, Path dataDir, String fileName) {
        super(serializer, new File(dataDir.toFile(), fileName));
        this.plugin = plugin;
        this.dataDir = dataDir;
        this.fileName = fileName;

        this.copyDefaultConfig();
    }

    private void copyDefaultConfig() {
        if (!dataDir.toFile().exists()) {
            dataDir.toFile().mkdir();
        }

        if (super.file.exists()) {
            return;
        }

        try (InputStream in = plugin.getClass().getResourceAsStream("/" + fileName)) {
            if (in == null) {
                // create empty file if nothing to copy. Reading will be null until written to
                Files.createFile(super.file.toPath());
            } else {
                Files.copy(in, super.file.toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
