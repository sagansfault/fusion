package com.projecki.fusion.chat.intake;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.ServerInfo;
import com.projecki.fusion.chat.chattype.ChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.NetworkBroadcastPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.PlayerToChannelPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.PlayerToPlayerPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.ServerToChannelPostChatType;
import com.projecki.fusion.chat.chattype.postprocessor.impl.ServerToPlayerPostChatType;
import com.projecki.fusion.chat.chattype.preprocessor.impl.PlayerToChannelPreChatType;
import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.chat.pipeline.ChatPipeline;
import com.projecki.fusion.chat.pipeline.postprocessor.PostProcessorManager;
import com.projecki.fusion.message.MessageClient;
import com.projecki.fusion.redis.pubsub.message.impl.chat.NetworkBroadcastChatTypeMessage;
import com.projecki.fusion.redis.pubsub.message.impl.chat.PlayerToChannelChatTypeMessage;
import com.projecki.fusion.redis.pubsub.message.impl.chat.PlayerToPlayerChatTypeMessage;
import com.projecki.fusion.redis.pubsub.message.impl.chat.ServerToChannelChatTypeMessage;
import com.projecki.fusion.redis.pubsub.message.impl.chat.ServerToPlayerChatTypeMessage;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;

public class ChatIntakeListener implements Listener {

    private final ChatPipeline chatPipeline;
    private final PostProcessorManager postProcessorManager;

    public ChatIntakeListener(ChatPipeline chatPipeline) {
        this.chatPipeline = chatPipeline;
        this.postProcessorManager = chatPipeline.getPostProcessorManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void chat(AsyncChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        FusionPaper.getChatPipeline().getChannelManager().getChannel(player.getUniqueId()).ifPresent(channel -> {
            Component message = event.message();
            FusionPaper.getServerInfo().map(ServerInfo::getServerName).ifPresent(serverName -> {

                Message initial = Message.fromInitialMessage(message);
                chatPipeline.intake(
                        PlayerToChannelPreChatType.class,
                        new PlayerToChannelPreChatType(initial, channel, serverName, player)
                );
            });
        });
    }

    @MessageClient.MessageListener
    public void onNetworkBroadcastMessage(String channel, NetworkBroadcastChatTypeMessage networkBroadcast) {
        NetworkBroadcastPostChatType chatType = new NetworkBroadcastPostChatType(networkBroadcast.getMessage(), networkBroadcast.getExcludedServers());
        ChatType result = postProcessorManager.intake(NetworkBroadcastPostChatType.class, chatType);
        Component built = result.getMessage().build();
        result.getAudience().stream()
                .flatMap(uuid -> Optional.ofNullable(Bukkit.getPlayer(uuid)).stream())
                .forEach(player -> player.sendMessage(built));
    }

    @MessageClient.MessageListener
    public void onPlayerChatMessage(String channel, PlayerToChannelChatTypeMessage playerToChannel) {
        FusionPaper.getChatPipeline().getChannelManager().getChannel(playerToChannel.getChannelId()).ifPresent(chatChannel -> {
            PlayerToChannelPostChatType chatType = new PlayerToChannelPostChatType(
                    playerToChannel.getMessage(),
                    chatChannel,
                    playerToChannel.getOriginServer(),
                    playerToChannel.getSenderUUID(),
                    playerToChannel.getSenderName()
            );

            ChatType result = postProcessorManager.intake(PlayerToChannelPostChatType.class, chatType);
            Component built = result.getMessage().build();
            result.getAudience().stream()
                    .flatMap(uuid -> Optional.ofNullable(Bukkit.getPlayer(uuid)).stream())
                    .forEach(player -> player.sendMessage(built));
        });
    }

    @MessageClient.MessageListener
    public void onPlayerToPlayerChatMessage(String channel, PlayerToPlayerChatTypeMessage playerToPlayer) {
        PlayerToPlayerPostChatType chatType = new PlayerToPlayerPostChatType(
                playerToPlayer.getMessage(),
                playerToPlayer.getSenderUUID(),
                playerToPlayer.getTargetUUID(),
                playerToPlayer.getSenderName(),
                playerToPlayer.getTargetName()
        );
        ChatType result = postProcessorManager.intake(PlayerToPlayerPostChatType.class, chatType);
        Component built = result.getMessage().build();
        result.getAudience().stream()
                .flatMap(uuid -> Optional.ofNullable(Bukkit.getPlayer(uuid)).stream())
                .forEach(player -> player.sendMessage(built));
    }

    @MessageClient.MessageListener
    public void onServerToChannelChatMessage(String channel, ServerToChannelChatTypeMessage serverToChannel) {
        FusionPaper.getChatPipeline().getChannelManager().getChannel(serverToChannel.getTargetChannel()).ifPresent(chatChannel -> {
            ServerToChannelPostChatType chatType = new ServerToChannelPostChatType(
                    serverToChannel.getMessage(),
                    chatChannel
            );
            ChatType result = postProcessorManager.intake(ServerToChannelPostChatType.class, chatType);
            Component built = result.getMessage().build();
            result.getAudience().stream()
                    .flatMap(uuid -> Optional.ofNullable(Bukkit.getPlayer(uuid)).stream())
                    .forEach(player -> player.sendMessage(built));
        });
    }

    @MessageClient.MessageListener
    public void onServerToPlayerChatMessage(String channel, ServerToPlayerChatTypeMessage serverToPlayer) {
        ServerToPlayerPostChatType chatType = new ServerToPlayerPostChatType(serverToPlayer.getMessage(), serverToPlayer.getTarget());
        ChatType result = postProcessorManager.intake(ServerToPlayerPostChatType.class, chatType);
        Component built = result.getMessage().build();
        result.getAudience().stream()
                .flatMap(uuid -> Optional.ofNullable(Bukkit.getPlayer(uuid)).stream())
                .forEach(player -> player.sendMessage(built));
    }
}
