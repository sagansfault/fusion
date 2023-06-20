package com.projecki.fusion.network;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.projecki.fusion.FusionVelocity;
import com.projecki.fusion.message.MessageClient;
import com.projecki.fusion.redis.pubsub.message.impl.NetworkMessages;
import com.projecki.fusion.util.AddressUtil;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A global register for all servers that should be in
 * each proxy's local list.
 *
 * {@link ServerRegistry} manages this list and broadcasts and listens
 * for changes so that each proxy is always up-to-date.
 */
public class ServerRegistry {

    private static final String SERVERS_KEY = "server-registry";
    private static final String CHANNEL = "server-registry-update";

    private final Gson gson = new Gson();

    private final ProxyServer proxy;
    private final Logger logger;
    private final RedisAsyncCommands<String, String> commands;
    private final MessageClient messagingClient;
    private final FusionVelocity plugin;

    public ServerRegistry(ProxyServer proxy, Logger logger, RedisAsyncCommands<String, String> commands,
                          MessageClient messagingClient, FusionVelocity plugin) {
        this.proxy = proxy;
        this.commands = commands;
        this.logger = logger;
        this.messagingClient = messagingClient;
        this.plugin = plugin;

        pushLocalServers()
                .thenRun(this::updateServers)
                .thenRun(() -> {
                    messagingClient.registerMessageListener(NetworkMessages.ServerRegistryUpdateMessage.class, (t, u) -> this.updateServers());
                    messagingClient.subscribe(CHANNEL);
                });
    }

    /**
     * Add a set of servers to the global server registry.
     * <p>
     * This updates the server lists for all proxies on the network.
     *
     * @param servers servers to add to the registry by {@link ServerInfo}
     */
    public void addServers(Set<ServerInfo> servers) {
        commands.hset(SERVERS_KEY, createNameInfoMap(servers))
                .thenRun(this::broadcastUpdate);

        var messageBuilder = new StringBuilder("Adding servers to server list: [ ");

        for (ServerInfo info : servers) {
            messageBuilder.append(info.serverName).append(" ")
                    .append(info.host).append(':').append(info.port)
                    .append(", ");
        }

        messageBuilder.append(']');

        logger.info(messageBuilder.toString());
    }

    /**
     * Remove server from the global registry by server name.
     * <p>
     * This updates the server lists for all proxies on the network.
     *
     * @param servers names of servers to remove from the registry
     */
    public void removeServers(String... servers) {
        commands.hdel(SERVERS_KEY, servers)
                .thenRun(this::broadcastUpdate);

        logger.info("Removing servers from server list [ " +
                String.join(", ", servers) + ']');
    }

    /**
     * Get a set of the {@link ServerInfo}s for all the servers that
     * are currently in the server registry.
     *
     * @return a set of all registered servers
     */
    public CompletableFuture<Set<ServerInfo>> getRegisteredServers() {
        return commands.hvals(SERVERS_KEY)
                .thenApply(set -> set.stream()
                        .map(val -> gson.fromJson(val, ServerInfo.class))
                        .collect(Collectors.toSet()))
                .toCompletableFuture();
    }

    /**
     * Push local servers that don't exist in the registry to the registry
     */
    private CompletionStage<Void> pushLocalServers() {
        return getRegisteredServers()
                .thenAccept(remote -> {
                    var difference = Sets.difference(getLocalServers(), remote);

                    // if there are more servers locally, we need to add them to remote
                    if (!difference.isEmpty()) addServers(difference.immutableCopy());
                });
    }

    /**
     * Broadcast updates to all proxies to update their server list from
     * global server registry
     */
    private void broadcastUpdate() {
        messagingClient.send(CHANNEL, new NetworkMessages.ServerRegistryUpdateMessage());
    }

    /**
     * Locally update the server list to match the global server registry
     */
    private void updateServers() {

        logger.info("Updating server list from server registry...");

        getRegisteredServers()
                .thenAccept(remote -> {
                    var local = getLocalServers();

                    // these servers don't exist remotely, but do locally
                    Sets.difference(local, remote)
                            .forEach(this::removeServer);

                    // these servers don't exist locally, but do remotely
                    Sets.difference(remote, local)
                            .forEach(this::addUpdateServer);
                })
                .thenRun(plugin::storeServers)
                .whenComplete((val, exe) -> {
                    if (exe != null) {
                        logger.warning("Exeception while updating server list.");
                        exe.printStackTrace();
                    } else {
                        logger.info("Server list sucessfully updated from registry");
                    }
                });
    }

    /**
     * Get a list of all the servers locally on the proxy
     * as {@link ServerInfo}s
     */
    private Set<ServerInfo> getLocalServers() {
        return proxy.getAllServers().stream()
                .map(RegisteredServer::getServerInfo)
                .map(ServerInfo::fromServerInfo)
                .collect(Collectors.toSet());
    }

    /**
     * Adds a server to proxy, or updates it if that server's details have changed
     */
    private void addUpdateServer(ServerInfo info) {
        // there's no way to edit servers, so we must remove the server and re-add it
        removeServer(info);
        proxy.registerServer(info.toServerInfo());
        logger.info(info.toServerInfo().toString());
    }

    /**
     * Remove a server from proxy
     */
    private void removeServer(ServerInfo info) {
        proxy.getServer(info.serverName)
                .ifPresent(ser -> proxy.unregisterServer(ser.getServerInfo()));
    }

    /**
     * Create a map to be stored in redis that is the server name mapped to their
     * host and port combo ({@code 127.0.0.1:25565}
     *
     * @param serverSet servers to be in name host map
     * @return map containing the name of the server mapped to it's host port combo
     */
    private Map<String, String> createNameInfoMap(Set<ServerInfo> serverSet) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        for (ServerInfo serverInfo : serverSet) {
            builder.put(serverInfo.serverName.toLowerCase(), gson.toJson(serverInfo));
        }

        return builder.build();
    }

    /**
     * Information for a server and it's host
     */
    public static final class ServerInfo {
        private String serverName;
        private String host;
        private int port;

        public ServerInfo() {
        }

        public ServerInfo(String serverName, String host, int port) {
            this.serverName = serverName;
            this.host = host;
            this.port = port;
        }

        public com.velocitypowered.api.proxy.server.ServerInfo toServerInfo() {
            return new com.velocitypowered.api.proxy.server.ServerInfo(serverName,
                    AddressUtil.parseAddress(host + ':' + port));
        }

        public static ServerInfo fromServerInfo(com.velocitypowered.api.proxy.server.ServerInfo info) {
            var address = info.getAddress();
            return new ServerInfo(info.getName(), address.getHostString(), address.getPort());
        }

        public String serverName() {
            return serverName;
        }

        public String host() {
            return host;
        }

        public int port() {
            return port;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ServerInfo) obj;
            return Objects.equals(this.serverName, that.serverName) &&
                    Objects.equals(this.host, that.host) &&
                    this.port == that.port;
        }

        @Override
        public int hashCode() {
            return Objects.hash(serverName, host, port);
        }

        @Override
        public String toString() {
            return "ServerInfo[" +
                    "serverName=" + serverName + ", " +
                    "host=" + host + ", " +
                    "port=" + port + ']';
        }
    }
}
