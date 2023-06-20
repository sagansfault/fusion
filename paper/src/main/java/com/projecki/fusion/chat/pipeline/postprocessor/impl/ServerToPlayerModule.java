package com.projecki.fusion.chat.pipeline.postprocessor.impl;

import com.projecki.fusion.chat.chattype.postprocessor.PostProcessorChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.ServerToPlayerPostChatType;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.postprocessor.PostProcessorManager;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;

public class ServerToPlayerModule extends ChatPipelineModule<PostProcessorChatType, PostProcessorManager> {

    public ServerToPlayerModule(PostProcessorManager postProcessorManager) {
        super(postProcessorManager);

        super.addComponent(new AbstractProcessorManager.Component<>(ServerToPlayerPostChatType.class, message -> {
            message.getAudience().add(message.getTarget());
        }));
    }
}
