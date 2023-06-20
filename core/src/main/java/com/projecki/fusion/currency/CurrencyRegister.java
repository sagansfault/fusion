package com.projecki.fusion.currency;

import com.google.common.collect.ImmutableCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CurrencyRegister {
    public static final String REDIS_CHANNEL = "fusion_currency";

    private static final Map<String, Currency> currencies = new HashMap<>();

    public static Optional<Currency> getCurrency (@NotNull String identifier) {
        return currencies.values()
                .stream()
                .filter(currency -> currency.matchIdentifier(identifier))
                .findFirst();
    }

    public static boolean registerCurrency (@NotNull Currency currency) {
        if (getCurrency(currency.id()).isPresent()) return false;

        currencies.put(currency.id(), currency);
        return true;
    }

    @Unmodifiable
    public static Collection<Currency> getCurrencies () {
        return Collections.unmodifiableCollection(currencies.values());
    }
}
