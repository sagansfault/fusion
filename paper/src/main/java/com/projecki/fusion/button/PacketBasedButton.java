package com.projecki.fusion.button;

import com.projecki.fusion.customfloatingtexture.PacketBasedCustomFloatingTexture;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collections;

public class PacketBasedButton extends AbstractButton {

    public PacketBasedButton(JavaPlugin plugin,
                             PacketBasedCustomFloatingTexture floatingTexture,
                             float textureWidth,
                             float textureHeight,
                             double range) {
        super(plugin, floatingTexture, textureWidth, textureHeight, range);
    }

    /**
     * As this is a packet-based implementation, not all players will be able to see this by default, show it to them
     * using this method.
     *
     * @param players The players to show this to
     */
    public void show(Collection<? extends Player> players) {
        ((PacketBasedCustomFloatingTexture) floatingTexture).show(players);
    }

    /**
     * As this is a packet-based implementation, not all players will be able to see this by default, show it to them
     * using this method.
     *
     * @param player The player to show this to
     */
    public void show(Player player) {
        this.show(Collections.singleton(player));
    }
}
