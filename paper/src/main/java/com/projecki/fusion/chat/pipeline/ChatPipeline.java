package com.projecki.fusion.chat.pipeline;

import co.aikar.commands.PaperCommandManager;
import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.chat.ChatConfig;
import com.projecki.fusion.chat.channel.ChannelManager;
import com.projecki.fusion.chat.channel.ChatChannel;
import com.projecki.fusion.chat.chattype.ChatType;
import com.projecki.fusion.chat.chattype.preprocessor.PreProcessorChatType;
import com.projecki.fusion.chat.command.ChannelCommand;
import com.projecki.fusion.chat.command.ChatFilterCommand;
import com.projecki.fusion.chat.command.ReplyCommand;
import com.projecki.fusion.chat.command.ShoutCommand;
import com.projecki.fusion.chat.command.WhisperCommand;
import com.projecki.fusion.chat.intake.ChatIntakeListener;
import com.projecki.fusion.chat.pipeline.ChatPipelineModule;
import com.projecki.fusion.chat.pipeline.postprocessor.PostProcessorManager;
import com.projecki.fusion.chat.pipeline.preprocessor.PreProcessorManager;
import com.projecki.fusion.config.PaperHermesConfig;
import com.projecki.fusion.config.serialize.JacksonSerializer;
import com.projecki.fusion.redis.CommonRedisKeys;
import com.projecki.fusion.redis.pubsub.message.impl.chat.ChatTypeMessage;
import com.projecki.fusion.redis.pubsub.message.impl.chat.PlayerToChannelChatTypeMessage;

import java.util.logging.Level;
import java.util.stream.Collectors;

public class ChatPipeline {

    public static boolean ENABLED = false;

    private final PreProcessorManager preProcessorManager;
    private final PostProcessorManager postProcessorManager;

    private final FusionPaper fusion;
    private ChannelManager channelManager;
    private ChatConfig config;

    public ChatPipeline(FusionPaper fusion) {
        this.fusion = fusion;
        this.config = new ChatConfig();

        this.preProcessorManager = new PreProcessorManager(this);
        this.postProcessorManager = new PostProcessorManager(this);

        PaperHermesConfig<ChatConfig> loader = new PaperHermesConfig<>(JacksonSerializer.ofYaml(ChatConfig.class), fusion, "chat");
        loader.loadConfig().thenAccept(configOptional -> configOptional.ifPresentOrElse(config -> {
            this.config = config;

            ENABLED = config.enabled();
            if (!ENABLED) {
                fusion.getLogger().severe("Chat has been set to be DISABLED, change this value in config and restart the server if you wish to enable it");
                return;
            } else {
                fusion.getLogger().info("Chat has been set to be ENABLED, change this value in config and restart the server if you wish to disable it");

            }

            preProcessorManager.initModules();
            postProcessorManager.initModules();

            channelManager = new ChannelManager(fusion, config);
            // only register commands after channel manager instantiation since it requires it
            this.registerCommands();
            this.registerIntakeListeners();

            fusion.getLogger().log(Level.INFO, "Loaded chat initial config from Hermes");
        }, () -> fusion.getLogger().log(Level.SEVERE, "Config could not be loaded, chat will not function!")));
        loader.onUpdate(configOptional -> configOptional.ifPresent(config -> {
            this.config = config;
            preProcessorManager.getModules().forEach(module -> module.onConfigReload(config));
            postProcessorManager.getModules().forEach(module -> module.onConfigReload(config));
            fusion.getLogger().log(Level.INFO, "Loaded updated config from Hermes and ran modules update");
        }));
    }

    public <K extends PreProcessorChatType> void intake(Class<K> type, K pre) {
        PreProcessorChatType preProcessed = preProcessorManager.intake(type, pre);
        ChatTypeMessage mapped = preProcessed.mapToChatTypeMessage();
        FusionPaper.getMessageClient().send(CommonRedisKeys.SERVER_CHAT_AND_API.getKey(), mapped);
    }

    private void registerIntakeListeners() {
        ChatIntakeListener intakeListener = new ChatIntakeListener(this);
        fusion.getServer().getPluginManager().registerEvents(intakeListener, fusion);
        FusionPaper.getMessageClient().subscribe(CommonRedisKeys.SERVER_CHAT_AND_API.getKey());
        FusionPaper.getMessageClient().registerMessageListener(intakeListener);
    }

    private void registerCommands() {
        PaperCommandManager commandManager = FusionPaper.getCommandManager();

        commandManager.getCommandReplacements().addReplacement(
                "channel-command-alias",
                channelManager.getChannels().stream().map(ChatChannel::getId).collect(Collectors.joining("|"))
        );

        commandManager.registerCommand(new WhisperCommand(this));
        commandManager.registerCommand(new ReplyCommand(this));
        commandManager.registerCommand(new ChatFilterCommand(this));
        commandManager.registerCommand(new ChannelCommand(this));
        commandManager.registerCommand(new ShoutCommand(this));
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public ChatConfig getConfig() {
        return config;
    }

    public FusionPaper getFusion() {
        return fusion;
    }

    public PreProcessorManager getPreProcessorManager() {
        return preProcessorManager;
    }

    public PostProcessorManager getPostProcessorManager() {
        return postProcessorManager;
    }

    public void onServerEnable() {
        this.preProcessorManager.getModules().forEach(ChatPipelineModule::onServerEnable);
        this.postProcessorManager.getModules().forEach(ChatPipelineModule::onServerEnable);
    }

    public void onServerDisable() {
        this.preProcessorManager.getModules().forEach(ChatPipelineModule::onServerDisable);
        this.postProcessorManager.getModules().forEach(ChatPipelineModule::onServerDisable);
    }
}
