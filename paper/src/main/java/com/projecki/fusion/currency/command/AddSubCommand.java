package com.projecki.fusion.currency.command;

import com.projecki.fusion.util.NetworkChat;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;

import java.util.concurrent.CompletableFuture;

public class AddSubCommand extends SubCommand {

    @Override
    public void onCommand(CommandExecutionInfo commandInfo) {

        if (isInvalidAmount(commandInfo)) return;

        var currency = commandInfo.getCurrencyType();

        CompletableFuture<Void> future = currency
                .transact(commandInfo.getTargetUuid(), commandInfo.getAmount());

        future.thenRun(() -> {

            commandInfo.getSender().sendMessage(
                    currency.getPrefix()
                    .append(Component.text("You credited ", currency.getPrimary()))
                    .append(Component.text(formatDouble(commandInfo.getAmount()), currency.getSecondary()))
                    .append(getCorrectName(currency, commandInfo.getAmount()).color(currency.getPrimary()))
                    .append(Component.text("to ", currency.getPrimary()))
                    .append(Component.text(commandInfo.getTargetDisplayName(), currency.getSecondary())));

            NetworkChat.sendToPlayer(commandInfo.getTargetUuid(),
                    currency.getPrefix()
                            .append(Component.text("You were credited ", currency.getPrimary()))
                            .append(Component.text(formatDouble(commandInfo.getAmount()), currency.getSecondary()))
                            .append(getCorrectName(currency,commandInfo.getAmount()).color(currency.getPrimary())));
        });

        future.exceptionally(t -> {
            commandInfo.getSender()
                    .sendMessage(ChatColor.RED + "Unable to credit " + commandInfo.getTargetDisplayName());
            logTopStack(t);
            return null;
        });

    }

    @Override
    String getName() {
        return "add";
    }

    @Override
    String[] getAliases() {
        return new String[]{"credit"};
    }

    @Override
    String getDescription() {
        return "Credit a player's balance";
    }

    @Override
    String getUsage() {
        return "add <player> <amount>";
    }

    @Override
    String getPermission() {
        return "add";
    }
}
