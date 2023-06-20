package com.projecki.fusion.chat.pipeline.preprocessor.impl;

import com.projecki.fusion.chat.ChatConfig;
import com.projecki.fusion.chat.chattype.preprocessor.PreProcessorChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.NetworkBroadcastPreChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.PlayerToChannelPreChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.PlayerToPlayerPreChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.ServerToChannelPreChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.ServerToPlayerPreChatType;
import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.preprocessor.PreProcessorManager;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;
import com.projecki.fusion.chat.util.Placeholder;
import com.projecki.fusion.component.ComponentBuilder;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class PlaceholderAndPAPIModule extends ChatPipelineModule<PreProcessorChatType, PreProcessorManager> {

    private static final Pattern PLACEHOLDER_MATCHER = Pattern.compile("%\\w+%", Pattern.CASE_INSENSITIVE);
    private final Set<String> placeholders = new HashSet<>();

    public PlaceholderAndPAPIModule(PreProcessorManager preProcessorManager) {
        super(preProcessorManager);
        this.registerPlaceholders(preProcessorManager.getPipeline().getConfig());

        super.addComponent(new AbstractProcessorManager.Component<>(NetworkBroadcastPreChatType.class, message -> {
            this.addReplacements(message.getMessage());
        }));

        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToChannelPreChatType.class, message -> {
            this.addReplacements(message.getMessage());
            this.applyPlaceholderAPIReplacements(message.getPlayer(), message.getMessage());
        }));

        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToPlayerPreChatType.class, message -> {
            this.addReplacements(message.getMessage());
            this.applyPlaceholderAPIReplacements(message.getSender(), message.getMessage());
        }));

        super.addComponent(new AbstractProcessorManager.Component<>(ServerToChannelPreChatType.class, message -> {
            this.addReplacements(message.getMessage());
        }));

        super.addComponent(new AbstractProcessorManager.Component<>(ServerToPlayerPreChatType.class, message -> {
            this.addReplacements(message.getMessage());
        }));
    }

    @Override
    public void onConfigReload(ChatConfig config) {
        this.placeholders.clear();
        this.registerPlaceholders(config);
    }

    private void applyPlaceholderAPIReplacements(Player player, Message message) {
        for (String key : message.getReplacements().keySet()) {
            String replacement = PlaceholderAPI.setPlaceholders(player, key);
            // placeholder api sucks
            if (!Placeholder.PLACEHOLDER_MATCHER.matcher(replacement).matches()) {
                String rawReplacement = PlaceholderAPI.setPlaceholders(player, key);
                String properCharReplacement = rawReplacement.replaceAll("§", "&");
                Component temp = ComponentBuilder.builder(properCharReplacement).toComponent();
                message.setReplacement(key, temp);
            }
        }
    }

    private void addReplacements(Message message) {
        for (String placeholder : placeholders) {
            // only add the placeholder if there's not already one set
            message.setIfAbsent(placeholder, Component.empty());
        }
    }

    private void registerPlaceholders(ChatConfig config) {
        config.chatChannelData().forEach(c -> {
            super.getChatPipeline().getFusion().getLogger().log(Level.INFO, "Loaded channel: " + c.id());
        });
        for (ChatConfig.ChatChannelData datum : config.chatChannelData()) {
            PLACEHOLDER_MATCHER.matcher(datum.defaultFormat()).results().forEach(result -> placeholders.add(result.group()));
            for (String format : datum.customFormats().values()) {
                PLACEHOLDER_MATCHER.matcher(format).results().forEach(result -> placeholders.add(result.group()));
            }
        }
    }
}
