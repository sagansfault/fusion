package com.projecki.fusion.command.base;

import com.projecki.fusion.command.CommandIssuer;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Papper implementation of {@link CommandIssuer}
 * wraps {@link CommandSender}
 */
public class PaperCommandIssuer extends CommandIssuer {

    private final CommandSender sender;

    public PaperCommandIssuer(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendMessage(TextComponent message) {
        sender.sendMessage(message);
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }
}
