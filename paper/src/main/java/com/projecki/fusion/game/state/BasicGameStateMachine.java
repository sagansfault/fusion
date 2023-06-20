package com.projecki.fusion.game.state;

import org.bukkit.plugin.java.JavaPlugin;

public final class BasicGameStateMachine extends GenericGameStateMachine<GameState> {

    public BasicGameStateMachine(JavaPlugin plugin) {
        super(plugin);
    }
}
