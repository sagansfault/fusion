package com.projecki.fusion.chat.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.projecki.fusion.chat.chattype.preprocessor.impl.PlayerToPlayerPreChatType;
import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.chat.pipeline.ChatPipeline;
import com.projecki.fusion.chat.pipeline.postprocessor.impl.WhisperModule;
import com.projecki.fusion.component.ComponentBuilder;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandAlias("reply|r")
public class ReplyCommand extends FusionChatBaseCommand {

    public ReplyCommand(ChatPipeline chatPipeline) {
        super(chatPipeline);
    }

    @Default
    @Syntax("<message>")
    @Description("Reply to the most recent person who whispered to you")
    public void onReply(Player player, String messageText) {
        UUID playerUUID = player.getUniqueId();
        WhisperModule.getRecentWhisperer(playerUUID).thenAccept(pairOpt -> pairOpt.ifPresentOrElse(pair -> {
            UUID recentWhispererUUID = pair.fst;
            String recentWhispererName = pair.snd;

            chatPipeline.intake(
                    PlayerToPlayerPreChatType.class,
                    new PlayerToPlayerPreChatType(Message.fromInitialMessage(messageText), player, recentWhispererUUID, recentWhispererName)
            );
        }, () -> player.sendMessage(ComponentBuilder.builder("No one to reply to :(", FusionChatBaseCommand.PRIMARY).toComponent())));
    }
}
