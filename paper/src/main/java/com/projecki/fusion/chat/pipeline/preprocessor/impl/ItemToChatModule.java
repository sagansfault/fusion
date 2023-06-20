package com.projecki.fusion.chat.pipeline.preprocessor.impl;

import com.projecki.fusion.chat.chattype.preprocessor.PreProcessorChatType;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.preprocessor.PreProcessorManager;

public class ItemToChatModule extends ChatPipelineModule<PreProcessorChatType, PreProcessorManager> {

    public ItemToChatModule(PreProcessorManager processorManager) {
        super(processorManager);

    }
}
