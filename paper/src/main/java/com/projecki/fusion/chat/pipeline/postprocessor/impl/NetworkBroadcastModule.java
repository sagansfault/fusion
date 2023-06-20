package com.projecki.fusion.chat.pipeline.postprocessor.impl;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.chat.chattype.postprocessor.PostProcessorChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.NetworkBroadcastPostChatType;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.postprocessor.PostProcessorManager;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.stream.Collectors;

public class NetworkBroadcastModule extends ChatPipelineModule<PostProcessorChatType, PostProcessorManager> {

    public NetworkBroadcastModule(PostProcessorManager postProcessorManager) {
        super(postProcessorManager);

        FusionPaper.getServerInfo().ifPresent(serverInfo -> {
            String serverName = serverInfo.getServerName();

            super.addComponent(new AbstractProcessorManager.Component<>(NetworkBroadcastPostChatType.class, message -> {
                for (String excludedServer : message.getExcludedServers()) {
                    if (excludedServer.equalsIgnoreCase(serverName)) {
                        return;
                    }
                }

                message.getAudience().addAll(Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).collect(Collectors.toSet()));
            }));
        });
    }
}
