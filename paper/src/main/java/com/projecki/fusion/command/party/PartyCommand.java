package com.projecki.fusion.command.party;

import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.projecki.fusion.command.base.PaperCommonBaseCommand;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

/**
 * @author Andavin
 * @since April 26, 2022
 */
@CommandAlias("party|p")
public class PartyCommand extends PaperCommonBaseCommand {

    private static final TextColor PRIMARY = TextColor.color(0x58CBF6);

    public PartyCommand(PaperCommandManager manager) {
        super(PRIMARY, NamedTextColor.WHITE, manager);
    }

    @Subcommand("invite")
    @Syntax("<target>")
    @Description("Invite a player to your party")
    public void invite(Player player, String target) {
        player.sendMessage(text("This command is not implemented yet", RED));
    }

    @Subcommand("kick")
    @CommandAlias("remove")
    @Syntax("<target>")
    @Description("Kick a player from your party")
    public void kick(Player player, String target) {
        player.sendMessage(text("This command is not implemented yet", RED));
    }

    @Subcommand("leave")
    @Description("Leave your current party")
    public void leave(Player player) {
        player.sendMessage(text("This command is not implemented yet", RED));
    }
}
