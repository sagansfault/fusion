package com.projecki.fusion.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A plugin associated container class that has support for mapping a key of type K to a value of type V.
 * This container provides some chill features like {@link Optional} get methods.
 *
 * @param <K> the type of the key used by the container
 * @param <V> the type of the value used by the container
 * @param <P> the type of the plugin this instance will be associated with
 */
public class PluginAssociatedContainer<K, V, P extends JavaPlugin> extends BaseContainer<K, V> {

    /**
     * The plugin instance that created this container
     */
    @NotNull
    protected final P plugin;

    /**
     * Construct a new {@link PluginAssociatedContainer} instance.
     *
     * @param plugin       the plugin that instantiated the container
     * @param containerMap the map instance to use for the container
     */
    public PluginAssociatedContainer(@NotNull P plugin, @NotNull Map<K, V> containerMap) {
        super(containerMap);
        this.plugin = plugin;
    }

    /**
     * Construct a new {@link PluginAssociatedContainer} instance.
     * This constructor uses a plain {@link HashMap} as it's {@link Map} implementation
     *
     * @param plugin the plugin that instantiated the container
     */
    public PluginAssociatedContainer(@NotNull P plugin) {
        super(new HashMap<>());
        this.plugin = plugin;
    }

    /**
     * Get the {@link JavaPlugin} that instantiated this {@link PluginAssociatedContainer} instance.
     *
     * @return the java plugin instance that created this container
     */
    @NotNull
    public P getPlugin() {
        return plugin;
    }
}
