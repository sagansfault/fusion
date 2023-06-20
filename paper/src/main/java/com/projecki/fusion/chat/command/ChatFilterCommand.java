package com.projecki.fusion.chat.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.projecki.fusion.chat.pipeline.ChatPipeline;
import com.projecki.fusion.chat.pipeline.preprocessor.impl.ChatFilterModule;
import com.projecki.fusion.component.ComponentBuilder;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

@CommandAlias("chatfilter")
@CommandPermission("fusion.chat.admin")
public class ChatFilterCommand extends FusionChatBaseCommand {

    public ChatFilterCommand(ChatPipeline chatPipeline) {
        super(chatPipeline);
    }

    @Subcommand("blockliteral")
    @Description("Adds a rule to block a text literal")
    @Syntax("<text>")
    public void blockLiteral(Player player, String literal) {
        Pattern pattern = Pattern.compile(literal, Pattern.LITERAL);
        ChatFilterModule.block(literal, pattern);
        player.sendMessage(ComponentBuilder.builder("Added blocking rule", FusionChatBaseCommand.PRIMARY).toComponent());
    }

    @Subcommand("blockregex")
    @Description("Adds a rule to block by matching a regex pattern")
    @Syntax("<id> <pattern>")
    public void blockLiteral(Player player, @Single String id, String regex) {
        Pattern pattern = Pattern.compile(regex);
        ChatFilterModule.block(id, pattern);
        player.sendMessage(ComponentBuilder.builder("Added blocking rule", FusionChatBaseCommand.PRIMARY).toComponent());
    }

    @Subcommand("remove")
    @Description("Removes a blocking rule based on its literal id or regex id")
    @Syntax("<id>")
    public void removeBlocked(Player player, @Single String id) {
        ChatFilterModule.remove(id).thenAccept(removed -> {
            if (removed) {
                player.sendMessage(ComponentBuilder.builder("Removed blocking rule: ", FusionChatBaseCommand.PRIMARY)
                        .content(id, FusionChatBaseCommand.SECONDARY).toComponent());
            } else {
                player.sendMessage(ComponentBuilder.builder("Could not find blocking rule: ", FusionChatBaseCommand.PRIMARY)
                        .content(id, FusionChatBaseCommand.SECONDARY).toComponent());
            }
        });
    }

    @Subcommand("list")
    @Description("See all the blocking rules")
    public void list(Player player) {
        ComponentBuilder builder = ComponentBuilder.builder("Blocking Rules:", PRIMARY);
        ChatFilterModule.BLOCKED.forEach((id, pattern) -> {
            builder.newLine().content("(" + id + ") ", PRIMARY).content(pattern.pattern(), SECONDARY);
        });
        player.sendMessage(builder.toComponent());
    }
}
