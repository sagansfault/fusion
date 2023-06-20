package com.projecki.fusion.chat.pipeline.postprocessor.impl;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.chat.ChatConfig;
import com.projecki.fusion.chat.channel.ChatChannel;
import com.projecki.fusion.chat.chattype.postprocessor.impl.PlayerToChannelPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.ServerToChannelPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.PostProcessorChatType;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.postprocessor.PostProcessorManager;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;
import com.projecki.fusion.chat.util.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.stream.Collectors;

public class ChannelModule extends ChatPipelineModule<PostProcessorChatType, PostProcessorManager> {

    public ChannelModule(PostProcessorManager postProcessorManager) {
        super(postProcessorManager);
        FusionPaper.getServerInfo().ifPresent(info -> {
            String serverName = info.getServerName();

            super.addComponent(new AbstractProcessorManager.Component<>(PlayerToChannelPostChatType.class, message -> {
                ChatChannel targetChannel = message.getTargetChannel();
                if (!targetChannel.isAcceptableTargetGroup(message.getOriginServer(), serverName)) {
                    return;
                }
                message.getAudience().addAll(Bukkit.getOnlinePlayers().stream()
                        .filter(player -> targetChannel.canSee(player) ||
                                postProcessorManager.getPipeline().getChannelManager().getDefaultChannel().equals(targetChannel))
                        .map(Entity::getUniqueId).collect(Collectors.toSet()));

                message.getMessage().setReplacement(Placeholder.PLAYER_SENDER.getPlaceholder(), message.getSenderName());
                message.getMessage().setReplacement(Placeholder.SENDER_ORIGIN_SERVER.getPlaceholder(), message.getOriginServer());
            }));

            super.addComponent(new AbstractProcessorManager.Component<>(ServerToChannelPostChatType.class, message -> {
                ChatChannel targetChannel = message.getTargetChannel();
                message.getAudience().addAll(Bukkit.getOnlinePlayers().stream()
                        .filter(player -> targetChannel.canSee(player) ||
                                postProcessorManager.getPipeline().getChannelManager().getDefaultChannel().equals(targetChannel))
                        .map(Entity::getUniqueId).collect(Collectors.toSet()));
            }));
        });
    }

    @Override
    public void onConfigReload(ChatConfig config) {
        super.getChatPipeline().getChannelManager().reloadChannelData(config);
    }
}
