package com.projecki.fusion.util.teleport;

import com.google.common.io.ByteStreams;
import com.projecki.fusion.constants.PluginMessageNames;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

/**
 * Sends players across the network without sending the
 * Respawn packet. This prevents the sending of a loading screen
 * in-between servers. <b>DO NOT USE</b> This class without
 * server-side specific support for this to occur. If the sending server
 * does not support this type of switching, obnoxious errors will occur for
 * the client.
 */
public class SeamlessServerTransport implements CrossServerTransport {

    private final JavaPlugin plugin;
    private final String channelId;

    /**
     * Create a new SeamlessServerTransport.
     * A valid plugin is required to register the plugin channel with Bukkit.
     *
     * @param plugin plugin to register plugin channel to
     */
    public SeamlessServerTransport(JavaPlugin plugin) {
        this.plugin = plugin;
        channelId = PluginMessageNames.CHANNEL_NAMESPACE + ':' + PluginMessageNames.SEAMLESS_CHANNEL;
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channelId);
    }


    /**
     * Sends the specified player to the server with the same serverName on the proxy.
     * If the server doesn't exist the future will still complete
     * successfully and the player will just not be connected to another server.
     *
     * @param player     player to send to another server
     * @param serverName name of server to send player to
     * @return future that completes once the plugin message is sent to the proxy.
     * this future still completes sucessfully even if there was an error sending a
     * plugin message to the proxy.
     */
    @Override
    public CompletableFuture<Void> transport(Player player, String serverName) {
        var future = new CompletableFuture<Void>();

        @SuppressWarnings("UnstableApiUsage")
        var out = ByteStreams.newDataOutput();
        out.writeUTF(serverName);

        player.sendPluginMessage(plugin, channelId, out.toByteArray());
        future.complete(null);

        return future;
    }
}
