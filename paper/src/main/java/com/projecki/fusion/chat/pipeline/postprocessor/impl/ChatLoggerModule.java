package com.projecki.fusion.chat.pipeline.postprocessor.impl;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.chat.chattype.postprocessor.PostProcessorChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.NetworkBroadcastPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.PlayerToChannelPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.PlayerToPlayerPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.ServerToChannelPostChatType;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.postprocessor.PostProcessorManager;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ChatLoggerModule extends ChatPipelineModule<PostProcessorChatType, PostProcessorManager> {

    private final FusionPaper plugin;

    public ChatLoggerModule(PostProcessorManager postProcessorManager) {
        super(postProcessorManager);
        plugin = postProcessorManager.getPipeline().getFusion();
        super.addComponent(new AbstractProcessorManager.Component<>(NetworkBroadcastPostChatType.class, message -> {
            if (message.getAudience().isEmpty()) return;
            plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(message.getMessage().build()));
        }));
        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToChannelPostChatType.class, message -> {
            if (message.getAudience().isEmpty()) return;
            plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(message.getMessage().build()));
        }));
        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToPlayerPostChatType.class, message -> {
            if (message.getAudience().isEmpty()) return;
            plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(message.getMessage().build()));
        }));
        super.addComponent(new AbstractProcessorManager.Component<>(ServerToChannelPostChatType.class, message -> {
            if (message.getAudience().isEmpty()) return;
            plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(message.getMessage().build()));
        }));
    }
}
