package com.projecki.fusion.currency;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.currency.pubsub.CurrencyUpdateMessage;
import com.projecki.fusion.currency.storage.CurrencyStorage;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PaperCurrency extends BasicCurrency {

    private final TextComponent prefix;
    private final TextColor primary, secondary;

    private final String command;

    public PaperCurrency(@NotNull String id, @NotNull String singular, @NotNull String plural, @NotNull CurrencyStorage currencyStorage, TextComponent prefix, TextColor primary, TextColor secondary, String command) {
        super(id, singular, plural, currencyStorage);
        this.prefix = prefix;
        this.primary = primary;
        this.secondary = secondary;
        this.command = command;
    }

    /**
     * Get prefix for messages.
     * Already colored by {@code secondary}. Padded one space on right
     */
    public TextComponent getPrefix() {
        return prefix;
    }

    public TextColor getPrimary() {
        return primary;
    }

    public TextColor getSecondary() {
        return secondary;
    }

    public Optional<String> getCommand() {
        return Optional.ofNullable(command);
    }

    @Override
    public CompletableFuture<Void> setBalance(@NotNull UUID uuid, long newAmount) {
        return super.setBalance(uuid, newAmount)
                .thenApply(_v -> {
                    FusionPaper.getMessageClient().send(CurrencyRegister.REDIS_CHANNEL, new CurrencyUpdateMessage(uuid, id));
                    return _v;
                });
    }

    @Override
    public CompletableFuture<Void> transact(@NotNull UUID uuid, long amountChange) {
        return super.transact(uuid, amountChange)
                .thenApply(_v -> {
                    FusionPaper.getMessageClient().send(CurrencyRegister.REDIS_CHANNEL, new CurrencyUpdateMessage(uuid, id));
                    return _v;
                });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaperCurrency that = (PaperCurrency) o;
        return prefix.equals(that.prefix) && primary.equals(that.primary) && secondary.equals(that.secondary) && command.equals(that.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, primary, secondary, command);
    }
}
