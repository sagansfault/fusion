package com.projecki.fusion.chat.pipeline.preprocessor;

import com.projecki.fusion.chat.chattype.preprocessor.PreProcessorChatType;
import com.projecki.fusion.chat.pipeline.ChatPipeline;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.preprocessor.impl.ChatFilterModule;
import com.projecki.fusion.chat.pipeline.preprocessor.impl.EmojiResolverModule;
import com.projecki.fusion.chat.pipeline.preprocessor.impl.ItemToChatModule;
import com.projecki.fusion.chat.pipeline.preprocessor.impl.PlaceholderAndPAPIModule;
import com.projecki.fusion.chat.pipeline.preprocessor.impl.TagsModule;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;

import java.util.List;

public class PreProcessorManager extends AbstractProcessorManager<PreProcessorChatType> {

    public PreProcessorManager(ChatPipeline pipeline) {
        super(pipeline);
    }

    @Override
    protected List<ChatPipelineModule<PreProcessorChatType, ? extends AbstractProcessorManager<PreProcessorChatType>>> getModuleProcessingOrder() {
        return List.of(
                new PlaceholderAndPAPIModule(this),
                new ChatFilterModule(this),
                new TagsModule(this),
                new ItemToChatModule(this),
                new EmojiResolverModule(this)
        );
    }
}
