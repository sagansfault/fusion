package com.projecki.fusion.statistic;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.projecki.fusion.statistic.impl.BreakoutStatistic;
import com.projecki.fusion.statistic.impl.DisastersStatistic;
import com.projecki.fusion.statistic.impl.NetworkWideStatistic;
import com.projecki.fusion.statistic.impl.SquidgameStatistic;

import java.util.Optional;

public interface StatisticType {

    /**
     * Returns the id specific to this statistic type. In a sense this could be the enum name, but it'd be nice to keep
     * those subject to change. These only have to be unique across the other ids of your statistic enum.
     *
     * @return The unique is for this statistic.
     */
    String getId();

    final class Registry {

        private final BiMap<Class<? extends StatisticType>, String> namespaceRegistry = HashBiMap.create();

        public Registry() {
            this.registerDefaults();
        }

        private void registerDefaults() {

            this.namespaceRegistry.put(NetworkWideStatistic.class, "network_wide");

            this.namespaceRegistry.put(SquidgameStatistic.class, "squidgame");
            this.namespaceRegistry.put(BreakoutStatistic.class, "breakout");
            this.namespaceRegistry.put(DisastersStatistic.class, "disasters");

        }

        BiMap<Class<? extends StatisticType>, String> getNamespaceRegistry() {
            return namespaceRegistry;
        }

        Optional<String> getNamespace(Class<? extends StatisticType> type) {
            return Optional.ofNullable(this.namespaceRegistry.get(type));
        }

        Optional<Class<? extends StatisticType>> getType(String namespace) {
            return Optional.ofNullable(this.namespaceRegistry.inverse().get(namespace));
        }

        <T extends StatisticType> Optional<T> getStatistic(Class<T> type, String id) {
            for (T enumConstant : type.getEnumConstants()) {
                if (enumConstant.getId().equalsIgnoreCase(id)) {
                    return Optional.of(enumConstant);
                }
            }
            return Optional.empty();
        }
    }
}
