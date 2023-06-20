package com.projecki.fusion;

import co.aikar.commands.VelocityCommandManager;
import com.google.inject.Inject;
import com.projecki.fusion.command.*;
import com.projecki.fusion.config.MotdContainer;
import com.projecki.fusion.config.VelocityHermesConfig;
import com.projecki.fusion.config.impl.MotdConfig;
import com.projecki.fusion.config.impl.VelocityConfig;
import com.projecki.fusion.config.local.plugin.LocalYamlPluginConfig;
import com.projecki.fusion.config.serialize.JacksonSerializer;
import com.projecki.fusion.listener.JoinLeaveListener;
import com.projecki.fusion.listener.JoinListener;
import com.projecki.fusion.listener.PingListener;
import com.projecki.fusion.listener.PlayerListener;
import com.projecki.fusion.message.MessageClient;
import com.projecki.fusion.message.redis.RedisMessageClient;
import com.projecki.fusion.monitor.PlayerHeartbeats;
import com.projecki.fusion.monitor.PlayerMonitor;
import com.projecki.fusion.network.NameCacheListener;
import com.projecki.fusion.network.ServerRegistry;
import com.projecki.fusion.network.SkinCacheListener;
import com.projecki.fusion.network.redis.RedisPlayerStorage;
import com.projecki.fusion.party.Parties;
import com.projecki.fusion.party.VelocityParties;
import com.projecki.fusion.redis.CommonRedisChannels;
import com.projecki.fusion.redis.CommonRedisKeys;
import com.projecki.fusion.server.RedisServerDataStorage;
import com.projecki.fusion.server.ServerDataStorage;
import com.projecki.fusion.transport.NetworkTransportListener;
import com.projecki.fusion.transport.SeamlessConnectListener;
import com.projecki.fusion.util.NameResolver;
import com.projecki.fusion.util.SkinResolver;
import com.projecki.fusion.voting.VoteEndpointStorage;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Plugin(
        id = "fusion-velocity",
        name = "FusionVelocity",
        version = "1.0.0",
        description = "A core and utility for velocity plugins",
        authors = {"sagan", "teamplayer"},
        dependencies = {
                @Dependency(id = "protocolize", optional = true),
                @Dependency(id = "forceresourcepacks", optional = true)
        }
)
public class FusionVelocity {

    // shared redis URI between all velocity plugins
    private static RedisClient redisClient;
    private static RedisAsyncCommands<String, String> redisCommands;
    private static RedisPubSubAsyncCommands<String, String> redisPubSubCommands;
    private static MessageClient messageClient;

    // proxy
    private final ProxyServer proxyServer;
    private final Logger logger;

    // data
    private final Path dataDir;
    private final LocalYamlPluginConfig<VelocityConfig> configLoader;
    private VelocityConfig config;
    private static ServerDataStorage serverDataStorage;

    // party manager
    private static Parties<Player> parties;

    // player data cache
    private static NameResolver nameResolver;
    private static SkinResolver skinResolver;
    private static ExecutorService httpExecutorService;

    // motd
    private final MotdContainer motdContainer = new MotdContainer();

    private static String organization;

    @Inject
    public FusionVelocity(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDir) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDir = dataDir;

        // loading is done in constructor
        configLoader = new LocalYamlPluginConfig<>(VelocityConfig.class, this, dataDir, "config.yml");
        configLoader.loadConfig().thenAccept(o -> {
            if (o.isPresent()) {
                config = o.get();
                RedisURI redisURI = RedisURI.builder()
                        .withHost(config.getRedisAddress())
                        .withPort(Integer.parseInt(config.getRedisPort()))
                        .withPassword(config.getRedisPassword())
                        .withTimeout(Duration.ofSeconds(10)).build();

                redisClient = RedisClient.create(redisURI);
                redisCommands = redisClient.connect().async();
                redisPubSubCommands = redisClient.connectPubSub().async();
                messageClient = new RedisMessageClient(redisCommands, redisPubSubCommands);

                messageClient.subscribe(CommonRedisChannels.SERVER_LOOKUP_CHANNEL.getChannel());

                organization = config.getOrganization();
            } else {
                getLogger().log(Level.SEVERE, "Config not available");
                getProxyServer().shutdown();
            }
        });

        // motd config loading
        var motdLoader = new VelocityHermesConfig<>(JacksonSerializer.ofYaml(MotdConfig.class),
                "FusionVelocity", "network");
        motdLoader.loadConfig().thenAccept(opt -> opt.ifPresent(this::updateMotd));
        motdLoader.onUpdate(opt -> {
            logger.info("Hermes updated motd!");

            if (opt.isPresent()) {
                logger.info(String.join("\n", opt.get().getMotd()));
            } else {
                logger.info("Optional is empty :(");
            }


            opt.ifPresent(this::updateMotd);
        });
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        // data
        this.registerMessageListeners();
        serverDataStorage = new RedisServerDataStorage(redisClient);

        // init all known servers to a redis for name/ip lookups
        storeServers();

        // server registry
        var serverRegistry = new ServerRegistry(proxyServer, logger, redisCommands, messageClient, this);

