package com.projecki.fusion.chat.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.chat.channel.ChannelManager;
import com.projecki.fusion.chat.channel.ChatChannel;
import com.projecki.fusion.chat.chattype.preprocessor.impl.PlayerToChannelPreChatType;
import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.chat.pipeline.ChatPipeline;
import com.projecki.fusion.component.ComponentBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

@CommandAlias("%channel-command-alias")
public class ChannelCommand extends FusionChatBaseCommand {

    public ChannelCommand(ChatPipeline chatPipeline) {
        super(chatPipeline);
    }

    @Default
    @Syntax("[message] - empty to toggle in/out, filled to send into")
    @Description("Sends a message to the channel you're in or toggles out/into it")
    public void onChannelCommand(Player player, @co.aikar.commands.annotation.Optional String message) {
        UUID uuid = player.getUniqueId();

        ChatPipeline chatPipeline = FusionPaper.getChatPipeline();
        ChannelManager channelManager = chatPipeline.getChannelManager();

        String targetChannelId = super.getExecCommandLabel();
        Optional<ChatChannel> channelOptional = channelManager.getChannel(targetChannelId);

        if (channelOptional.isEmpty()) {
            player.sendMessage(ComponentBuilder.builder("That channel does not exist or you don't have permission for it",
                    FusionChatBaseCommand.PRIMARY).toComponent());
            return;
        }

        ChatChannel channel = channelOptional.get();

        if (channel.getHidden().contains(uuid)) {
            player.sendMessage(ComponentBuilder.builder("You have that channel hidden, un-hide it to use it.",
                    FusionChatBaseCommand.PRIMARY).toComponent());
            return;
        }

        if (!player.hasPermission(channel.getPermission()) && !channel.equals(channelManager.getDefaultChannel())) {
            player.sendMessage(ComponentBuilder.builder("You don't have permission for that channel",
                    FusionChatBaseCommand.PRIMARY).toComponent());
            return;
        }

        if (message == null) {
            Optional<ChatChannel> existingOpt = channelManager.getChannel(uuid);

            if (existingOpt.isPresent()) {
                ChatChannel existing = existingOpt.get();
                if (existing.equals(channel)) {
                    player.sendMessage(ComponentBuilder.builder("You're already in that channel",
                            FusionChatBaseCommand.PRIMARY).toComponent());
                    return;
                }

                player.sendMessage(ComponentBuilder.builder("Left: ", FusionChatBaseCommand.PRIMARY)
                        .content(existing.getId(), FusionChatBaseCommand.SECONDARY).toComponent());

                channelManager.putInChannel(uuid, channel);
                player.sendMessage(ComponentBuilder.builder("Entered: ", FusionChatBaseCommand.PRIMARY)
                        .content(channel.getId(), FusionChatBaseCommand.SECONDARY).toComponent());
            }
        } else {
            Message messageObj = Message.fromInitialMessage(Component.text(message));
            FusionPaper.getServerInfo().ifPresent(serverInfo -> {
                chatPipeline.intake(
                        PlayerToChannelPreChatType.class,
                        new PlayerToChannelPreChatType(messageObj, channel, serverInfo.getServerName(), player)
                );
            });
        }
    }

    @Subcommand("togglehidden")
    @Description("Toggles whether you can see this channel or not")
    public void toggleHidden(Player player) {
        UUID uuid = player.getUniqueId();
        ChannelManager channelManager = FusionPaper.getChatPipeline().getChannelManager();

        String targetChannelId = super.getExecCommandLabel();
        Optional<ChatChannel> channelOptional = channelManager.getChannel(targetChannelId);

        if (channelOptional.isEmpty()) {
            player.sendMessage(ComponentBuilder.builder("That channel does not exist or you don't have permission for it",
                    FusionChatBaseCommand.PRIMARY).toComponent());
            return;
        }

        ChatChannel channel = channelOptional.get();
        boolean hidden = channel.toggleHidden(uuid);
        if (hidden) {
            player.sendMessage(ComponentBuilder.builder("Channel hidden",
                    FusionChatBaseCommand.PRIMARY).toComponent());
        } else {
            player.sendMessage(ComponentBuilder.builder("Channel unhidden",
                    FusionChatBaseCommand.PRIMARY).toComponent());
        }
    }
}
