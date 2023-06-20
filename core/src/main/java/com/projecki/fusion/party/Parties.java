package com.projecki.fusion.party;

import com.projecki.fusion.party.member.Member;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * @since May 01, 2022
 * @author Andavin
 */
public interface Parties<T> {

    /**
     * Get the {@link Party} for a currently online player.
     * <p>
     *     If no {@link Party} exists, then a {@link Party} with
     *     a single {@link Member} will be created for the specified player.
     * </p>
     *
     * @param player The player to get the {@link Party} for.
     * @return The {@link Party} for the player.
     */
    @NotNull
    Party getParty(T player);

    /**
     * Get the {@link Party} using the {@link Party#id()}.
     *
     * @param partyId The {@link UUID} of the {@link Party}.
     * @return The {@link Party} for the ID if one is present.
     */
    Optional<Party> getPartyById(UUID partyId);

    /**
     * Get the {@link Party} using a member's unique ID.
     *
     * @param memberId The {@link UUID} of the member to
     *                 get the {@link Party} for.
     * @return The {@link Party} for the ID if one is present.
     */
    Optional<Party> getPartyByMemberId(UUID memberId);

    /**
     * Get the {@link Party} using a member's unique ID.
     *
     * @param memberId The {@link UUID} of the member to
     *                 get the {@link Party} for.
     * @param memberName The name of the member.
     * @return The {@link Party} for the ID if one is present.
     */
    Party getPartyByMemberId(UUID memberId, String memberName);

    /**
     * Update the {@link Party} in this manager to the
     * specified {@link Party}.
     * <p>
     *     For example, if a member was added or removed from
     *     a {@link Party}, then this method will update all
     *     applicable mappings to the new {@link Party}.
     * </p>
     *
     * @param previous The {@link Party} to update from.
     * @param updated The {@link Party} to update to.
     */
    void update(Party previous, Party updated);
}
