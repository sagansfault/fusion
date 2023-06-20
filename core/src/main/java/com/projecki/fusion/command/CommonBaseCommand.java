package com.projecki.fusion.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.Subcommand;
import io.netty.util.internal.StringUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Base command that has methods that assist in making nice looking
 * command messages. Also provides a base 'help' command that is default
 * and print out all subcommands of this {@link BaseCommand}
 *
 * Command messages are typically a lighter {@code primaryColor}
 * and a brighter, bolder {@code secondaryColor}. The {@code prefix} is meant
 * to be used as a text component base for sending command messages and
 * notifications, as it is pre colored.
 */
public class CommonBaseCommand extends BaseCommand {

    /** Primary used color, typically a lighter color */
    protected final TextColor primaryColor;

    /** Secondary color, typically a bolder, brigther color than {@code primaryColor} */
    protected final TextColor secondaryColor;

    /** A colored prefix that can be used as a base to append the rest of a command message to
     * this prefix is already pre-colored and has a space padding the right. */
    protected final TextComponent prefix;

    /**
     * A new {@link CommonBaseCommand} with the specified colors and a default prefix.
     *
     * @param primaryColor primary used color, typically a lighter color
     * @param secondaryColor secondary color, typically a bolder, brigther color than {@code primaryColor}
     */
    public CommonBaseCommand(TextColor primaryColor, TextColor secondaryColor) {
        this(primaryColor, secondaryColor, "■");
    }

    /**
     * A new {@link CommonBaseCommand} with the specified colors and prefix.
     * There is no need to color the prefix or pad it on the right, as that is already
     * done.
     *
     * @param primaryColor primary used color, typically a lighter color
     * @param secondaryColor secondary color, typically a bolder, brigther color than {@code primaryColor}
     * @param prefix start of all command messages and notifications
     */
    public CommonBaseCommand(TextColor primaryColor, TextColor secondaryColor, String prefix) {
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.prefix = Component.text(prefix + " ", secondaryColor);
    }

    /**
     * Prints out a help message that displays all of this {@link CommonBaseCommand}'s
     * sub commands. Give another method the {@code @Default} annotation to make
     * the default command another subcommand.
     */
    @Subcommand("help")
    @CatchUnknown
    public void onHelp(CommandIssuer sender) {
        var message = Component.text()
                .append(getHeaderFooter())
                .append(Component.newline())
                .append(getTitle(capitalize(getName()) + " Commands"));

        getCurrentCommandManager().getRootCommand(getName()).getSubCommands().forEach((name, command) -> {
            // aikar registers base command with name __default
            if (name.equals("__default"))
                name = command.getCommand();
            if (!StringUtil.isNullOrEmpty(command.getHelpText().trim())) { // only display help for commands with descriptions
                message.append(Component.newline())
                        .append(Component.text(" - ", secondaryColor))
                        .append(Component.text(command.getHelpText(), primaryColor))
                        .append(Component.text(' ' + name + ' ' + command.getSyntaxText(),
                                NamedTextColor.GRAY))
                        .decoration(TextDecoration.BOLD, false);
            }

        });

        message.append(Component.newline())
                .append(getHeaderFooter());

        sender.sendMessage(message.build());
    }

    /**
     * Create a {@link TextComponent} that is offset to be algined with
     * the {@link TextComponent} made from {@code getTitle()} and {@code getHeaderFooter()}
     *
     * @param message base message to offset
     * @return a {@link TextComponent} offset by spaces
     */
    protected TextComponent offsetMessage(TextComponent message) {
        var amount = (int) Math.max(0, 22 - Math.round(message.content().length() / 2D));
        return TextComponent.ofChildren(Component.text(" ".repeat(amount)), message);
    }

    private String capitalize(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    /**
     * Get a {@link Component} that represents a title used in command
     * messages that are blocks of text. The title is almost centered
     * aligned, bold, and colored by the {@code secondaryColor}
     *
     * @param title text to turn into a title
     * @return title {@link Component}
     */
    protected Component getTitle(String title) {
        return offsetMessage(Component.text(title))
                .color(secondaryColor)
                .decorate(TextDecoration.BOLD);
    }

    /**
     * Get a {@link TextComponent} that is a line 20 characters across
     * that is meant to be used to seperate block messages out in chat.
     * A new line isn't included in this header/footer.
     * The line is colored by the {@code secondaryColor}.
     *
     * @return a line in chat used for a header or footer
     */
    protected TextComponent getHeaderFooter() {
        return Component.text()
                .append(Component.text(" ".repeat(19)))
                .append(Component.text(" ".repeat(20))
                        .color(secondaryColor)
                        .decorate(TextDecoration.STRIKETHROUGH))
                .build();
    }

}
