package com.projecki.fusion.setting;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.projecki.fusion.setting.impl.PlayerVisibilitySetting;

public interface Setting {

    /**
     * @return The id unique to this enum class for this enum setting.
     */
    String getId();

    final class Registry {

        private final BiMap<Class<? extends Setting>, String> namespaceRegistry = HashBiMap.create();

        public Registry() {
            this.registerDefaults();
        }

        private void registerDefaults() {
            this.namespaceRegistry.put(PlayerVisibilitySetting.class, "player_visibility");
        }

        BiMap<Class<? extends Setting>, String> getNamespaceRegistry() {
            return namespaceRegistry;
        }
    }
}
