package com.projecki.fusion.party.member;

import net.kyori.adventure.audience.ForwardingAudience;

import java.util.UUID;

/**
 * @author Andavin
 * @since April 21, 2022
 */
public interface Member extends ForwardingAudience.Single {

    /**
     * The {@link UUID} of this member.
     *
     * @return The ID.
     */
    UUID id();

    /**
     * The username of this member.
     *
     * @return The username.
     */
    String name();

//    /**
//     * The identifier for the server where this member was
//     * last known to be online.
//     *
//     * @return The last known server identifier.
//     */
//    String lastKnownServer();

//    /**
//     * Determine whether this member is currently online
//     * somewhere on a server.
//     *
//     * @return If this member is online.
//     */
//    boolean isOnline();

    /**
     * Send this member to the server with the specified identifier.
     *
     * @param serverId The ID of the server to send the member to.
     */
    void send(String serverId);
}
