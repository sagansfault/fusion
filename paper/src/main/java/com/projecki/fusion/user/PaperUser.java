package com.projecki.fusion.user;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.projecki.fusion.object.DependsOnAll;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @since May 27, 2022
 * @author Andavin
 */
@DependsOnAll(PaperUserModule.class)
public class PaperUser extends User<Player> {

    private static final String DEFAULT_NAME = "unknown";
    private static final UUID DEFAULT_UUID = new UUID(0, 0);

    private UUID uuid = DEFAULT_UUID;
    private String name = DEFAULT_NAME;

    public PaperUser() {
    }

    PaperUser(PlayerProfile profile) {
        this.uuid = profile.getId();
        this.name = profile.getName();
    }

    @Override
    public String name() {
        return name;
    }

    /**
     * Get the {@link UUID} of the {@link #reference()}.
     *
     * @return The {@link UUID}.
     */
    public UUID uuid() {
        return uuid;
    }

    @Override
    protected void reference(@NotNull Player reference) {
        this.name = reference.getName();
        this.uuid = reference.getUniqueId();
        super.reference(reference);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return o == this || o instanceof PaperUser u && u.uuid.equals(uuid);
    }

    /**
     * Determine whether the specified {@link Player} is
     * equal to the {@link #reference()} of this user.
     *
     * @param player The {@link Player} to compare with.
     * @return If the reference is equal to the {@link Player}.
     */
    public boolean equals(@NotNull Player player) {
        return player.equals(this.reference());
    }
}
