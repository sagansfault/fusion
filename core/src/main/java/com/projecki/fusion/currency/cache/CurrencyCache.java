package com.projecki.fusion.currency.cache;

import com.projecki.fusion.currency.Currency;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CurrencyCache {

    private final Map<Currency, Map<UUID, Long>> balances = new HashMap<>();


    /**
     * Get the cached balance for the specified {@link UUID}.
     *
     * @param currency the currency type to get
     * @param uuid         the uuid to get the balance for
     *
     * @return the cached balance wrapped in an optional
     */
    public Optional<Long> getCachedBalance(@NotNull Currency currency, @NotNull UUID uuid) {

        // make sure cache exists for currency
        assertCacheExists(currency);

        return Optional.ofNullable(balances.get(currency).get(uuid));
    }


    /**
     * Remove the cached balance for the specified {@link UUID}, if cached
     *
     * @param uuid the uuid to clear the cached balance for
     */
    public void clear(@NotNull UUID uuid) {
        balances.values().forEach(cache -> cache.remove(uuid));
    }

    /**
     * Update the cached balance for the specified {@link UUID}.
     *
     * @param currency currency type to update
     * @param uuid         the uuid to update the balance for
     *
     * @return a future that completes when the balance has updated
     */
    public CompletableFuture<Void> updateBalance(@NotNull Currency currency, @NotNull UUID uuid) {

        // make sure cache exists for currency
        assertCacheExists(currency);

        return currency.getBalance(uuid)
                .thenApply(balance -> {
                    if (balance != null) {
                        setCacheBalance(currency, uuid, balance);
                    }
                    return null;
                });
    }

    /**
     * Update the cached balance for the specified {@link UUID} and return it
     *
     * @param currency the currency type to update
     * @param uuid         the uuid to update the balance for
     *
     * @return a future that completes when the balance has been updated, with the updated balance
     */
    public CompletableFuture<Optional<Long>> updateAndGetBalance(@NotNull Currency currency, @NotNull UUID uuid) {
        return updateBalance(currency, uuid)
                .thenApply(_v -> getCachedBalance(currency, uuid));
    }

    /**
     * Set the cached balance for a specific uuid
     *
     * @param currency the currency type to set the cache balance for
     * @param uuid         the uuid to set the balance for
     * @param balance      the cached balance
     */
    protected void setCacheBalance(@NotNull Currency currency, @NotNull UUID uuid, long balance) {
        assertCacheExists(currency);

        // prevent negative balances
        if (balance < 0) return;

        balances.get(currency).put(uuid, balance);
    }

    protected final void assertCacheExists(@NotNull Currency currency) {
        if (!balances.containsKey(currency)) {
            balances.put(currency, new HashMap<>());
        }
    }
}
