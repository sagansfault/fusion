package com.projecki.fusion.chat.pipeline.postprocessor.impl;

import com.projecki.fusion.chat.channel.ChatChannel;
import com.projecki.fusion.chat.chattype.postprocessor.PostProcessorChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.ServerToChannelPostChatType;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.postprocessor.PostProcessorManager;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.stream.Collectors;

public class ServerToChannelModule extends ChatPipelineModule<PostProcessorChatType, PostProcessorManager> {

    public ServerToChannelModule(PostProcessorManager postProcessorManager) {
        super(postProcessorManager);

        super.addComponent(new AbstractProcessorManager.Component<>(ServerToChannelPostChatType.class, message -> {
            ChatChannel targetChannel = message.getTargetChannel();
            message.getAudience().addAll(Bukkit.getOnlinePlayers().stream().filter(targetChannel::canSee)
                    .map(Entity::getUniqueId).collect(Collectors.toSet()));
        }));
    }
}
