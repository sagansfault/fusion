package com.projecki.fusion.chat.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.chat.chattype.preprocessor.impl.PlayerToPlayerPreChatType;
import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.chat.pipeline.ChatPipeline;
import com.projecki.fusion.chat.pipeline.postprocessor.impl.WhisperModule;
import com.projecki.fusion.component.ComponentBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

@CommandAlias("msg|m|whisper|w|dm")
public class WhisperCommand extends FusionChatBaseCommand {

    public WhisperCommand(ChatPipeline chatPipeline) {
        super(chatPipeline);
    }

    @Default
    @Syntax("<player> <message>")
    @Description("Whisper to someone")
    public void onWhisper(Player player, String target, String messageText) {
        FusionPaper.getNameResolver().resolveUuidMojang(target).thenAccept(uuidOpt -> uuidOpt.ifPresent(targetUUID -> {
            chatPipeline.intake(
                    PlayerToPlayerPreChatType.class,
                    new PlayerToPlayerPreChatType(
                            Message.fromInitialMessage(messageText),
                            player,
                            targetUUID,
                            target
                    )
            );
        }));
    }

    @Subcommand("allow")
    @Syntax("<player>")
    @Description("Re-allow someone to send whispers to you")
    public void onAllow(Player player, String target) {
        FusionPaper.getNameResolver().resolveUuidMojang(target).thenAccept(uuidOpt -> uuidOpt.ifPresent(uuid -> {
            UUID playerUUID = player.getUniqueId();
            boolean contained = WhisperModule.BLOCKED_DATA.computeIfAbsent(playerUUID, u -> new HashSet<>()).remove(uuid);
            Component message;
            if (contained) {
                message = ComponentBuilder.builder("Re-allowed ", FusionChatBaseCommand.PRIMARY)
                        .content(target, FusionChatBaseCommand.SECONDARY)
                        .content(" to whisper to you", FusionChatBaseCommand.PRIMARY).toComponent();
            } else {
                message = ComponentBuilder.builder("That person wasn't already blocked", FusionChatBaseCommand.PRIMARY).toComponent();
            }
            player.sendMessage(message);
        }));
    }

    @Subcommand("block|deny")
    @Syntax("<player>")
    @Description("Block/deny someone from sending whispers to you")
    public void onDeny(Player player, String target) {
        FusionPaper.getNameResolver().resolveUuidMojang(target).thenAccept(uuidOpt -> uuidOpt.ifPresent(uuid -> {
            UUID playerUUID = player.getUniqueId();
            boolean didNotContain = WhisperModule.BLOCKED_DATA.computeIfAbsent(playerUUID, u -> new HashSet<>()).add(uuid);
            Component message;
            if (didNotContain) {
                message = ComponentBuilder.builder("Blocked ", FusionChatBaseCommand.PRIMARY)
                        .content(target, FusionChatBaseCommand.SECONDARY)
                        .content(" from whispering to you", FusionChatBaseCommand.PRIMARY).toComponent();
            } else {
                message = ComponentBuilder.builder("That person is already blocked", FusionChatBaseCommand.PRIMARY).toComponent();
            }
            player.sendMessage(message);
        }));
    }
}
