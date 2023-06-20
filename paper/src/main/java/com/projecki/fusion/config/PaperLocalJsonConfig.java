package com.projecki.fusion.config;

import com.projecki.fusion.config.local.plugin.LocalJsonPluginConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperLocalJsonConfig<T> extends LocalJsonPluginConfig<T> {

    public PaperLocalJsonConfig(Class<T> configClass, JavaPlugin plugin, String fileName) {
        super(configClass, plugin, plugin.getDataFolder().toPath(), fileName);
    }

    public PaperLocalJsonConfig(Class<T> configClass, JavaPlugin plugin) {
        super(configClass, plugin, plugin.getDataFolder().toPath());
    }
}
