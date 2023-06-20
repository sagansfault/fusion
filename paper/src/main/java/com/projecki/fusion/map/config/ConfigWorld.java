package com.projecki.fusion.map.config;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains all the config signs for a world
 */
public class ConfigWorld {

    /**
     * Map containing all the config signs mapped to their identifier
     */
    private final ImmutableMultimap<String, ConfigSign> configSigns;
    private final World world;

    private ConfigWorld(World world, ImmutableMultimap<String, ConfigSign> configSigns) {
        this.world = world;
        this.configSigns = configSigns;
    }

    /**
     * Get ConfigWorld for a specified world.
     * Removes all config signs from the world as well.
     *
     * @param world world to get signs from
     * @param regionDiameter the diameter (in chunks) of the region to load chunks for
     * @return ConfigWorld for the world
     */
    public static ConfigWorld fromWorld(World world, int regionDiameter) {
        // forces chunks to load, gets all the signs in those chunks and create ConfigSigns from them if they are config signs
        Set<Chunk> forceLoadedChunks = forceLoadChunks(world, regionDiameter);
        var signs = forceLoadedChunks.stream()
                .flatMap(chunk -> Arrays.stream(chunk.getTileEntities()))
                .filter(state -> state instanceof Sign)
                .map(state -> ((Sign) state))
                .filter(state -> state.getLine(0).matches("\\[\\S+]")) // match strings that look like [this]
                .map(BlockState::getBlock)
                .map(ConfigSign::fromBlock)
                .collect(ArrayListMultimap::<String, ConfigSign>create,
                        (builder, sign) -> builder.put(
                                sign.getContents()[0].replaceAll("[\\[\\]]", ""), // remove brackets
                                sign
                        ),
                        ArrayListMultimap::putAll);

        // set all the config signs to air
        signs.values().forEach(sign -> sign.getLocation().getBlock().setType(Material.AIR));
        forceLoadedChunks.forEach(chunk -> chunk.setForceLoaded(false)); // stops force loading of chunks after all data is read

        return new ConfigWorld(world, ImmutableMultimap.copyOf(signs));
    }

    /**
     * Loads the ConfigWorld with a default diameter of 16 chunks
     *
     * @param world to load signs from
     * @return the ConfigWorld
     */
    public static ConfigWorld fromWorld(World world) {
        return fromWorld(world, 16);
    }

    /**
     * Forcefully loads chunks from the spawn point of a specified world within a given radius then returns them in a set.
     *
     * @param world    to load chunks in
     * @param diameter the diameter of chunks to load around the spawn point (chunks loaded=diameter^2)
     * @return a set of the forcefully loaded chunks
     */
    @Unmodifiable
    public static Set<Chunk> forceLoadChunks(World world, int diameter) {
        Set<Chunk> chunks = new HashSet<>();

        Location spawnLocation = world.getSpawnLocation();
        Chunk spawnChunk = spawnLocation.getChunk();
        int spawnX = spawnChunk.getX();
        int spawnZ = spawnChunk.getZ();

        int radius = diameter / 2;
        for (int x = spawnX - radius; x <= spawnX + radius; x++) {
            for (int z = spawnZ - radius; z <= spawnZ + radius; z++) {
                Chunk chunk = world.getChunkAt(x, z);
                if (!chunk.load(false))
                    throw new IllegalStateException(
                            String.format("Failed to load chunk (%s, %s) in world '%s'", x, z, world.getName()));
                chunk.setForceLoaded(true);
                chunks.add(chunk);
            }
        }
        return ImmutableSet.copyOf(chunks);
    }


    /**
     * Gets signs in the world by their identifier.
     *
     * @param identifier The string inside the brackets on the first
     *                   line of the sign
     * @return immutable set containing all the signs with the specified
     * identifier
     */
    public Set<ConfigSign> getSigns(String identifier) {
        return ImmutableSet.copyOf(configSigns.get(identifier));
    }

    /**
     * @return the {@link World} from which the values of this ConfigWorld were retrieved
     */
    public World getWorld() {
        return world;
    }

}
