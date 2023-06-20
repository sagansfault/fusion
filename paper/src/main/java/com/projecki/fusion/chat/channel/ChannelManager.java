package com.projecki.fusion.chat.channel;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.chat.ChatConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChannelManager {

    private final FusionPaper fusion;

    private ChatChannel defaultChannel = null;
    private final Set<ChatChannel> channels;
    private final Map<UUID, ChatChannel> enteredChannels;

    public ChannelManager(FusionPaper fusion, ChatConfig config) {
        this.fusion = fusion;
        this.channels = new HashSet<>();
        this.enteredChannels = new HashMap<>();
        this.reloadChannelData(config);

        fusion.getServer().getPluginManager().registerEvents(new DefaultChannelInitializer(this), fusion);
    }

    public void reloadChannelData(ChatConfig config) {
        for (ChatConfig.ChatChannelData datum : config.chatChannelData()) {
            if (datum.id() == null || datum.defaultFormat() == null) {
                continue;
            }
            ChatChannel channel = new ChatChannel(datum.id(), datum.defaultFormat());
            if (datum.customFormats() != null) {
                datum.customFormats().forEach(channel::setCustomFormat);
            }
            if (datum.targetGroup() != null) {
                channel.setTargetGroup(Arrays.stream(datum.targetGroup()).collect(Collectors.toSet()));
            }
            this.putChannel(channel);
        }

        String defaultChannel = config.defaultChannel();
        ChatChannel found = null;
        for (ChatChannel channel : this.channels) {
            if (channel.getId().equalsIgnoreCase(defaultChannel)) {
                found = channel;
                break;
            }
        }

        if (found == null) {
            found = new ChatChannel("$", "%player_sender%: %message%");
            this.putChannel(found);
        }

        this.defaultChannel = found;
    }

    public Set<ChatChannel> getChannels() {
        return new HashSet<>(channels);
    }

    public boolean addChannel(ChatChannel channel) {
        return this.channels.add(channel);
    }

    public boolean putChannel(ChatChannel channel) {
        boolean removed = this.channels.remove(channel);
        this.channels.add(channel);
        return removed;
    }

    public Optional<ChatChannel> removeChannel(String id) {
        for (ChatChannel channel : this.getChannels()) {
            if (channel.getId().equalsIgnoreCase(id)) {
                this.channels.remove(channel);
                return Optional.of(channel);
            }
        }
        return Optional.empty();
    }

    public Optional<ChatChannel> getChannel(UUID player) {
        return Optional.ofNullable(this.enteredChannels.get(player));
    }

    public Optional<ChatChannel> getChannel(String id) {
        for (ChatChannel channel : this.channels) {
            if (channel.getId().equalsIgnoreCase(id)) {
                return Optional.of(channel);
            }
        }
        return Optional.empty();
    }

    public ChatChannel getDefaultChannel() {
        return this.defaultChannel;
    }

    public Optional<ChatChannel> putInChannel(UUID player, String id) {
        Optional<ChatChannel> channelOptional = this.getChannel(id);
        channelOptional.ifPresent(chatChannel -> this.enteredChannels.put(player, chatChannel));
        return channelOptional;
    }

    public void putInChannel(UUID player, ChatChannel channel) {
        this.enteredChannels.put(player, channel);
    }

    private static final class DefaultChannelInitializer implements Listener {

        private final ChannelManager channelManager;

        public DefaultChannelInitializer(ChannelManager channelManager) {
            this.channelManager = channelManager;
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            channelManager.putInChannel(event.getPlayer().getUniqueId(), channelManager.getDefaultChannel());
        }
    }
}
