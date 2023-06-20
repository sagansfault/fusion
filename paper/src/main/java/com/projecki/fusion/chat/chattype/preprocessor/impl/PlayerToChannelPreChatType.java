package com.projecki.fusion.chat.chattype.preprocessor.impl;

import com.projecki.fusion.chat.channel.ChatChannel;
import com.projecki.fusion.chat.chattype.preprocessor.PreProcessorChatType;
import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.redis.pubsub.message.impl.chat.ChatTypeMessage;
import com.projecki.fusion.redis.pubsub.message.impl.chat.PlayerToChannelChatTypeMessage;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerToChannelPreChatType extends PreProcessorChatType {

    private ChatChannel targetChannel;
    private String originServer;
    private Player player;

    public PlayerToChannelPreChatType(Message message, ChatChannel targetChannel, String originServer, Player player) {
        super(message);
        this.targetChannel = targetChannel;
        this.originServer = originServer;
        this.player = player;
    }

    public ChatChannel getTargetChannel() {
        return targetChannel;
    }

    public void setTargetChannel(ChatChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

    public String getOriginServer() {
        return originServer;
    }

    public void setOriginServer(String originServer) {
        this.originServer = originServer;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public ChatTypeMessage mapToChatTypeMessage() {
        return new PlayerToChannelChatTypeMessage(super.getMessage(), super.getAudience(), originServer,
                targetChannel.getId(), player.getUniqueId(), player.getName());
    }
}
