package com.projecki.fusion.currency.config;

import com.projecki.fusion.currency.PaperCurrency;
import com.projecki.fusion.currency.storage.CurrencyStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

public record CurrencyConfig (Map<String, Currency> currency) {

    @Unmodifiable
    public List<PaperCurrency> getCurrencies (@NotNull CurrencyStorage storage) {
        return currency != null ? currency.entrySet().stream().map(entry -> entry.getValue().getCurrency(entry.getKey(), storage))
                .toList() : List.of();
    }

    public record Currency (
            @NotNull String singular,
            @NotNull String plural,
            @NotNull String prefix,
            @NotNull Color color
    ) {

        public PaperCurrency getCurrency (@NotNull String id, @NotNull CurrencyStorage storage) {
            return new PaperCurrency(id, singular, plural, storage, Component.text(prefix), color.getPrimary(), color.getSecondary(), plural);
        }

        public record Color (
                @NotNull String primary,
                @NotNull String secondary
        ) {
            public TextColor getPrimary() {
                return TextColor.fromHexString(primary);
            }

            public TextColor getSecondary() {
                return TextColor.fromHexString(primary);
            }
        }
    }
}
