package com.projecki.fusion.party;

import com.projecki.fusion.party.member.Member;
import com.projecki.fusion.party.member.VelocityMember;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since June 30, 2022
 * @author Andavin
 */
public class VelocityParties implements Parties<Player> {

    private final ProxyServer server;
    private final Map<UUID, Party> partyIds = new ConcurrentHashMap<>();
    private final Map<UUID, Party> memberIds = new ConcurrentHashMap<>();

    public VelocityParties(ProxyServer server) {
        this.server = server;
    }

    @Override
    public @NotNull Party getParty(Player player) {
        return memberIds.computeIfAbsent(player.getUniqueId(),
                __ -> this.createParty(new VelocityMember(server, player)));
    }

    @Override
    public Optional<Party> getPartyById(UUID partyId) {
        return Optional.ofNullable(partyIds.get(partyId));
    }

    @Override
    public Optional<Party> getPartyByMemberId(UUID memberId) {
        return Optional.ofNullable(memberIds.get(memberId));
    }

    @Override
    public Party getPartyByMemberId(UUID memberId, String memberName) {
        return memberIds.computeIfAbsent(memberId, __ ->
                this.createParty(new VelocityMember(server, memberId, memberName)));
    }

    @Override
    public void update(Party previous, Party updated) {

        this.partyIds.remove(previous.id());
        for (Member member : previous.members()) {
            this.memberIds.remove(member.id());
        }

        this.partyIds.put(updated.id(), updated);
        for (Member member : updated.members()) {

            Party memberParty = memberIds.put(member.id(), updated);
            if (memberParty != null && !memberParty.equals(updated)) {
                this.partyIds.remove(memberParty.id());
            }
        }
    }

    private Party createParty(Member member) {
        Party party = new SingleMemberParty(member);
        this.partyIds.put(party.id(), party);
        this.memberIds.put(member.id(), party);
        return party;
    }
}
