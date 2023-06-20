package com.projecki.fusion.chat;

import com.projecki.fusion.chat.pipeline.postprocessor.impl.GradientNameModule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record ChatConfig(boolean enabled,
                         String dmFormat,
                         String shoutFormat,
                         int shoutCooldown,
                         Set<String> shoutBlacklistedServers,
                         Set<ChatChannelData> chatChannelData,
                         String defaultChannel,
                         Map<String, String> emojis,
                         Map<String, String> tags,
                         Set<GradientNameModule.Gradient> gradients) {

    public ChatConfig {
        shoutBlacklistedServers = Objects.requireNonNullElse(shoutBlacklistedServers, new HashSet<>());
        chatChannelData = Objects.requireNonNullElse(chatChannelData, new HashSet<>());
        emojis = Objects.requireNonNullElse(emojis, new HashMap<>());
        tags = Objects.requireNonNullElse(tags, new HashMap<>());
        gradients = Objects.requireNonNullElse(gradients, new HashSet<>());
    }

    public ChatConfig() {
        this(false, "", "", 30, new HashSet<>(), new HashSet<>(),
                "", new HashMap<>(), new HashMap<>(), new HashSet<>());
    }

    public record ChatChannelData(String id, String defaultFormat,
                                  Map<String, String> customFormats,
                                  String[] targetGroup) {

        public ChatChannelData {
            id = Objects.requireNonNullElse(id, "");
            defaultFormat = Objects.requireNonNullElse(defaultFormat, "");
            customFormats = Objects.requireNonNullElse(customFormats, new HashMap<>());
            targetGroup = Objects.requireNonNullElse(targetGroup, new String[]{});
        }
    }
}
