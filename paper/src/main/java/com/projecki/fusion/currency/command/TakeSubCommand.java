package com.projecki.fusion.currency.command;

import com.projecki.fusion.FusionPaper;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;

public class TakeSubCommand extends SubCommand {

    @Override
    public void onCommand(CommandExecutionInfo commandInfo) {
        if (isInvalidAmount(commandInfo)) return;

        var currency = commandInfo.getCurrencyType();

        currency
                .transact(commandInfo.getTargetUuid(), commandInfo.getAmount() * -1)
                .thenRun(() -> commandInfo.getSender().sendMessage(
                        currency.getPrefix()
                                .append(Component.text("You took ", currency.getPrimary()))
                                .append(Component.text(formatDouble(commandInfo.getAmount()), currency.getSecondary()))
                                .append(getCorrectName(currency, commandInfo.getAmount()).color(currency.getPrimary()))
                                .append(Component.text("from ", currency.getPrimary()))
                                .append(Component.text(commandInfo.getTargetDisplayName(), currency.getSecondary()))))
                .exceptionally(t -> {
                    commandInfo.getSender()
                            .sendMessage(ChatColor.RED + "Unable to take from " + commandInfo.getTargetDisplayName());
                    logTopStack(t);
                    return null;
                });
    }

    @Override
    String getName() {
        return "take";
    }

    @Override
    String[] getAliases() {
        return new String[]{"debit"};
    }

    @Override
    String getDescription() {
        return "Debit currency from a player's balance";
    }

    @Override
    String getUsage() {
        return "take <player> <amount>";
    }

    @Override
    String getPermission() {
        return "take";
    }
}
