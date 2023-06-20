package com.projecki.fusion.chat.pipeline.processor;

import com.projecki.fusion.chat.chattype.ChatType;
import com.projecki.fusion.chat.pipeline.ChatPipeline;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AbstractProcessorManager<T extends ChatType> {

    private final ChatPipeline pipeline;
    private final Set<Container<? extends ChatType>> containers = new HashSet<>();
    private final List<ChatPipelineModule<T, ? extends AbstractProcessorManager<T>>> modules = new ArrayList<>();

    public AbstractProcessorManager(ChatPipeline pipeline) {
        this.pipeline = pipeline;
    }

    public final void initModules() {
        this.modules.clear();
        this.modules.addAll(getModuleProcessingOrder());
    }

    protected abstract List<ChatPipelineModule<T, ? extends AbstractProcessorManager<T>>> getModuleProcessingOrder();

    public final <K extends ChatType> T intake(Class<K> type, K chatType) {
        for (Component<K> component : this.getContainer(type).components()) {
            component.function().accept(chatType);
        }
        return (T) chatType; // funny java
    }

    public final <K extends ChatType> Container<K> getContainer(Class<K> type) {
        for (Container<? extends ChatType> container : this.containers) {
            if (container.type() == type) {
                return (Container<K>) container;
            }
        }
        Container<K> created = new Container<>(type);
        containers.add(created);
        return created;
    }

    public ChatPipeline getPipeline() {
        return pipeline;
    }

    public List<ChatPipelineModule<T, ? extends AbstractProcessorManager<T>>> getModules() {
        return Collections.unmodifiableList(this.modules);
    }

    public record Container<K extends ChatType>(Class<K> type, List<Component<K>> components) {

        public Container(Class<K> type) {
            this(type, new ArrayList<>());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Container<?> container = (Container<?>) o;
            return type == container.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type);
        }
    }

    public record Component<M extends ChatType>(Class<M> type, Consumer<M> function) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Component<?> component = (Component<?>) o;
            return type == component.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type);
        }
    }
}
