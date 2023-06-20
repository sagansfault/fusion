package com.projecki.fusion.wrapper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.WorldType;

public class WrapperPlayServerRespawn extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.RESPAWN;

    public WrapperPlayServerRespawn() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperPlayServerRespawn(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Retrieve Dimension.
     * <p>
     * Notes: -1: The Nether, 0: The Overworld, 1: The End
     *
     * @return The current Dimension
     */
    public int getDimension() {
        return handle.getDimensions().optionRead(0).orElse(0);
    }

    /**
     * Set Dimension.
     *
     * @param value - new value.
     */
    public void setDimension(int value) {
        handle.getDimensions().write(0, value);
    }

    /**
     * Retrieve Gamemode.
     * <p>
     * Notes: 0: survival, 1: creative, 2: adventure. The hardcore flag is not
     * included
     *
     * @return The current Gamemode
     */
    public EnumWrappers.NativeGameMode getGamemode() {
        return handle.getGameModes().read(0);
    }

    /**
     * Set Gamemode.
     *
     * @param value - new value.
     */
    public void setGamemode(EnumWrappers.NativeGameMode value) {
        handle.getGameModes().write(0, value);
    }

    /**
     * Retrieve Level Type.
     * <p>
     * Notes: same as Join Game
     *
     * @return The current Level Type
     */
    public WorldType getLevelType() {
        return handle.getWorldTypeModifier().read(0);
    }

    /**
     * Set Level Type.
     *
     * @param value - new value.
     */
    public void setLevelType(WorldType value) {
        handle.getWorldTypeModifier().write(0, value);
    }

}
