package com.projecki.fusion.chat.pipeline.postprocessor.impl;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.chat.chattype.postprocessor.PostProcessorChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.NetworkBroadcastPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.PlayerToChannelPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.PlayerToPlayerPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.ServerToChannelPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.ServerToPlayerPostChatType;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.postprocessor.PostProcessorManager;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;
import com.projecki.fusion.chat.util.Placeholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class FormatDefinerModule extends ChatPipelineModule<PostProcessorChatType, PostProcessorManager> {

    public FormatDefinerModule(PostProcessorManager postProcessorManager) {
        super(postProcessorManager);

        super.addComponent(new AbstractProcessorManager.Component<>(NetworkBroadcastPostChatType.class, chatType -> {
            // dont actually need to set a format, %message% -> message replacement default is already set
            // this also makes it so shouts that use this ChatType with a custom format won't be overwritten
        }));

        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToChannelPostChatType.class, chatType -> {
            FusionPaper.getServerInfo().ifPresent(serverInfo -> {
                String serverName = serverInfo.getServerName();
                String format = chatType.getTargetChannel().getFormat(serverName);
                chatType.getMessage().setFormat(LegacyComponentSerializer.legacyAmpersand().deserialize(format));
            });
        }));

        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToPlayerPostChatType.class, chatType -> {
            String format = postProcessorManager.getPipeline().getConfig().dmFormat();
            chatType.getMessage().setFormat(LegacyComponentSerializer.legacyAmpersand().deserialize(format));
        }));

        super.addComponent(new AbstractProcessorManager.Component<>(ServerToChannelPostChatType.class, chatType -> {
            FusionPaper.getServerInfo().ifPresent(serverInfo -> {
                String serverName = serverInfo.getServerName();
                String format = chatType.getTargetChannel().getFormat(serverName);
                chatType.getMessage().setFormat(LegacyComponentSerializer.legacyAmpersand().deserialize(format));
            });
        }));

        super.addComponent(new AbstractProcessorManager.Component<>(ServerToPlayerPostChatType.class, chatType -> {
            chatType.getMessage().setFormat(Component.text(Placeholder.MESSAGE.getPlaceholder()));
        }));
    }
}
