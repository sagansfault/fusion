package com.projecki.fusion.statistic;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class StatisticLoader {

    // a table where statistic values are mapped to two values: uuid and type
    private final Table<UUID, StatisticType, Long> statistics = HashBasedTable.create();
    private final StatisticType.Registry registry;

    public StatisticLoader() {
        this.registry = new StatisticType.Registry();
    }

    /**
     * Gets a possibly present statistic value for a given statistic type.
     *
     * @param id The id to get the statistic of. Often a player
     * @param type The type of statistic type to get.
     * @return A possibly present statistic type tied to a given id
     */
    public final Optional<Long> getStatistic(UUID id, StatisticType type) {
        return Optional.ofNullable(this.statistics.get(id, type));
    }

    /**
     * Sets a statistic tied to a given id of a given type to a given value.
     *
     * @param id The id to attach this statistic to
     * @param type The type of statistic to set
     * @param value The value to set this statistic to
     */
    public final void setStatistic(UUID id, StatisticType type, long value) {
        this.statistics.put(id, type, value);
    }

    /**
     * Increment the given statistic type by the given difference of a given id. If there is no statistic already
     * present to increment, one is set with the given difference (essentially incrementing a present statistic with
     * a value of 0). Diff can be negative.
     *
     * @param id The id tied to the statistic to increment
     * @param type The type of statistic to increment or set if nothing present
     * @param diff The difference to increment the present statistic by or set if nothing present. Can be negative.
     */
    public final void incrementStatistic(UUID id, StatisticType type, long diff) {
        if (diff != 0) {
            this.setStatistic(id, type, this.getStatistic(id, type).orElse(0L) + diff);
        }
    }

    /**
     * Loads in statistics from the database and sets them in the cache. The cache is not cleared before this operation.
     *
     * @param parentId The id to get the statistics attributed to
     * @param type The type of statistic to load.
     * @return A future that completes when the statistics have been loaded
     */
    public final CompletableFuture<Void> load(UUID parentId, Class<? extends StatisticType> type) {
        Optional<String> namespaceOpt = registry.getNamespace(type);
        if (namespaceOpt.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        } else {
            String namespace = namespaceOpt.get();
            return this.loadImpl(parentId, namespace).thenApply(map -> {
                map.forEach((id, value) -> this.registry.getStatistic(type, id).ifPresent(stat -> this.statistics.put(parentId, stat, value)));
                return null;
            });
        }
    }

    /**
     * Loads all statistics attributed to a given id from the database and sets them in the cache. The cache is not
     * cleared before this operation.
     *
     * @param parentId The id to get the statistics attributed to
     * @return A future that completes when the statistics are loaded
     */
    public final CompletableFuture<Void> loadAll(UUID parentId) {
        return CompletableFuture.allOf(
                registry.getNamespaceRegistry().inverse().values()
                        .stream().map(value -> this.load(parentId, value))
                        .toArray(CompletableFuture[]::new)
        );
    }

    /**
     * Saves all statistics currently in the cache attributed to a given id and of a given type to the database.
     * This operation does NOT edit the cache
     *
     * @param parentId The id to save the statistics of
     * @param type The type of statistics to save for this id
     * @return A future that completes when the statistics are saved
     */
    public final CompletableFuture<Void> save(UUID parentId, Class<? extends StatisticType> type) {
        Optional<String> namespaceOpt = registry.getNamespace(type);
        if (namespaceOpt.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        } else {
            String namespace = namespaceOpt.get();
            Map<String, Long> toSave = this.statistics.row(parentId).entrySet().stream()
                    .filter(e -> e.getKey().getClass() == type)
                    .collect(Collectors.toMap(entry -> entry.getKey().getId(), Map.Entry::getValue));
            return this.saveImpl(parentId, namespace, toSave);
        }
    }

    /**
     * Saves all statistics currently in the cache attributed to a given id to the database.
     * This operation does NOT edit the cache
     *
     * @param parentId The id to save the statistics of
     * @return A future that completes when the statistics are saved
     */
    public final CompletableFuture<Void> saveAll(UUID parentId) {
        return CompletableFuture.allOf(registry.getNamespaceRegistry().inverse().values().stream().map(value -> this.save(parentId, value)).toArray(CompletableFuture[]::new));
    }

    protected abstract CompletableFuture<Map<String, Long>> loadImpl(UUID parentId, String namespace);

    protected abstract CompletableFuture<Void> saveImpl(UUID parentId, String namespace, Map<String, Long> statistics);
}
