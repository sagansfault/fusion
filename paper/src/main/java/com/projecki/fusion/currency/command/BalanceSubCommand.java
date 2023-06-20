package com.projecki.fusion.currency.command;

import com.projecki.fusion.FusionPaper;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class BalanceSubCommand extends SubCommand {

    @Override
    void onCommand(CommandExecutionInfo commandInfo) {
        boolean self = commandInfo.getSender() instanceof Player &&
                ((Player) commandInfo.getSender()).getUniqueId().equals(commandInfo.getTargetUuid());

        if (!self && !commandInfo.getSender().hasPermission(commandInfo.getPermission() + ".other")) {
            commandInfo.getSender().sendMessage(ChatColor.RED + "You don't have permission to do that");
            return;
        }

        commandInfo.getCurrencyType().getBalance(commandInfo.getTargetUuid())
                .thenAccept(b -> sendMessage(commandInfo, self, b))
                .exceptionally(t -> {
                    commandInfo.getSender().sendMessage(ChatColor.RED + "Unable to get balance for "
                            + commandInfo.getTargetDisplayName());
                    logTopStack(t);
                    return null;
                });
    }

    private void sendMessage(CommandExecutionInfo commandInfo, boolean self, double balance) {
        var currency = commandInfo.getCurrencyType();

        if (self) {
            commandInfo.getSender().sendMessage(
                    currency.getPrefix()
                            .append(Component.text("Your balance is ", currency.getPrimary()))
                            .append(Component.text(formatDouble(balance), currency.getSecondary()))
                            .append(getCorrectName(currency, balance).color(currency.getPrimary()))
            );
        } else {
            commandInfo.getSender().sendMessage(
                    currency.getPrefix()
                            .append(Component.text(commandInfo.getTargetDisplayName(), currency.getSecondary()))
                            .append(Component.text("'s balance is ", currency.getPrimary()))
                            .append(Component.text(formatDouble(balance), currency.getSecondary()))
                            .append(getCorrectName(currency, balance).color(currency.getPrimary()))
            );
        }
    }

    @Override
    String getName() {
        return "balance";
    }

    @Override
    String[] getAliases() {
        return new String[]{"bal"};
    }

    @Override
    String getDescription() {
        return "Get your own or another player's balance";
    }

    @Override
    String getUsage() {
        return "balance [player]";
    }

    @Override
    String getPermission() {
        return "balance";
    }
}
