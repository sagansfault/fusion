package com.projecki.fusion.currency.storage;

import com.projecki.fusion.currency.Currency;
import com.projecki.fusion.currency.CurrencyPair;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Store and retrieve money for players
 * Supports CompletableFutures for blocking operations
 */
public interface CurrencyStorage {

    /**
     * Get the stored balance for the player.
     * NOTE: Assume future will be executed async of tick loop.
     *
     * @param currency the currency to get the balance for
     * @param uuid     the id of the balance to get
     * @return current balance stored
     */
    CompletableFuture<Long> getBalance(@NotNull Currency currency, @NotNull UUID uuid);

    /**
     * Set the player's stored balance to new value
     * <p>
     * WARNING: For most uses you should transact a player's balance to prevent possible data loss
     *
     * @param currency  the currency to set the balance for
     * @param uuid      the id of the balance to update
     * @param newAmount the new balance for the player
     * @return a future that's completed once the balance update is completed
     */
    CompletableFuture<Void> setBalance(@NotNull Currency currency, @NotNull UUID uuid, long newAmount);

    /**
     * Increment or decrement a player's balance by a specified amount
     * <p>
     * This method is preferred for transactions over updateBalance, as it should only
     * apply increments or decrements in the backing storage as opposed to setting a new value
     *
     * @param currency     the currency to transact with
     * @param uuid         id of user whose balance to update
     * @param amountChange increment or decrement change for balance
     * @return a future that's completed once the transaction is completed
     */
    CompletableFuture<Void> transact(@NotNull Currency currency, @NotNull UUID uuid, long amountChange);

    /**
     * Get all the currencies this {@link CurrencyStorage} is able to retrieve for the specified {@link UUID user}.
     *
     * @param uuid the {@link UUID} of the user
     *
     * @return a future that's completed once all currency pairs have been loaded.
     */
    CompletableFuture<Collection<CurrencyPair>> getCurrencies (@NotNull UUID uuid);

    /**
     * For use by networked money stores
     */
    void close();
}
