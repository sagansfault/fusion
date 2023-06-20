package com.projecki.fusion.currency.cache;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.currency.CurrencyRegister;
import com.projecki.fusion.currency.pubsub.CurrencyUpdateMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerCurrencyCache extends CurrencyCache implements Listener {

    public PlayerCurrencyCache() {

        FusionPaper.getMessageClient().subscribe(CurrencyRegister.REDIS_CHANNEL);
        FusionPaper.getMessageClient().registerMessageListener(CurrencyUpdateMessage.class, (channel, message) -> {

            if(!channel.equals(CurrencyRegister.REDIS_CHANNEL)) return;

            CurrencyRegister.getCurrency(message.currencyId())
                    .ifPresent(currency -> updateBalance(currency, message.uuid()));
        });
    }

    @EventHandler
    public void onPlayerJoinEvent (@NotNull PlayerJoinEvent event) {
        CurrencyRegister.getCurrencies().forEach(currency -> {
            updateBalance(currency, event.getPlayer().getUniqueId());
        });
    }

    @EventHandler
    public void onPlayerQuitEvent (@NotNull PlayerQuitEvent event) {
        clear(event.getPlayer().getUniqueId());
    }
}
