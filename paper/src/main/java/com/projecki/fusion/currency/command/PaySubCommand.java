package com.projecki.fusion.currency.command;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.currency.Currency;
import com.projecki.fusion.util.NetworkChat;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PaySubCommand extends SubCommand {
    @Override
    void onCommand(CommandExecutionInfo commandInfo) {
        UUID payer;

        if (!(commandInfo.getSender() instanceof Player)) {
            commandInfo.getSender().sendMessage(ChatColor.RED + "Only players can use this command");
            return;
        } else {
            payer = ((Player) commandInfo.getSender()).getUniqueId();
        }

        if (isInvalidAmount(commandInfo)) return;

        Currency currency = commandInfo.getCurrencyType();
        CompletableFuture<Long> payerBalance = currency.getBalance(payer);

        payerBalance.thenAccept(b -> {
            if (b >= commandInfo.getAmount()) {
                currency
                        .transact(payer, commandInfo.getAmount() * -1)
                        .thenCompose(f -> currency.transact(commandInfo.getTargetUuid(), commandInfo.getAmount()))
                        .thenRun(() -> sendMessages(commandInfo))
                        .exceptionally(t -> {
                            commandInfo.getSender().sendMessage(ChatColor.RED + "Unable to update balances.");
                            logTopStack(t);
                            return null;
                        });

            } else {
                commandInfo.getSender().sendMessage(ChatColor.RED + "Your balance isn't high enough for that.");
            }
        });

    }

    private void sendMessages(CommandExecutionInfo info) {
        var currency = info.getCurrencyType();

        info.getSender().sendMessage(
                currency.getPrefix()
                        .append(Component.text("You paid ", currency.getPrimary()))
                        .append(Component.text(formatDouble(info.getAmount()), currency.getSecondary()))
                        .append(getCorrectName(currency, info.getAmount())
                                .append(Component.text("to "))
                                .color(currency.getPrimary()))
                        .append(Component.text(info.getTargetDisplayName(), currency.getSecondary()))
        );

        NetworkChat.sendToPlayer(info.getTargetUuid(),
                currency.getPrefix()
                        .append(Component.text("You were paid ", currency.getPrimary()))
                        .append(Component.text(formatDouble(info.getAmount()), currency.getSecondary()))
                        .append(getCorrectName(currency, info.getAmount())
                                .append(Component.text("by "))
                                .color(currency.getPrimary()))
                        .append(Component.text(info.getSender().getName(), currency.getSecondary()))
        );
    }

    @Override
    String getName() {
        return "pay";
    }

    @Override
    String[] getAliases() {
        return new String[]{"send"};
    }

    @Override
    String getDescription() {
        return "Pay another player";
    }

    @Override
    String getUsage() {
        return "pay <player> <amount>";
    }

    @Override
    String getPermission() {
        return "pay";
    }
}
