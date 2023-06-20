package com.projecki.fusion.command;

import net.kyori.adventure.text.TextComponent;

/**
 * Generic command issuer to be implemented by specific
 * command sender implementations
 */
public abstract class CommandIssuer {

    public abstract void sendMessage(TextComponent message);

    public abstract boolean isPlayer();

    public boolean isConsole() {
        return !isPlayer();
    }

}
