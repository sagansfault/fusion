package com.projecki.fusion.party;

import com.projecki.fusion.party.member.Member;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @since May 16, 2022
 * @author Andavin
 */
public record MultiMemberParty(UUID id, List<Member> members) implements Party, ForwardingAudience {

    public MultiMemberParty {
        int size = members.size();
        checkArgument(size > 1, "invalid member size: %s", size);
        members = List.copyOf(members);
    }

    @Override
    public int size() {
        return members.size();
    }

    @Override
    public Member leader() {
        return members.get(0);
    }

    @Override
    public Party leave(UUID memberId) {
        List<Member> members = this.members().stream()
                .filter(m -> !memberId.equals(m.id()))
                .toList();
        return members.size() == 1 ?
                new SingleMemberParty(members.get(0)) :
                new MultiMemberParty(this.id(), members);
    }

    @Override
    public @NotNull Iterable<? extends Audience> audiences() {
        return members;
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof Party p && p.id().equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
