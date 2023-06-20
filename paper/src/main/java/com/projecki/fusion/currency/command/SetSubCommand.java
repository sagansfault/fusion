package com.projecki.fusion.currency.command;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.currency.Currency;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;

public class SetSubCommand extends SubCommand {

    @Override
    public void onCommand(CommandExecutionInfo commandInfo) {

        if (isInvalidAmount(commandInfo)) return;

        Currency currency = commandInfo.getCurrencyType();
        currency
                .setBalance(commandInfo.getTargetUuid(), commandInfo.getAmount())
                .thenRun(() -> sendMessage(commandInfo))
                .exceptionally(t -> {
                    commandInfo.getSender().sendMessage(ChatColor.RED + "Unable to set balance of " +
                                    commandInfo.getTargetDisplayName());
                    logTopStack(t);
                    return null;
                });
    }

    private void sendMessage(CommandExecutionInfo commandInfo) {
        var currency = commandInfo.getCurrencyType();

        commandInfo.getSender().sendMessage(
                currency.getPrefix()
                        .append(Component.text("You set ", currency.getPrimary()))
                        .append(Component.text(commandInfo.getTargetDisplayName(), currency.getSecondary()))
                        .append(Component.text("'s balance to ", currency.getPrimary()))
                        .append(Component.text(formatDouble(commandInfo.getAmount()), currency.getSecondary()))
                        .append(getCorrectName(currency, commandInfo.getAmount())
                                .color(currency.getPrimary())));
    }

    @Override
    String getName() {
        return "set";
    }

    @Override
    String[] getAliases() {
        return new String[0];
    }

    @Override
    String getDescription() {
        return "Set a player's balance";
    }

    @Override
    String getUsage() {
        return "set <player> <amount>";
    }

    @Override
    String getPermission() {
        return "set";
    }
}
