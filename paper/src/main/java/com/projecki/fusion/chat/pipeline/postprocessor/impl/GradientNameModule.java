package com.projecki.fusion.chat.pipeline.postprocessor.impl;

import com.projecki.fusion.chat.ChatConfig;
import com.projecki.fusion.chat.chattype.postprocessor.PostProcessorChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.PlayerToChannelPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.PlayerToPlayerPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.ServerToPlayerPostChatType;
import com.projecki.fusion.chat.chattype.preprocessor.PreProcessorChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.PlayerToChannelPreChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.PlayerToPlayerPreChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.ServerToPlayerPreChatType;
import com.projecki.fusion.chat.gradient.GradientStorage;
import com.projecki.fusion.chat.gradient.RedisGradientStorage;
import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.postprocessor.PostProcessorManager;
import com.projecki.fusion.chat.pipeline.preprocessor.PreProcessorManager;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;
import com.projecki.fusion.component.ComponentBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class GradientNameModule extends ChatPipelineModule<PostProcessorChatType, PostProcessorManager> {

    private static final Set<Gradient> AVAILABLE_GRADIENTS = new HashSet<>();
    private static final Map<UUID, Gradient> GRADIENTS = new HashMap<>();

    private static final GradientStorage GRADIENT_STORAGE = new RedisGradientStorage();

    public GradientNameModule(PostProcessorManager postProcessorManager) {
        super(postProcessorManager);
        AVAILABLE_GRADIENTS.addAll(postProcessorManager.getPipeline().getConfig().gradients());

        // get placeholder after this one and set replacement of it to gradient-ified version
        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToChannelPostChatType.class, chatType -> {
            this.applyGradients(chatType.getMessage(), chatType.getSenderUUID());
        }));
        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToPlayerPostChatType.class, chatType -> {
            this.applyGradients(chatType.getMessage(), chatType.getSenderUUID());
            this.applyGradients(chatType.getMessage(), chatType.getTargetUUID());
        }));
        super.addComponent(new AbstractProcessorManager.Component<>(ServerToPlayerPostChatType.class, chatType -> {
            this.applyGradients(chatType.getMessage(), chatType.getTarget());
        }));
    }

    @Override
    public void onConfigReload(ChatConfig config) {
        AVAILABLE_GRADIENTS.clear();
        AVAILABLE_GRADIENTS.addAll(config.gradients());
    }

    public void applyGradients(Message message, UUID player) {
        Gradient gradient = GRADIENTS.get(player);
        if (gradient == null) {
            return;
        }
        message.getFormat().replaceText(builder -> {
            Pattern pattern = Pattern.compile("(%gradient%)\\s*(%\\w+%)", Pattern.CASE_INSENSITIVE);
            builder.match(pattern).replacement((res, b) -> {
                if (res.groupCount() == 2) {
                    String target = res.group(2);
                    message.getReplacement(target).ifPresent(replacement -> {
                        message.setReplacement(target, this.gradientify(replacement, gradient));
                    });
                }
                // remove the gradient placeholder regardless of if there was a replacement or not
                message.setReplacement("%gradient%", Component.empty());
                return Component.empty();
            });
        });
    }

    private Component gradientify(String word, Gradient gradient) {
        ComponentBuilder builder = ComponentBuilder.builder();

        TextColor gradientFirst = TextColor.fromHexString(gradient.firstHex());
        TextColor gradientSecond = TextColor.fromHexString(gradient.secondHex());

        if (gradientFirst == null || gradientSecond == null) {
            return Component.text(word);
        }

        float step = 1.0f / (word.length() - 1);
        float current = 0.0f;
        for (char c : word.toCharArray()) {
            builder.content(String.valueOf(c), TextColor.lerp(current, gradientFirst, gradientSecond));
            current += step;
        }

        return builder.toComponent();
    }

    private Component gradientify(Component word, Gradient gradient) {
        return this.gradientify(PlainTextComponentSerializer.plainText().serialize(word), gradient);
    }

    @Override
    public void onServerDisable() {
        GRADIENT_STORAGE.saveGradients(GRADIENTS);
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        GRADIENT_STORAGE.loadGradient(uuid).thenAccept(opt -> opt.ifPresent(id -> {
            for (Gradient availableGradient : AVAILABLE_GRADIENTS) {
                if (availableGradient.id().equalsIgnoreCase(id)) {
                    GRADIENTS.put(uuid, availableGradient);
                    break;
                }
            }
        }));
    }

    @EventHandler
    public void leave(PlayerQuitEvent event) {
        GRADIENTS.remove(event.getPlayer().getUniqueId());
    }

    public record Gradient(String id, String firstHex, String secondHex) {}
}
