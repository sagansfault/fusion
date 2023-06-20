package com.projecki.fusion;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.PluginClassLoader;

import java.util.stream.Stream;

/**
 * @since June 23, 2022
 * @author Andavin
 */
public final class PaperBootstrap {

    /**
     * Call {@link Bootstrap#bootstrap(ClassLoader...)} using the
     * {@link PluginClassLoader class loaders} from all {@link Plugin plugins}
     * that are currently available via {@link PluginManager#getPlugins()}.
     */
    public static void bootstrap() {
        Bootstrap.bootstrap(
                Stream.of(Bukkit.getPluginManager().getPlugins())
                        .map(Plugin::getClass)
                        .map(Class::getClassLoader)
                        .toArray(ClassLoader[]::new)
        );
    }
}
