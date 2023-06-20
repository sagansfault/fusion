package com.projecki.fusion.currency;

import com.projecki.fusion.currency.storage.CurrencyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Currency {

    String id();

    String singular();

    String plural();

    CompletableFuture<Long> getBalance(@NotNull UUID uuid);

    CompletableFuture<Void> setBalance(@NotNull UUID uuid, long newAmount);

    CompletableFuture<Void> transact(@NotNull UUID uuid, long amountChange);

    default boolean matchIdentifier(@NotNull String identifier) {
        return identifier.equalsIgnoreCase(id()) ||
                identifier.equalsIgnoreCase(singular()) ||
                identifier.equalsIgnoreCase(plural());
    }

    /**
     * Format the specified amount using this currency.
     * <br><br>
     * E.g. a currency type with singular: {@code dollar} and plural {@code dollars},
     * the following calls return:
     * <ul>
     *     <li>{@code currency.format(10)} => "10 dollars"</li>
     *     <li>{@code currency.format(1)} => "1 dollar<"/li>
     *     <li>{@code currency.format(0)} => "0 dollars"</li>
     * </ul>
     *
     * @param amount the amount to format
     * @return the amount formatted as a string
     */
    default String format(long amount) {
        return String.format("%s %s", amount, amount == 1 ? singular() : plural());
    }
}
