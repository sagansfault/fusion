package com.projecki.fusion.chat.pipeline;

import com.projecki.fusion.chat.ChatConfig;
import com.projecki.fusion.chat.chattype.ChatType;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public abstract class ChatPipelineModule<M extends ChatType, T extends AbstractProcessorManager<M>> implements Listener {

    private final ChatPipeline chatPipeline;
    private final T processorManager;

    public ChatPipelineModule(T processorManager) {
        this.processorManager = processorManager;
        this.chatPipeline = processorManager.getPipeline();
        Bukkit.getServer().getPluginManager().registerEvents(this, chatPipeline.getFusion());
    }

    public ChatPipeline getChatPipeline() {
        return chatPipeline;
    }

    public <P extends M> void addComponent(AbstractProcessorManager.Component<P> component) {
        processorManager.getContainer(component.type()).components().add(component);
    }

    public void onServerEnable() {}

    public void onServerDisable() {}

    public void onConfigReload(ChatConfig config) {}
}
