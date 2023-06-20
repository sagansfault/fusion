package com.projecki.fusion.currency.command;

import com.projecki.fusion.currency.PaperCurrency;
import org.bukkit.command.CommandSender;

import java.util.UUID;

class CommandExecutionInfo {

    private final CommandSender sender;
    private final String command;
    private final PaperCurrency currencyType;
    private final String targetDisplayName;
    private final UUID targetUuid;
    private final Long amount;
    private final String permission;

    CommandExecutionInfo(CommandSender sender, String command, PaperCurrency currencyType, String targetDisplayName, UUID targetUuid, Long amount, String permission) {
        this.sender = sender;
        this.command = command;
        this.currencyType = currencyType;
        this.targetDisplayName = targetDisplayName;
        this.targetUuid = targetUuid;
        this.amount = amount;
        this.permission = permission;
    }

    CommandSender getSender() {
        return sender;
    }

    String getCommand() {
        return command;
    }

    PaperCurrency getCurrencyType() {
        return currencyType;
    }

    String getTargetDisplayName() {
        return targetDisplayName;
    }

    UUID getTargetUuid() {
        return targetUuid;
    }

    Long getAmount() {
        return amount;
    }

    String getPermission() {
        return permission;
    }
}