        // name resolving
        httpExecutorService = new ThreadPoolExecutor(1, 3, 30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());

        var nameStorage = new NameResolver.RedisNameResolverStorage(redisCommands);
        nameResolver = new NameResolver(httpExecutorService, nameStorage);
        proxyServer.getEventManager().register(this, new NameCacheListener(nameStorage));

        // player storage
        var playerStorage = new RedisPlayerStorage(redisCommands);

        // commands
        var commandManager = new VelocityCommandManager(proxyServer, this);
        commandManager.registerCommand(new ServerManageCommand(commandManager, proxyServer, serverRegistry, this));
        commandManager.registerCommand(new RestartingCommand(commandManager, this, proxyServer));
        commandManager.registerCommand(new FindCommand(commandManager, proxyServer, playerStorage, nameResolver));
        commandManager.registerCommand(new JoinCommand(commandManager, proxyServer, playerStorage, nameResolver));

        // we only need to register this command if ForceResourcePacks is on the proxy
        if (proxyServer.getPluginManager().isLoaded("forceresourcepacks")) {
            commandManager.registerCommand(new PackReloadCommand(commandManager, proxyServer, messageClient));
        }

        // skin caching
        var skinStorage = new SkinResolver.RedisSkinStorage(CommonRedisKeys.PLAYER_CACHE.getKey(), redisCommands);
        skinResolver = new SkinResolver(skinStorage);
        proxyServer.getEventManager().register(this, new SkinCacheListener(skinStorage));

        // parties
        parties = new VelocityParties(proxyServer);
        FusionCore.setParties(FusionVelocity::getParties);

        // listeners
        new NetworkTransportListener(messageClient, proxyServer);
        proxyServer.getEventManager().register(this, new PlayerListener(playerStorage, config.getProxyId()));
        proxyServer.getEventManager().register(this, new PingListener(playerStorage, motdContainer));
        proxyServer.getEventManager().register(this, new JoinListener(playerStorage));
        proxyServer.getEventManager().register(this, new JoinLeaveListener(messageClient));

        if (proxyServer.getPluginManager().getPlugin("protocolize").isPresent()) {
            proxyServer.getEventManager().register(this, new SeamlessConnectListener(proxyServer));
        }

        // player tracking
        new PlayerMonitor(this, proxyServer, playerStorage);
        new PlayerHeartbeats(this, proxyServer, playerStorage);

        // voting
        new VoteEndpointStorage(redisCommands, Logger.getGlobal(), config.getProxyId(), config.getVotifierPort());
    }

    /**
     * Store all servers in redis for ProCommonPaper to do lookups
     * for server names.
     */
    public void storeServers() {
        if (!proxyServer.getAllServers().isEmpty()) {
            Map<String, String> addressNameMap = proxyServer.getAllServers()
                    .stream()
                    .collect(Collectors.toMap(
                            regServer -> {
                                InetSocketAddress inetSocketAddress = regServer.getServerInfo().getAddress();
                                return inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
                            },
                            regServer -> regServer.getServerInfo().getName()
                    ));
            redisCommands.hset(CommonRedisKeys.SERVER_LOOKUP.getKey(), addressNameMap);
        }
    }

    private void updateMotd(MotdConfig config) {
        motdContainer.setMotd(config.getMotd());
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDir() {
        return dataDir;
    }

    /**
     * Get the {@link Parties} for paper.
     *
     * @return The {@link Parties}.
     */
    public static Parties<Player> getParties() {
        return parties;
    }

    /**
     * Get the internal {@link RedisClient} used be ProCommon that is
     * to be shared will all usages that need to use {@link RedisClient}
     *
     * @return the shared {@link RedisClient}
     */
    public static RedisClient getLettuceClient() {
        return redisClient;
    }

    /**
     * Get the shared redis commands that are used on the connection that
     * is shared between all projects that rely on ProCommon.
     * <p>
     * A single redis connection can support upwards of 10k requests.
     * The only reason to create another redis connection is if blocking
     * operations are used or transactions need to be performed.
     *
     * @return the shared {@link RedisAsyncCommands}
     */
    public static RedisAsyncCommands<String, String> getRedisCommands() {
        return redisCommands;
    }

    /**
     * Get the shared redis pubsub commands that are used on the connection
     * that is shared between all projects that rely on ProCommon.
     *
     * @return the shared {@link RedisPubSubAsyncCommands}
     */
    public static RedisPubSubAsyncCommands<String, String> getRedisPubSubCommands() {
        return redisPubSubCommands;
    }

    /**
     * Get the shared {@link MessageClient} that is used by all ProCommon
     * projects to communicate messages within a network.
     *
     * @return the share {@link MessageClient}
     */
    public static MessageClient getMessageClient() {
        return messageClient;
    }

    public static NameResolver getNameResolver() {
        return nameResolver;
    }

    public static SkinResolver getSkinResolver() {
        return skinResolver;
    }

    private void registerMessageListeners() {
    }

    public static ServerDataStorage getServerDataStorage() {
        return serverDataStorage;
    }

    public static String getOrganization() {
        return organization;
    }
}
