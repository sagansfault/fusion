package com.projecki.fusion.control;

import com.projecki.fusion.message.MessageClient;

public record ControlMessage(ControlAction action) implements MessageClient.Message {

    /**
     * Get the {@link MessageClient} channel that is used
     * specifically to control the specified server
     *
     * @param serverName name of server to get channel for
     * @return channel for server
     */
    static String getServerChannel(String serverName) {
        return "server:control:" + serverName;
    }

}
