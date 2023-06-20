package com.projecki.fusion.config.local;

import com.projecki.fusion.config.Config;
import com.projecki.fusion.config.serialize.Serializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LocalConfig<T> implements Config<T> {

    protected final Serializer<T> serializer;
    protected final File file;

    public LocalConfig(Serializer<T> serializer, File file) {
        this.serializer = serializer;
        this.file = file;

        if (!file.exists()) {
            try (InputStream is = getClass().getResourceAsStream(file.getName())) {
                if (is != null) {
                    Files.copy(is, file.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructs a local config in which the config file is a file beside the running jar file (in the same directory)
     *
     * @param serializer The serializer to construct this config loader with
     * @param mainClass The main class of the running app, usually the header class (one at the highest level)
     * @param fileName The name of the file including the extension
     * @return An optional containing the config loader from the side-by-side file if no errors were encountered
     */
    public static <K> Optional<LocalConfig<K>> fromSideBySideConfig(Serializer<K> serializer, Class<?> mainClass, String fileName) {
        File parent;
        try {
            parent = new File(mainClass.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
        } catch (URISyntaxException e) {
            return Optional.empty();
        }

        File file = new File(parent, fileName);
        if (!file.exists() || file.isDirectory()) {
            return Optional.empty();
        }

        return Optional.of(new LocalConfig<>(serializer, file));
    }

    @Override
    public CompletableFuture<Void> storeConfig(T object) {
        try {
            Files.writeString(this.file.toPath(), this.serializer.serialize(object));
        } catch (IOException e) {
            return CompletableFuture.failedFuture(new IOException("Could not store config"));
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<T>> loadConfig() {
        try {
            String s = Files.readString(this.file.toPath());
            return CompletableFuture.completedFuture(this.serializer.deserialize(s));
        } catch (IOException e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }
}
