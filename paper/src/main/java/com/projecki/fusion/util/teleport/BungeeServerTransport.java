package com.projecki.fusion.util.teleport;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Sends players across servers using a BungeeCord plugin channel
 * This is originally for BungeeCord, but Velocity supports it as well
 * to have compatibility for Spigot plugins made for Bungee
 */
public class BungeeServerTransport implements CrossServerTransport {

    private static final String CHANNEL = "BungeeCord";
    private final JavaPlugin plugin;

    /**
     * Create a new BungeeServerTransport.
     * A valid plugin is required to register the plugin channel with Bukkit.
     *
     * @param plugin plugin to register plugin channel to
     */
    public BungeeServerTransport(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
    }

    /**
     * Sends the specified player to the server with the same serverName on the proxy.
     * This methods uses BungeeCord plugin channels and makes no guarantee that
     * the server specified exists. If the server doesn't exist the future will still complete
     * successfully and the player will just be sent to a fallback server.
     *
     * @param player     player to send
     * @param serverName server to send the player to
     * @return a future that completes once the request is sent to the proxy
     */
    @Override
    public CompletableFuture<Void> transport(Player player, String serverName) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteStream);

        try {
            outputStream.writeUTF("Connect");
            outputStream.writeUTF(serverName);
        } catch (IOException e) {
            e.printStackTrace();
            future.completeExceptionally(e);
        }

        // this is going to be used with velocity, but velocity preserves this bungee channel
        player.sendPluginMessage(plugin, CHANNEL, byteStream.toByteArray());
        future.complete(null);

        return future;
    }
}
