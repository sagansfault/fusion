package com.projecki.fusion.currency.command;

import com.projecki.fusion.currency.PaperCurrency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.text.DecimalFormat;
import java.util.concurrent.CompletionException;

abstract class SubCommand {

    private static final DecimalFormat format = new DecimalFormat("#.##");

    abstract void onCommand(CommandExecutionInfo commandInfo);

    abstract String getName();

    abstract String[] getAliases();

    abstract String getDescription();

    abstract String getUsage();

    abstract String getPermission();

    /**
     * Whether this command can only be executed by players.
     */
    boolean isPlayerOnly() { //TODO implement this
        return false;
    }

    /**
     * Whether this command needs {@code CommandExecutionInfo.getAmount()} validated
     */
    boolean needAmountValidated() {
        return false; //TODO implement this
    }

    /**
     * Ensure the given command info's amount is not zero, positive,
     * and not null, then send the commandSender a message if it isn't
     *
     * @param commandInfo command info to ensure
     * @return whether the commandInfo amount is valid
     */
    protected boolean isInvalidAmount(CommandExecutionInfo commandInfo) {
        if (commandInfo.getAmount() == null || commandInfo.getAmount() <= 0) {
            commandInfo.getSender().sendMessage(ChatColor.RED + "Amount cannot be zero or negative");
            return true;
        }
        return false;
    }

    /**
     * Get the plural or singular version of the currency name based off ot the amount
     *
     * @param currencyType type of currency
     * @param amount amount to check against
     * @return {@link TextComponent} containing correct name, padded by spaces on both sides
     */
    protected TextComponent getCorrectName(PaperCurrency currencyType, double amount) {
        return Component.text(' ' + (amount == 1 ? currencyType.singular() : currencyType.plural()) + ' ');
    }

    /**
     * Format a double to standard readable string
     *
     * @param value double to format
     * @return readable string
     */
    protected String formatDouble(double value) {
        return format.format(value);
    }

    /**
     * Log just the top of the stack for this throwable for exceptions that are
     * expected or easy to diagnose.
     *
     * @param throwable throwable to log
     */
    protected void logTopStack(Throwable throwable) {
        if (throwable instanceof CompletionException) {
            throwable = throwable.getCause();
        }

        Bukkit.getLogger().warning("Error while executing currency command (" +
                throwable.getStackTrace()[0].getClassName() + ')');
    }
}
