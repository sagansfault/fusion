package com.projecki.fusion.setting;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a main loader class for settings in a given instantiation. Load, set, save, check
 * settings from here and here alone.
 */
public abstract class SettingLoader {

    private final Table<UUID, Class<? extends Setting>, Setting> settings = HashBasedTable.create();
    private final Setting.Registry registry;

    public SettingLoader() {
        this.registry = new Setting.Registry();
    }

    /**
     * Looks in the local setting table for a setting of a given type and assigned to a given
     * parentId (often a player UUID). If no setting is found to be set of that type for that
     * parentId, a given default value is returned
     *
     * @param parentId The parent id to look for a setting assigned to
     * @param type The type of setting to look for
     * @param defaultValue The default value to return if no set setting was found
     * @param <T> The class type
     * @return Either the setting of that type assigned to the given id or the given default value
     */
    @SuppressWarnings("unchecked")
    public final <T extends Setting> T getSetting(UUID parentId, Class<T> type, T defaultValue) {
        Setting setting = this.settings.get(parentId, type);
        if (setting == null) {
            return defaultValue;
        }
        if (type.isInstance(setting)) {
            return (T) setting;
        } else {
            return defaultValue;
        }
    }

    /**
     * Sets a setting in the local table here in this loader for the given parent id to the given
     * value. This does not save the setting anywhere in any database.
     *
     * @param parentId The parent id to set the setting for
     * @param setting The value to set the setting to
     */
    public final void setSetting(UUID parentId, Setting setting) {
        this.settings.put(parentId, setting.getClass(), setting);
    }

    /**
     * Increments a setting along its enum ordinal path. If no next setting is found it returns
     * to the 0th ordinal setting.
     *
     * @param parentId The parent id to increment this setting for
     * @param settingType The type of this setting to increment
     * @param <T> The class type
     * @return The setting this setting has been incremented to (the new setting)
     */
    public final <T extends Setting> T incrementSetting(UUID parentId, Class<T> settingType) {
        T existing = this.getSetting(parentId, settingType, null);
        T[] settingsOfType = settingType.getEnumConstants();
        if (existing == null) {
            return settingsOfType[0];
        }

        int i = 0;
        for (T setting : settingsOfType) {
            if (setting == existing) {
                break;
            }
            i++;
        }

        int nextOrd = i + 1;
        if (nextOrd > settingsOfType.length - 1) {
            nextOrd = 0;
        }

        return settingsOfType[nextOrd];
    }

    /**
     * Loads all stored settings for the given parent id using the child implementation and
     * stores them in the local table here for manipulation and reading.
     *
     * @param parentId The parent id to get load the settings of
     * @return A future that completes once the settings have been loaded
     */
    public final CompletableFuture<Void> load(UUID parentId) {
        return this.loadImpl(parentId).thenApply(map -> {
            Map<Class<? extends Setting>, Setting> converted = new HashMap<>();

            for (Map.Entry<String, String> entry : map.entrySet()) {
                String namespace = entry.getKey();
                String id = entry.getValue();

                Class<? extends Setting> clazz = this.registry.getNamespaceRegistry().inverse().get(namespace);
                if (clazz == null) {
                    continue;
                }

                for (Setting setting : clazz.getEnumConstants()) {
                    if (setting.getId().equalsIgnoreCase(id)) {
                        converted.put(clazz, setting);
                    }
                }
            }

            converted.forEach((c, s) -> this.settings.put(parentId, c, s));
            return null;
        });
    }

    /**
     * Should load settings from wherever they are stored and return them AS IS.
     * Very little manipulation of data is required here. You should be storing the settings
     * as Strings to Strings mapped to the given parentId. The first string is the class
     * name of the setting and the second one is the setting id.
     *
     * Store them as is and retrieve them as is.
     *
     * @param parentId The parent id you should retrieve the settings of
     * @return A future of a map of setting's type class name to the setting's id
     */
    protected abstract CompletableFuture<Map<String, String>> loadImpl(UUID parentId);

    /**
     * Saves the settings associated with a given parent id using the child implementation
     *
     * @param parentId The parent id to save the setting of
     * @return A future that completes once the settings have been saved
     */
    public final CompletableFuture<Void> save(UUID parentId) {
        Map<String, String> converted = new HashMap<>();
        for (Map.Entry<Class<? extends Setting>, Setting> entry : this.settings.row(parentId).entrySet()){
            Class<? extends Setting> clazz = entry.getKey();
            Setting setting = entry.getValue();
            String namespace = this.registry.getNamespaceRegistry().get(clazz);
            if (namespace == null) {
                continue;
            }
            converted.put(namespace, setting.getId());
        }
        return this.saveImpl(parentId, converted);
    }

    /**
     * Should store the given map of strings assigned to a parent id in whatever format you feel
     * fit.
     *
     * There is no need to change the passed map. Simply store the map to each parent id.
     * FYI: The first string is the setting class name and the second is the setting id
     *
     * @param parentId The parent id to store the settings of
     * @param toSave The map of settings assigned to the passed parent id to store.
     * @return A future that completes once the settings have been saved
     */
    protected abstract CompletableFuture<Void> saveImpl(UUID parentId, Map<String, String> toSave);
}
