package com.projecki.fusion.party.message;

import com.projecki.fusion.FusionCore;
import com.projecki.fusion.message.MessageClient.HandledMessage;
import com.projecki.fusion.party.Parties;
import com.projecki.fusion.party.Party;

import java.util.UUID;

public record JoinPartyMessage(UUID partyId, UUID memberId, String memberName) implements HandledMessage {

    @Override
    public void handle(String channel) {
        Parties<?> parties = FusionCore.getParties();
        parties.getPartyById(partyId).ifPresent(currentParty -> {
            Party memberParty = parties.getPartyByMemberId(memberId, memberName);
            parties.update(memberParty, currentParty.join(memberParty));
        });
    }
}
