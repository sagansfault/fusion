package com.projecki.fusion.chat;

import com.projecki.fusion.chat.message.Message;
import com.projecki.fusion.message.MessageClient;
import com.projecki.fusion.redis.CommonRedisKeys;
import com.projecki.fusion.redis.pubsub.message.impl.chat.NetworkBroadcastChatTypeMessage;
import com.projecki.fusion.redis.pubsub.message.impl.chat.ServerToChannelChatTypeMessage;
import com.projecki.fusion.redis.pubsub.message.impl.chat.ServerToPlayerChatTypeMessage;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class ChatAPI {

    /**
     * Sends a message to the entire network except for a few servers specified
     *
     * @param messageClient The message client to use (redis credentials depend on the network you wish to send to)
     * @param message The message to send
     * @param excludedServers The servers to exclude
     */
    public static void sendNetworkBroadCast(MessageClient messageClient, Component message, String... excludedServers) {
        messageClient.send(
                CommonRedisKeys.SERVER_CHAT_AND_API.getKey(),
                new NetworkBroadcastChatTypeMessage(Message.fromInitialMessage(message), excludedServers)
        );
    }

    /**
     * Sends a message to a specific channel on a network (specified by the message client's redis credentials).
     *
     * @param messageClient The message client to use (redis credentials depend on the network you wish to send to)
     * @param message The message to send
     * @param targetChannelId The id of the channel you want to send this message to
     */
    public static void sendServerToChannelChatType(MessageClient messageClient, Component message, String targetChannelId) {
        messageClient.send(
                CommonRedisKeys.SERVER_CHAT_AND_API.getKey(),
                new ServerToChannelChatTypeMessage(Message.fromInitialMessage(message), targetChannelId)
        );
    }

    /**
     * Sends a message to a specific player on the network (specified by the message client's redis credentials). This
     * will not follow any specific whisper/msg format; the message you pass in will be the message they see.
     *
     * @param messageClient The message client to use (redis credentials depend on the network you wish to send to)
     * @param message The message to send
     * @param targetUUID The UUID of the player you want to send the message to
     */
    public static void sendServerToPlayerChatType(MessageClient messageClient, Component message, UUID targetUUID) {
        messageClient.send(
                CommonRedisKeys.SERVER_CHAT_AND_API.getKey(),
                new ServerToPlayerChatTypeMessage(Message.fromInitialMessage(message), targetUUID)
        );
    }
}
