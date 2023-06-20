package com.projecki.fusion.party;

import com.projecki.fusion.party.member.Member;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @since May 01, 2022
 * @author Andavin
 */
public final class SingleMemberParty implements Party, ForwardingAudience.Single {

    private final Member member;
    private final List<Member> members;

    public SingleMemberParty(Member member) {
        this.member = member;
        this.members = List.of(member);
    }

    @Override
    public UUID id() {
        return member.id();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Member leader() {
        return member;
    }

    @Override
    public List<Member> members() {
        return members;
    }

    @Override
    public Party join(Party other) {

        if (other instanceof SingleMemberParty single) {
            return new MultiMemberParty(this.id(), List.of(member, single.member));
        }

        List<Member> members = new ArrayList<>(other.size() + 1);
        members.add(member);
        members.addAll(other.members());
        return new MultiMemberParty(this.id(), members);
    }

    @Override
    public Party leave(UUID memberId) {
        throw new UnsupportedOperationException("cannot leave a single member party");
    }

    @Override
    public @NotNull Audience audience() {
        return member;
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof Party p && p.id().equals(this.id());
    }

    @Override
    public int hashCode() {
        return this.id().hashCode();
    }
}
