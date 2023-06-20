package com.projecki.fusion.chat.pipeline.preprocessor.impl;

import com.projecki.fusion.chat.ChatConfig;
import com.projecki.fusion.chat.chattype.preprocessor.PreProcessorChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.NetworkBroadcastPreChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.PlayerToChannelPreChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.PlayerToPlayerPreChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.ServerToChannelPreChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.ServerToPlayerPreChatType;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.preprocessor.PreProcessorManager;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;
import com.projecki.fusion.chat.util.Placeholder;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class EmojiResolverModule extends ChatPipelineModule<PreProcessorChatType, PreProcessorManager> {

    public static final Pattern EMOJI = Pattern.compile(":(\\w+):", Pattern.CASE_INSENSITIVE);
    private final Map<String, String> emojis;

    public EmojiResolverModule(PreProcessorManager preProcessorManager) {
        super(preProcessorManager);

        emojis = preProcessorManager.getPipeline().getConfig().emojis();

        super.addComponent(new AbstractProcessorManager.Component<>(NetworkBroadcastPreChatType.class, chatType -> {
            chatType.getMessage().mutateExistingReplacement(Placeholder.MESSAGE.getPlaceholder(), this::resolve);
        }));

        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToChannelPreChatType.class, chatType -> {
            chatType.getMessage().mutateExistingReplacement(Placeholder.MESSAGE.getPlaceholder(), this::resolve);
        }));

        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToPlayerPreChatType.class, chatType -> {
            chatType.getMessage().mutateExistingReplacement(Placeholder.MESSAGE.getPlaceholder(), this::resolve);
        }));

        super.addComponent(new AbstractProcessorManager.Component<>(ServerToChannelPreChatType.class, chatType -> {
            chatType.getMessage().mutateExistingReplacement(Placeholder.MESSAGE.getPlaceholder(), this::resolve);
        }));

        super.addComponent(new AbstractProcessorManager.Component<>(ServerToPlayerPreChatType.class, chatType -> {
            chatType.getMessage().mutateExistingReplacement(Placeholder.MESSAGE.getPlaceholder(), this::resolve);
        }));
    }

    private Component resolve(Component input) {
        return input.replaceText(builder -> builder.match(EMOJI).replacement((matchResult, b) -> {
            String emoji = matchResult.group(1); // match the inner word and not the colons :'s
            Optional<String> emojiUnicodeOptional = Optional.ofNullable(emojis.get(emoji));
            // return the full capture back or the replacement
            return emojiUnicodeOptional.map(Component::text).orElseGet(() -> Component.text(matchResult.group(0)));
        }));
    }

    @Override
    public void onConfigReload(ChatConfig config) {
        emojis.clear();
        emojis.putAll(config.emojis());
    }
}
