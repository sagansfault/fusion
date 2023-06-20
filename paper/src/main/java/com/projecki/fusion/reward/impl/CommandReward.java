package com.projecki.fusion.reward.impl;

import com.projecki.fusion.reward.Reward;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Function;

/**
 * Represents a command reward to award a given player. The player is instance independent such that this reward can
 * be given to multiple players without having to make a new Reward instance.
 */
public class CommandReward implements Reward {

    private final Function<Player, String> commandGenerator;

    /**
     * Creates a new command reward. This command reward is a simple generator function that takes a given player
     * and returns a command that will reward them.
     *
     * This reward is player independent, as in, this same reward instance can be given to multiple players. Java
     * garbage collectors are good, but I advise not making duplicate reward instances to just give the same reward
     * to multiple players.
     *
     * @param commandGenerator A function to determine what command to run based on a given player. This function is
     *                         run each time the reward is executed.
     */
    public CommandReward(Function<Player, String> commandGenerator) {
        this.commandGenerator = commandGenerator;
    }

    @Override
    public void reward(Player target) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), this.commandGenerator.apply(target));
    }
}
