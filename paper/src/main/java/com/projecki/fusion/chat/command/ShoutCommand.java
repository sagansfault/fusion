package com.projecki.fusion.chat.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.chat.ChatConfig;
import com.projecki.fusion.chat.chattype.preprocessor.impl.NetworkBroadcastPreChatType;
import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.chat.pipeline.ChatPipeline;
import com.projecki.fusion.chat.util.Placeholder;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommandAlias("shout")
@CommandPermission("fusion.chat.shout")
public class ShoutCommand extends FusionChatBaseCommand {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public ShoutCommand(ChatPipeline chatPipeline) {
        super(chatPipeline);
    }

    @Default
    @Syntax("<message>")
    @Description("Send a message to the network")
    public void onShout(Player player, String messageText) {
        ChatPipeline chatPipeline = FusionPaper.getChatPipeline();
        ChatConfig config = chatPipeline.getConfig();

        if (!player.hasPermission("fusion.chat.shout.bypass")) {
            UUID uuid = player.getUniqueId();
            long expiryTime = cooldowns.getOrDefault(uuid, 0L);
            if (expiryTime > System.currentTimeMillis()) {
                return;
            }
            cooldowns.put(uuid, System.currentTimeMillis() + config.shoutCooldown() * 1000L);
        }

        Message message = new Message(config.shoutFormat());

        message.setReplacement(Placeholder.MESSAGE.getPlaceholder(), messageText);
        message.setReplacement(Placeholder.PLAYER_SENDER.getPlaceholder(), player.getName());
        FusionPaper.getServerInfo().ifPresent(serverInfo -> {
            message.setReplacement(Placeholder.SENDER_ORIGIN_SERVER.getPlaceholder(), serverInfo.getServerName());
        });

        chatPipeline.intake(
                NetworkBroadcastPreChatType.class,
                new NetworkBroadcastPreChatType(message, config.shoutBlacklistedServers().toArray(String[]::new))
        );
    }
}
