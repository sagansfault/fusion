package com.projecki.fusion.party.message;

import com.projecki.fusion.FusionCore;
import com.projecki.fusion.message.MessageClient.HandledMessage;
import com.projecki.fusion.party.Parties;

import java.util.UUID;

public record LeavePartyMessage(UUID partyId, UUID memberId) implements HandledMessage {

    @Override
    public void handle(String channel) {
        Parties<?> parties = FusionCore.getParties();
        parties.getPartyById(partyId).ifPresent(currentParty ->
                parties.getPartyByMemberId(memberId).ifPresent(
                        memberParty -> parties.update(currentParty, currentParty.leave(memberId))));
    }
}
