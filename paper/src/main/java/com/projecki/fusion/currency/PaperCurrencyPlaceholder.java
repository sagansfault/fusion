package com.projecki.fusion.currency;

import com.projecki.fusion.FusionPaper;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PaperCurrencyPlaceholder extends PlaceholderExpansion {

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {

        Currency currency = CurrencyRegister.getCurrency(params).orElse(null);
        if (currency == null) {
            return "INVALID_CURRENCY";
        }

        long amount = FusionPaper.getPlayerCurrencyCache().getCachedBalance(currency, player.getUniqueId()).orElse(-1L);
        return currency.format(amount);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "currency";
    }

    @Override
    public @NotNull String getAuthor() {
        return "";
    }

    @Override
    public @NotNull String getVersion() {
        return FusionPaper.getPlugin(FusionPaper.class).getDescription().getVersion();
    }
}
