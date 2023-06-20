package com.projecki.fusion.util;


import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Object that contains a cached value that is updated periodically
 * at a specified update interval and with a supplier to generate the
 * new value.
 *
 * @param <T> type of value to cache and update
 */
public class ValueCache<T> {

    private static final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);

    private final Duration updateInterval;
    private final Supplier<CompletionStage<T>> supplier;
    private final AtomicReference<T> value = new AtomicReference<>();

    private ValueCache(Duration updateInterval, Supplier<CompletionStage<T>> supplier) {
        this.updateInterval = updateInterval;
        this.supplier = supplier;

        try {
            updateValue().toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Exception generated when getting initial value for a ValueCache");
            e.printStackTrace();
        }

        scheduleTask();
    }

    private void scheduleTask() {
        executor.schedule(() -> {
            updateValue();
            scheduleTask();
        }, updateInterval.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Create a new {@link ValueCache} that periodically updates the internal value
     * to the value of the {@link CompletionStage} supplied from the supplied supplier.
     *
     * @param updateInterval how often the cached value is updated
     * @param supplier       supplier that supplies a {@link CompletionStage} that completes
     *                       with the value intended to be cached.
     * @param <T>            type of value to be cached
     * @return a new {@link ValueCache} with that caches a value of type {@code T} generated
     * from {@code supplier} that refreshes every {@code updateInterval}
     */
    public static <T> ValueCache<T> create(Duration updateInterval, Supplier<CompletionStage<T>> supplier) {
        return new ValueCache<>(updateInterval, supplier);
    }

    private CompletionStage<Void> updateValue() {
        return supplier.get().thenAccept(value::set);
    }

    /**
     * Get the cached value
     */
    public T getValue() {
        return value.get();
    }

}
