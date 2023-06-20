package com.projecki.fusion.chat.pipeline.postprocessor;

import com.projecki.fusion.chat.chattype.postprocessor.PostProcessorChatType;
import com.projecki.fusion.chat.pipeline.ChatPipeline;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.postprocessor.impl.ChannelModule;
import com.projecki.fusion.chat.pipeline.postprocessor.impl.ChatLoggerModule;
import com.projecki.fusion.chat.pipeline.postprocessor.impl.FormatDefinerModule;
import com.projecki.fusion.chat.pipeline.postprocessor.impl.GradientNameModule;
import com.projecki.fusion.chat.pipeline.postprocessor.impl.NetworkBroadcastModule;
import com.projecki.fusion.chat.pipeline.postprocessor.impl.ServerToChannelModule;
import com.projecki.fusion.chat.pipeline.postprocessor.impl.ServerToPlayerModule;
import com.projecki.fusion.chat.pipeline.postprocessor.impl.WhisperModule;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;

import java.util.List;

public class PostProcessorManager extends AbstractProcessorManager<PostProcessorChatType> {

    public PostProcessorManager(ChatPipeline pipeline) {
        super(pipeline);
    }

    @Override
    protected List<ChatPipelineModule<PostProcessorChatType, ? extends AbstractProcessorManager<PostProcessorChatType>>> getModuleProcessingOrder() {
        return List.of(
                new FormatDefinerModule(this),

                new ChannelModule(this),

                new NetworkBroadcastModule(this),
                new ServerToChannelModule(this),
                new ServerToPlayerModule(this),

                new WhisperModule(this),

                new GradientNameModule(this),

                new ChatLoggerModule(this)
        );
    }
}
