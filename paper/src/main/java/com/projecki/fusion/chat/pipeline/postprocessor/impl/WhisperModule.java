package com.projecki.fusion.chat.pipeline.postprocessor.impl;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.chat.chattype.postprocessor.PostProcessorChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.PlayerToPlayerPostChatType;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.postprocessor.PostProcessorManager;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;
import com.projecki.fusion.chat.util.Placeholder;
import com.projecki.fusion.chat.whisper.RedisWhisperBlockedStorage;
import com.projecki.fusion.chat.whisper.WhisperBlockedStorage;
import com.projecki.fusion.util.Pair;
import io.lettuce.core.SetArgs;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WhisperModule extends ChatPipelineModule<PostProcessorChatType, PostProcessorManager> {

    public static final Map<UUID, Set<UUID>> BLOCKED_DATA = new HashMap<>();
    private final WhisperBlockedStorage whisperBlockedStorage = new RedisWhisperBlockedStorage();

    public WhisperModule(PostProcessorManager postProcessorManager) {
        super(postProcessorManager);
        whisperBlockedStorage.loadBlockedWhisperers().thenAccept(BLOCKED_DATA::putAll);

        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToPlayerPostChatType.class, message -> {
            String senderName = message.getSenderName();
            UUID senderUUID = message.getSenderUUID();
            UUID targetUUID = message.getTargetUUID();

            if (BLOCKED_DATA.getOrDefault(targetUUID, new HashSet<>()).contains(senderUUID)) {
                return;
            }

            message.getMessage().setReplacement(Placeholder.PLAYER_SENDER.getPlaceholder(), senderName);
            message.getMessage().setReplacement(Placeholder.PLAYER_TARGET.getPlaceholder(), message.getTargetName());

            message.getAudience().add(targetUUID);
            message.getAudience().add(senderUUID);

            /*
            This would usually be in the whisper command itself, but it does not take into account any of the
            parsing or checks done by this module like whisper/dm blacklists. So we only set the reply if the
            whisper was successful.
             */
            setRecentWhisperer(targetUUID, senderUUID, senderName);
        }));
    }

    @Override
    public void onServerDisable() {
        whisperBlockedStorage.saveBlockedWhisperers(BLOCKED_DATA);
    }

    public static CompletableFuture<Optional<Pair<UUID, String>>> getRecentWhisperer(UUID receiver) {
        return FusionPaper.getRedisCommands().get("fusion:chat:whisper:recent" + receiver.toString()).thenApply(s -> {
            if (s == null) {
                return Optional.<Pair<UUID, String>>empty();
            } else {
                try {
                    String[] parts = s.split("-", 2);
                    if (parts.length != 2) {
                        return Optional.<Pair<UUID, String>>empty();
                    }
                    String name = parts[0];
                    String uuidString = parts[1];
                    UUID uuid = UUID.fromString(uuidString);
                    return Optional.of(Pair.of(uuid, name));
                } catch (IllegalArgumentException ignored) {
                    return Optional.<Pair<UUID, String>>empty();
                }
            }
        }).toCompletableFuture();
    }

    public static void setRecentWhisperer(UUID receiver, UUID whisperer, String whispererName) {
        FusionPaper.getRedisCommands().set(
                "fusion:chat:whisper:recent" + receiver.toString(),
                whispererName + "-" + whisperer.toString(),
                SetArgs.Builder.ex(Duration.ofMinutes(30))
        );
    }
}
