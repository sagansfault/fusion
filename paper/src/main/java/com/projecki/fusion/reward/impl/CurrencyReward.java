package com.projecki.fusion.reward.impl;

import com.projecki.fusion.currency.Currency;
import com.projecki.fusion.currency.storage.CurrencyStorage;
import com.projecki.fusion.reward.Reward;
import org.bukkit.entity.Player;

/**
 * Represents a currency reward to award a given player. The player is instance independent such that this reward can
 * be given to multiple players without having to make a new Reward instance.
 */
public class CurrencyReward implements Reward {
    private final Currency currency;
    private final long reward;

    /**
     * Creates a new currency reward. This reward is player independent, as in, this same reward instance can be given
     * to multiple players. Java garbage collectors are good, but I advise not making duplicate reward instances to just
     * give the same reward to multiple players.
     *
     * @param currency The currency type to reward
     * @param reward The amount of the currency type to reward
     */
    public CurrencyReward(Currency currency, long reward) {
        this.currency = currency;
        this.reward = reward;
    }

    @Override
    public void reward(Player target) {
        currency.transact(target.getUniqueId(), reward);
    }

    public Currency getCurrencyType() {
        return currency;
    }

    public long getReward() {
        return reward;
    }
}
