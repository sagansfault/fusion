package com.projecki.fusion.currency;

import com.projecki.fusion.FusionCore;
import com.projecki.fusion.currency.storage.CurrencyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BasicCurrency implements Currency {

    protected final String id, singular, plural;
    protected final CurrencyStorage currencyStorage;

    public BasicCurrency(@NotNull String id, @NotNull String singular, @NotNull String plural, @NotNull CurrencyStorage currencyStorage) {
        this.id = id;
        this.singular = singular;
        this.plural = plural;
        this.currencyStorage = currencyStorage;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String singular() {
        return singular;
    }

    @Override
    public String plural() {
        return plural;
    }

    @Override
    public CompletableFuture<Long> getBalance(@NotNull UUID uuid) {
        return currencyStorage.getBalance(this, uuid);
    }

    @Override
    public CompletableFuture<Void> setBalance(@NotNull UUID uuid, long newAmount) {
        return currencyStorage.setBalance(this, uuid, newAmount);
    }

    @Override
    public CompletableFuture<Void> transact(@NotNull UUID uuid, long amountChange) {
        return currencyStorage.transact(this, uuid, amountChange);
    }
}
