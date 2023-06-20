package com.projecki.fusion.command.base;

import com.projecki.fusion.command.CommandIssuer;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.TextComponent;

/**
 * Velocity implementation of {@link CommandIssuer}
 * wraps {@link CommandSource}
 */
public class VelocityCommandIssuer extends CommandIssuer {
    
    private final CommandSource commandSource;

    public VelocityCommandIssuer(CommandSource commandSource) {
        this.commandSource = commandSource;
    }

    @Override
    public void sendMessage(TextComponent message) {
        commandSource.sendMessage(message);
    }

    @Override
    public boolean isPlayer() {
        return commandSource instanceof Player;
    }
}
