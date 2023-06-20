package com.projecki.fusion.party;

import com.projecki.fusion.party.member.Member;
import net.kyori.adventure.audience.Audience;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Andavin
 * @since April 21, 2022
 */
public interface Party extends Audience {

    /**
     * The {@link UUID} for this party.
     *
     * @return The ID for the party.
     */
    UUID id();

    /**
     * The amount of {@link #members()} that belong to this party.
     *
     * @return The size.
     */
    int size();

    /**
     * The leading {@link Member} of this party.
     *
     * @return The party leader.
     */
    Member leader();

    /**
     * All the {@link Member members} that belong to this party.
     *
     * @return The players.
     */
    List<Member> members();

    /**
     * Join this party with another creating a new party
     * that contains all the {@link Member members} of each.
     * <p>
     *     The resulting party will always contains the {@link Member members}
     *     of this party first followed by those of the specified party.
     * </p>
     * <p>
     *     The {@link Party#leader() leader} of this party will also
     *     be the leader of the new resulting party.
     * </p>
     *
     * @param other The party to join with.
     * @return The new party containing all {@link Member members}.
     */
    default Party join(Party other) {
        List<Member> members = new ArrayList<>(this.size() + other.size());
        members.addAll(this.members());
        members.addAll(other.members());
        return new MultiMemberParty(this.id(), members);
    }

    /**
     * Remove the {@link Member} with the specified identifier
     * and create a new party without the {@link Member}.
     *
     * @param memberId The {@link Member#id() ID} of the
     *                 {@link Member} to remove.
     * @return The new party with the {@link Member} removed.
     */
    Party leave(UUID memberId);

    /**
     * Send this party to the server with the specified identifier.
     *
     * @param serverId The ID of the server to send the party to.
     */
    default void send(String serverId) {
        this.members().forEach(member -> member.send(serverId));
    }
}
