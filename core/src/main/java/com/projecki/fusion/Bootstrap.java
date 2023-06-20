package com.projecki.fusion;

import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

/**
 * @since June 23, 2022
 * @author Andavin
 */
public final class Bootstrap {

    /**
     * The {@link Reflections} instance that holds all the
     * metadata that has been scanned.
     */
    public static final Reflections REFLECTIONS = new Reflections("com.projecki", Scanners.values());

    /**
     * Bootstrap all metadata using {@link Reflections} on the
     * specified {@link ClassLoader} for the package {@code com.projecki}.
     *
     * @param classLoaders The {@link ClassLoader class loaders} to search URLs for.
     */
    public static void bootstrap(ClassLoader... classLoaders) {
        Configuration config = new ConfigurationBuilder()
                .forPackage("com.projecki", classLoaders)
                .addClassLoaders(classLoaders)
                .addScanners(Scanners.values());
        REFLECTIONS.merge(new Reflections(config));
    }
}
