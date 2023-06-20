package com.projecki.fusion.currency.command;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.currency.PaperCurrency;
import com.projecki.fusion.util.RateLimiter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.core.util.Integers;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CreditsCommand extends BukkitCommand {

    private static final Map<String, SubCommand> subcommandExecutors = new HashMap<>();
    private static final Map<Player, RateLimiter> rateLimiters = new WeakHashMap<>();

    private final PaperCurrency currencyType;

    static {
        register(new SetSubCommand());
        register(new AddSubCommand());
        register(new TakeSubCommand());
        register(new BalanceSubCommand());
        register(new PaySubCommand());
    }

    public CreditsCommand(PaperCurrency currencyType) {
        super(currencyType.plural(), "manage " + currencyType.plural(), "placeholder", Collections.emptyList());

        this.currencyType = currencyType;

        StringBuilder usageBuilder = new StringBuilder()
                .append('/')
                .append(getName())
                .append(" <");

        subcommandExecutors.values().stream()
                .distinct()
                .map(SubCommand::getName)
                .forEach(n -> usageBuilder.append(n).append('/'));

        usageBuilder.deleteCharAt(usageBuilder.length() - 1)
                .append("> [player] [amount]");

        usageMessage = usageBuilder.toString();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {

        if (strings.length == 0 || strings[0].equalsIgnoreCase("help")) {
            commandSender.sendMessage(getHelpMessage(getPermittedCommands(commandSender)));
            return false;
        }

        String commandName = strings[0].toLowerCase();
        SubCommand command = subcommandExecutors.get(commandName);

        if (command == null) {
            commandSender.sendMessage(ChatColor.RED + String.format("'%s' is not a valid command.", commandName));
            return false;
        }

        RateLimiter limiter = null;
        String permission = getPermission(command);

        if (commandSender instanceof Player) {

            if (!commandSender.hasPermission(permission) && !commandSender.isOp()) {
                commandSender.sendMessage(ChatColor.RED + "You do not have permission to do this.");
                return true;
            }

            limiter = rateLimiters.computeIfAbsent(((Player) commandSender),
                    p -> new RateLimiter(1000 * 30, 3));

            if (limiter.isLimited() && !commandSender.hasPermission("ignoreratelimit")) {
                commandSender.sendMessage(ChatColor.RED + "You're sending currency commands too fast.");
                return true;
            }
        }

        CompletableFuture<Optional<UUID>> targetUuid;
        String targetName;
        Long amount = null;

        if (strings.length == 1) {

            if (commandSender instanceof Player) {
                targetName = commandSender.getName();
                targetUuid = CompletableFuture.completedFuture(Optional.of(((Player) commandSender).getUniqueId()));
            } else {
                return false;
            }

        } else {
            targetName = strings[1];


            targetUuid = FusionPaper.getNameResolver().resolveUuidMojang(targetName);

            if (strings.length >= 3) {
                try {
                    amount = Long.parseLong(strings[2]);
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(ChatColor.RED + "Invalid amount value.");
                    return false;
                }
            }
        }

        Long finalAmount = amount;
        targetUuid.thenAccept((uuidOpt) -> {
                    if (uuidOpt.isPresent()) {
                        command.onCommand(new CommandExecutionInfo(commandSender, commandName, currencyType, targetName,
                                uuidOpt.get(), finalAmount, permission));
                    } else {
                        throw new IllegalArgumentException("no player exists with that name");
                    }
                })
                .whenComplete((v, exe) -> {
                    if (exe != null)
                        commandSender.sendMessage(ChatColor.RED + targetName + " doesn't exist or has never logged on.");
                });

        if (limiter != null) {
            limiter.use();
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return switch (args.length) {
            case 1 -> getPermittedCommands(sender).stream()
                    .map(SubCommand::getName)
                    .collect(Collectors.toList());
            case 2 -> super.tabComplete(sender, alias, args);
            default -> Collections.emptyList();
        };
    }

    private String getPermission(String permission) {
        return getName() + ".command." + permission;
    }

    private String getPermission(SubCommand subCommand) {
        return getPermission(subCommand.getPermission());
    }

    /**
     * Get the subcommands a {@link Permissible} has permission to use
     */
    private Set<SubCommand> getPermittedCommands(Permissible permissible) {
        return subcommandExecutors.values().stream()
                .distinct()
                .filter(c -> permissible.hasPermission(getPermission(c)))
                .collect(Collectors.toSet());
    }

    private TextComponent getHelpMessage(Set<SubCommand> commands) {
        var message = Component.text()
                .append(getHeaderFooter())
                .append(Component.newline())
                .append(Component.text(StringUtils.repeat(" ", 16)))
                .append(Component.text()
                        .content(StringUtils.capitalize(currencyType.plural()) + " Commands")
                        .color(currencyType.getSecondary())
                        .decorate(TextDecoration.BOLD));

        for (SubCommand subCommand : commands) {
            message.append(Component.newline())
                    .append(Component.text(" - ", currencyType.getSecondary()))
                    .append(Component.text(subCommand.getDescription(), currencyType.getPrimary()))
                    .append(Component.text(" /")
                            .append(Component.text(currencyType.plural()))
                            .append(Component.text(' '))
                            .append(Component.text(subCommand.getUsage()))
                            .color(NamedTextColor.GRAY));

        }

        message.append(Component.newline())
                .append(getHeaderFooter());

        return message.build();
    }

    private Component getHeaderFooter() {
        return Component.text()
                .append(Component.text(StringUtils.repeat(" ", 19)))
                .append(Component.text(StringUtils.repeat(" ", 20))
                        .color(currencyType.getSecondary())
                        .decorate(TextDecoration.STRIKETHROUGH))
                .build();
    }

    private static void register(SubCommand subCommand) {
        subcommandExecutors.put(subCommand.getName().toLowerCase(), subCommand);

        Arrays.stream(subCommand.getAliases())
                .map(String::toLowerCase)
                .forEach(a -> subcommandExecutors.put(a, subCommand));
    }

}
