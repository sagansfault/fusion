package com.projecki.fusion.chat.pipeline.preprocessor.impl;

import com.projecki.fusion.chat.ChatConfig;
import com.projecki.fusion.chat.chattype.preprocessor.PreProcessorChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.PlayerToChannelPreChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.PlayerToPlayerPreChatType;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.preprocessor.PreProcessorManager;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;
import com.projecki.fusion.chat.tags.RedisTagStorage;
import com.projecki.fusion.chat.tags.TagStorage;
import com.projecki.fusion.chat.util.Placeholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TagsModule extends ChatPipelineModule<PreProcessorChatType, PreProcessorManager> {

    public static final String TAGS_PERMISSION_BASE = "fusion.chat.tags.";
    private final TagStorage tagStorage;

    private final Map<String, Component> availableTags = new HashMap<>();
    private final Map<UUID, String> tags = new HashMap<>();

    public TagsModule(PreProcessorManager preProcessorManager) {
        super(preProcessorManager);

        for (Map.Entry<String, String> entry : preProcessorManager.getPipeline().getConfig().tags().entrySet()) {
            availableTags.put(entry.getKey(), LegacyComponentSerializer.legacyAmpersand().deserialize(entry.getValue()));
        }

        tagStorage = new RedisTagStorage();
        tagStorage.getTags().thenAccept(this.tags::putAll);

        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToChannelPreChatType.class, chatType -> {
            chatType.getMessage().setReplacement(
                    Placeholder.PLAYER_CHAT_TAG.getPlaceholder(),
                    this.tags.getOrDefault(chatType.getPlayer().getUniqueId(), "")
            );
        }));

        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToPlayerPreChatType.class, chatType -> {
            chatType.getMessage().setReplacement(
                    Placeholder.PLAYER_CHAT_TAG.getPlaceholder(),
                    this.tags.getOrDefault(chatType.getSender().getUniqueId(), "")
            );
        }));
    }

    @Override
    public void onConfigReload(ChatConfig config) {
        availableTags.clear();
        for (Map.Entry<String, String> entry : config.tags().entrySet()) {
            availableTags.put(entry.getKey(), LegacyComponentSerializer.legacyAmpersand().deserialize(entry.getValue()));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        tagStorage.getTag(uuid).thenAccept(s -> s.ifPresent(tag -> this.tags.put(uuid, tag)));
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Optional.ofNullable(tags.get(uuid)).ifPresent(tag -> tagStorage.saveTag(uuid, tag));
    }

    @Override
    public void onServerDisable() {
        tagStorage.saveTags(this.tags);
    }
}
