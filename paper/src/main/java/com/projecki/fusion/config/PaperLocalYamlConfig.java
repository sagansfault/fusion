package com.projecki.fusion.config;

import com.projecki.fusion.config.local.plugin.LocalYamlPluginConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperLocalYamlConfig<T> extends LocalYamlPluginConfig<T> {

    public PaperLocalYamlConfig(Class<T> configClass, JavaPlugin plugin, String fileName) {
        super(configClass, plugin, plugin.getDataFolder().toPath(), fileName);
    }

    public PaperLocalYamlConfig(Class<T> configClass, JavaPlugin plugin) {
        super(configClass, plugin, plugin.getDataFolder().toPath());
    }
}
