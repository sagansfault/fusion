package com.projecki.fusion.chat.pipeline.preprocessor.impl;

import com.google.common.base.Strings;
import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.chat.chatfilter.ChatFilterStorage;
import com.projecki.fusion.chat.chatfilter.RedisChatFilterStorage;
import com.projecki.fusion.chat.chattype.preprocessor.PreProcessorChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.PlayerToChannelPreChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.PlayerToPlayerPreChatType;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.preprocessor.PreProcessorManager;
import com.projecki.fusion.chat.pipeline.processor.AbstractProcessorManager;
import com.projecki.fusion.chat.util.Placeholder;
import com.projecki.fusion.message.MessageClient;
import com.projecki.fusion.redis.CommonRedisKeys;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatFilterModule extends ChatPipelineModule<PreProcessorChatType, PreProcessorManager> {

    // map of blocked words/phrases, literals or regex, both in patterns, regex tied to id, literal's id same as literal
    public static final Map<String, Pattern> BLOCKED = new HashMap<>();

    private static final ChatFilterStorage CHAT_FILTER_STORAGE = new RedisChatFilterStorage();

    public ChatFilterModule(PreProcessorManager preProcessorManager) {
        super(preProcessorManager);
        FusionPaper.getMessageClient().registerMessageListener(this);

        CHAT_FILTER_STORAGE.loadBlockedChatFilter().thenAccept(map -> BLOCKED.putAll(map.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> Pattern.compile(entry.getValue(), Pattern.CASE_INSENSITIVE)
        ))));

        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToChannelPreChatType.class, message -> {
            message.getMessage().mutateExistingReplacement(Placeholder.MESSAGE.getPlaceholder(), this::filter);
        }));

        super.addComponent(new AbstractProcessorManager.Component<>(PlayerToPlayerPreChatType.class, message -> {
            message.getMessage().mutateExistingReplacement(Placeholder.MESSAGE.getPlaceholder(), this::filter);
        }));
    }

    private Component filter(Component component) {
        Component returnable = component;
        for (Map.Entry<String, Pattern> entry : BLOCKED.entrySet()) {
            Pattern pattern = entry.getValue();
            returnable = returnable.replaceText(builder -> {
                builder.match(pattern).replacement((res, b) -> Component.text(Strings.repeat("*", res.group().length())));
            });
        }
        return returnable;
    }

    public static void block(String id, Pattern pattern) {
        BLOCKED.put(id, pattern);
        CHAT_FILTER_STORAGE.saveBlockedChatFilter(id, pattern.pattern()).whenComplete((v, ex) -> {
            Map<String, String> casted = BLOCKED.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().pattern()
            ));
            FusionPaper.getMessageClient().send(CommonRedisKeys.SERVER_CHAT_AND_API.getKey(), new ChatFilterUpdate(casted));
        });
    }

    public static CompletableFuture<Boolean> remove(String id) {
        BLOCKED.remove(id);
        return CHAT_FILTER_STORAGE.removeBlockedChatFilter(id).thenApply(removed -> {
            Map<String, String> casted = BLOCKED.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().pattern()
            ));
            FusionPaper.getMessageClient().send(CommonRedisKeys.SERVER_CHAT_AND_API.getKey(), new ChatFilterUpdate(casted));
            return removed;
        });
    }

    @Override
    public void onServerEnable() {
        CHAT_FILTER_STORAGE.loadBlockedChatFilter().thenAccept(map -> {
            if (map != null) {
                BLOCKED.putAll(map.entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Pattern.compile(entry.getValue(), Pattern.CASE_INSENSITIVE)
                )));
            }
        });
    }

    @MessageClient.MessageListener
    public void chatFilterUpdateListener(String channel, ChatFilterUpdate updateMessage) {
        BLOCKED.clear();
        BLOCKED.putAll(updateMessage.blocked().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> Pattern.compile(entry.getValue(), Pattern.CASE_INSENSITIVE)
        )));
    }

    private record ChatFilterUpdate(Map<String, String> blocked) implements MessageClient.Message {}
}
