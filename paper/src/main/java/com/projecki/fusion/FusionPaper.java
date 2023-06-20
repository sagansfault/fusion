package com.projecki.fusion;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.projecki.fusion.chat.pipeline.ChatPipeline;
import com.projecki.fusion.command.party.PartyCommand;
import com.projecki.fusion.config.PaperHermesConfig;
import com.projecki.fusion.config.PaperLocalYamlConfig;
import com.projecki.fusion.config.impl.PlatformMessageConfig;
import com.projecki.fusion.config.impl.RedisConfig;
import com.projecki.fusion.config.impl.ServerInfoConfig;
import com.projecki.fusion.control.ControlMessageHandler;
import com.projecki.fusion.control.PaperControlMessageHandler;
import com.projecki.fusion.control.SafeStopHandler;
import com.projecki.fusion.currency.CurrencyRegister;
import com.projecki.fusion.currency.PaperCurrencyPlaceholder;
import com.projecki.fusion.currency.cache.PlayerCurrencyCache;
import com.projecki.fusion.currency.command.CreditsCommand;
import com.projecki.fusion.currency.config.CurrencyConfig;
import com.projecki.fusion.currency.storage.CurrencyStorage;
import com.projecki.fusion.currency.storage.SqlCurrencyStorage;
import com.projecki.fusion.item.HotbarItem;
import com.projecki.fusion.item.ItemBuilder;
import com.projecki.fusion.menu.MenuViewInteractManager;
import com.projecki.fusion.menu.deprecated.MenuManager;
import com.projecki.fusion.message.MessageClient;
import com.projecki.fusion.message.redis.RedisMessageClient;
import com.projecki.fusion.network.PlayerStorage;
import com.projecki.fusion.network.redis.RedisPlayerStorage;
import com.projecki.fusion.object.ModularObject;
import com.projecki.fusion.party.PaperParties;
import com.projecki.fusion.party.Parties;
import com.projecki.fusion.placeholder.PlatformMessagePlaceholder;
import com.projecki.fusion.placeholder.PlayerCountPlaceholder;
import com.projecki.fusion.placeholder.RankPrefixPlaceholder;
import com.projecki.fusion.redis.CommonRedisKeys;
import com.projecki.fusion.scoreboard.ScoreboardPacketListener;
import com.projecki.fusion.serializer.formatted.JacksonSerializer;
import com.projecki.fusion.serializer.itemstack.ItemStackDeserializer;
import com.projecki.fusion.serializer.itemstack.ItemStackSerializer;
import com.projecki.fusion.serializer.location.LocationDeserializer;
import com.projecki.fusion.serializer.location.LocationSerializer;
import com.projecki.fusion.serializer.profile.ProfileDeserializer;
import com.projecki.fusion.serializer.profile.ProfileSerializer;
import com.projecki.fusion.server.BasicServerData;
import com.projecki.fusion.server.RedisServerDataStorage;
import com.projecki.fusion.server.ServerDataStorage;
import com.projecki.fusion.sql.SqlConfig;
import com.projecki.fusion.sql.SqlConnectionPool;
import com.projecki.fusion.statistic.SqlStatisticLoader;
import com.projecki.fusion.ui.inventory.GUIManager;
import com.projecki.fusion.user.PaperUsers;
import com.projecki.fusion.user.UserTaskExecutor;
import com.projecki.fusion.util.NameResolver;
import com.projecki.fusion.util.NetworkChat;
import com.projecki.fusion.util.PlayerHeadCreator;
import com.projecki.fusion.util.SkinResolver;
import com.projecki.fusion.util.concurrent.RefreshTaskExecutor;
import com.projecki.fusion.util.teleport.BungeeServerTransport;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.logging.Level;

public class FusionPaper extends JavaPlugin {

    // redis
    private static RedisClient redisClient;
    private static RedisAsyncCommands<String, String> redisCommands;
    private static RedisPubSubAsyncCommands<String, String> redisPubSubCommands;
    private static MessageClient messageClient;

    // sql
    private static SqlConnectionPool sqlConnectionPool;
    private static ExecutorService databaseExecutorService;
    private static SqlStatisticLoader statisticLoader;

    // util
    private static NameResolver nameResolver;
    private static SkinResolver skinResolver;
    private static PlayerHeadCreator headCreator;
    private static BungeeServerTransport serverTransport;

    // server data
    private static ServerInfo serverInfo;
    private static ServerDataStorage serverDataStorage;
    private static PlayerStorage playerStorage;

    // command
    private static PaperCommandManager commandManager;

    // currency
    private CurrencyStorage currencyStorage;
    private static PlayerCurrencyCache currencyCache;

    // chat
    private static ChatPipeline chatPipeline;

    // control
    private static ControlMessageHandler controlMessageHandler;
    private static SafeStopHandler safeStopHandler;

    // misc libs
    private static ProtocolManager protocolManager;

    private static MenuManager menuManager;
    private static Parties<Player> parties;
    private static MenuViewInteractManager menuViewInteractManager;
    private static HotbarItem.Registry hotbarItemRegistry;

    // users
    private static PaperUsers users;
    private Thread userTaskThread;
    private RefreshTaskExecutor userTaskExecutor;

    // configs
    private final PaperLocalYamlConfig<SqlConfig> sqlConfigLoader =
            new PaperLocalYamlConfig<>(SqlConfig.class, this, "sql.yml");

    private final PaperLocalYamlConfig<RedisConfig> redisConfigLoader =
            new PaperLocalYamlConfig<>(RedisConfig.class, this, "redis.yml");
    private RedisConfig redisConfig;
    private final PaperLocalYamlConfig<ServerInfoConfig> serverInfoConfigLoader =
            new PaperLocalYamlConfig<>(ServerInfoConfig.class, this, "serverinfo.yml");
    private ServerInfoConfig serverInfoConfig;

    private final PaperLocalYamlConfig<PlatformMessageConfig> messagesConfigLoader =
            new PaperLocalYamlConfig<>(PlatformMessageConfig.class, this, "messages.yml");
    private PlatformMessageConfig messagesConfig;

    private CurrencyConfig currencyConfig;

    // Used for tests
    public FusionPaper() {
        super();
    }

    // Used for tests
    protected FusionPaper(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onLoad() {
        PaperBootstrap.bootstrap();
        protocolManager = ProtocolLibrary.getProtocolManager();
        users = new PaperUsers();
        GUIManager.initialize(this);
    }

    @Override
    public void onEnable() {
        registerSerializers();

        // load configs is done on main thread to prevent depending plugins getting null values after
        try {
            var sqlConfigOpt = sqlConfigLoader.loadConfig().get();
            var redisConfigOpt = redisConfigLoader.loadConfig().get();
            var serverInfoOpt = serverInfoConfigLoader.loadConfig().get();
            var messageConfigOpt = messagesConfigLoader.loadConfig().get();

            redisConfigLoader.loadConfig().whenComplete((v, e) -> {
                if (e != null) e.printStackTrace();
            });

            // validate configs were found
            if (sqlConfigOpt.isEmpty()) sendConfigIssue("sql");
            var issue = sqlConfigOpt.isEmpty();

            if (redisConfigOpt.isEmpty()) sendConfigIssue("redis");
            issue = redisConfigOpt.isEmpty() || issue;

            if (serverInfoOpt.isEmpty()) sendConfigIssue("serverInfo");
            issue = serverInfoOpt.isEmpty() || issue;

            if (messageConfigOpt.isEmpty()) sendConfigIssue("platform messages");
            issue = messageConfigOpt.isEmpty() || issue;

            if (issue) {
                getLogger().log(Level.SEVERE, "ProCommonPaper had a config fail to load.");
                getPluginLoader().disablePlugin(this);
                return;
            }

            sqlConnectionPool = new SqlConnectionPool(sqlConfigOpt.get());

            // redis config
            redisConfig = redisConfigOpt.get();

            @SuppressWarnings("deprecation")
            RedisURI redisURI = RedisURI.builder()
                    .withHost(redisConfig.getHost())
                    .withPort(redisConfig.getPort())
                    .withPassword(redisConfig.getPassword())
                    .withTimeout(Duration.ofSeconds(10)).build();

            redisClient = RedisClient.create(redisURI);
            redisCommands = redisClient.connect().async();
            redisPubSubCommands = redisClient.connectPubSub().async();

            messageClient = new RedisMessageClient(redisCommands, redisPubSubCommands);
            messageClient.subscribe(NetworkChat.CHANNEL);
            messageClient.registerMessageListener(new NetworkChat.Receiver(this));

            // Player info storage
            playerStorage = new RedisPlayerStorage(redisCommands);

            // server info config
            serverDataStorage = new RedisServerDataStorage(redisClient);
            serverInfoConfig = serverInfoOpt.get();

            ServerInfo.get(this, serverInfoConfig).thenAccept(possibleServerInfo ->
                    possibleServerInfo.ifPresentOrElse(s -> {
                        serverInfo = s;

                        // run all this once we get the finished server info
                        serverDataStorage.storeInfo(new BasicServerData(serverInfo.getServerName(),
                                serverInfo.getServerGroup(), Bukkit.getMaxPlayers()));

                        // start heart beating information to redis
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                serverDataStorage.storeInfo(new BasicServerData(
                                        serverInfo.getServerName(),
                                        Bukkit.getOnlinePlayers().size(),
                                        Bukkit.getServer().getMaxPlayers(),
                                        System.currentTimeMillis()
                                ));
                            }
                        }.runTaskTimerAsynchronously(this, 0, 5);
                    }, () -> {
                        getLogger().log(Level.SEVERE, "Could not load server info. (Is ProCommonVelocity on the proxy(s)?)");
                    })
            ).get();

            // messages config
            messagesConfig = messageConfigOpt.get();
            registerPlaceholders();

        } catch (InterruptedException | ExecutionException e) {
            Bukkit.getLogger().severe("There was an exception loading configs.");
            e.printStackTrace();
        }

        // server transport
        serverTransport = new BungeeServerTransport(this);

        // commands
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new PartyCommand(commandManager));

        // setup shared executor service
        databaseExecutorService = new ThreadPoolExecutor(1, 6, 30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());

        // name resolver
        var nameStorage = new NameResolver.RedisNameResolverStorage(redisCommands);
        nameResolver = new NameResolver(databaseExecutorService, nameStorage);

        // skin resolver
        var skinStorage = new SkinResolver.RedisSkinStorage(CommonRedisKeys.PLAYER_CACHE.getKey(), redisCommands);
        skinResolver = new SkinResolver(skinStorage);
        headCreator = new PlayerHeadCreator(skinResolver);

        // parties
        parties = new PaperParties();
        FusionCore.setParties(FusionPaper::getParties);

        // menu manager
        menuManager = new MenuManager(this);
        getServer().getPluginManager().registerEvents(new MenuManager.MenuEventHandler(), this);

        // updated menu manager
        menuViewInteractManager = new MenuViewInteractManager();
        getServer().getPluginManager().registerEvents(new MenuViewInteractManager.MenuEventHandler(this), this);

        // hot bar item manager
        hotbarItemRegistry = new HotbarItem.Registry();
        getServer().getPluginManager().registerEvents(hotbarItemRegistry, this);

        // user manager
        this.userTaskExecutor = new UserTaskExecutor(users);
        this.userTaskThread = new Thread(userTaskExecutor, "User Refresh");
        this.userTaskThread.start();
        getServer().getPluginManager().registerEvents(users, this);

        // register glow enchantment
        ItemBuilder.registerGlowEnchantment(this);

        // currency
        currencyStorage = new SqlCurrencyStorage("balances", sqlConnectionPool);
        PaperHermesConfig<CurrencyConfig> currencyConfigLoader = new PaperHermesConfig<>(
                com.projecki.fusion.config.serialize.JacksonSerializer.ofYaml(CurrencyConfig.class), this, "currency");
        try {
            currencyConfig = currencyConfigLoader.loadConfig().get().orElseThrow(() -> new IllegalStateException("Failed to parse currency config."));

            getLog4JLogger().info("Registering currency types:");
            currencyConfig.getCurrencies(currencyStorage)
                    .forEach(currency -> {

                        // register currencies
                        CurrencyRegister.registerCurrency(currency);
                        Bukkit.getCommandMap().register(currency.plural(), new CreditsCommand(currency));
                        getLog4JLogger().info(" - Registered currency: {}", currency.plural());
                    });
        } catch (InterruptedException | ExecutionException e) {
            Bukkit.getLogger().severe("There was an exception loading the currency configuration from hermes.");
        }

        // setup currency cache
        currencyCache = new PlayerCurrencyCache();
        Bukkit.getPluginManager().registerEvents(currencyCache, this);

        statisticLoader = new SqlStatisticLoader(databaseExecutorService, sqlConnectionPool);

        // chat
        chatPipeline = new ChatPipeline(this);
        chatPipeline.onServerEnable();

        // Register events and packet handling
        GUIManager.register(this, protocolManager);
        ScoreboardPacketListener.register(this, protocolManager);

        // remote server control
        safeStopHandler = new SafeStopHandler();
        controlMessageHandler = new PaperControlMessageHandler(messageClient, serverInfo.getServerName(), safeStopHandler);
    }

    @Override
    public void onDisable() {
        // Shutdown the GUI manager
        GUIManager.shutdown();
        // Shutdown and destroy all users
        users.getAll().forEach(ModularObject::destroy);
        if (userTaskThread != null) {

            this.userTaskExecutor.shutdown();
            try {
                userTaskThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // safely shutdown database executor
        Bukkit.getLogger().info("Awaiting database threads to finish... (This may take up to 10 minutes)");
        boolean successful = false;

        try {
            databaseExecutorService.shutdown();
            successful = databaseExecutorService.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (successful) {
                Bukkit.getLogger().info("All database threads successfully finished");
            } else {
                Bukkit.getLogger().severe("Database threads could not all sucessfully finish, there may be data loss");
            }
        }

        // shut down currency storage
        currencyStorage.close();

        if (sqlConnectionPool != null) // could be null if not configured correctly on enable
            sqlConnectionPool.shutdown();

        if (redisClient != null) // could be null if not configured correctly on enable
            redisClient.shutdown();

        chatPipeline.onServerDisable();
    }

    private void registerPlaceholders() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return;

        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            new RankPrefixPlaceholder().register();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            new PlatformMessagePlaceholder(messagesConfig).register();
        }

        new PlayerCountPlaceholder(serverDataStorage).register();
        new PaperCurrencyPlaceholder().register();

        serverInfoConfigLoader.storeConfig(serverInfoConfig);
        redisConfigLoader.storeConfig(redisConfig);
    }

    private void sendConfigIssue(String configName) {
        Bukkit.getLogger().severe("Failed to load config " + configName);
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

    /**
     * Get the {@link PaperUsers user manager}.
     *
     * @return The {@link PaperUsers}.
     */
    public static PaperUsers getUsers() {
        return users;
    }

    /**
     * Get the {@link PlayerStorage} for info on players'
     * current state on the network.
     *
     * @return The {@link PlayerStorage}.
     */
    public static PlayerStorage getPlayerStorage() {
        return playerStorage;
    }

    /**
     * Gets the data store used among these plugins
     *
     * @return The data store used among these plugins
     */
    public static ServerDataStorage getServerDataStorage() {
        return serverDataStorage;
    }

    /**
     * Gets the info related to this server.
     *
     * @return The info related to this server
     */
    public static Optional<ServerInfo> getServerInfo() {
        return Optional.ofNullable(serverInfo);
    }

    /**
     * Gets the name resolver for this network.
     *
     * @return The name resolver for this network
     */
    public static NameResolver getNameResolver() {
        return nameResolver;
    }

    /**
     * Get the skin resolver for this network.
     *
     * @return The skin resolver for this network.
     */
    public static SkinResolver getSkinResolver() {
        return skinResolver;
    }

    /**
     * Get the {@link PlayerHeadCreator} that's backed by the
     * {@link SkinResolver} for the network to get player head
     * textures from the on-premesis skin-cache instead of blocking
     * the server with a MojangAPI call.
     *
     * @return the player head creator
     */
    public static PlayerHeadCreator getPlayerHeadCreator() {
        return headCreator;
    }

    /**
     * Gets the common connection pool shared between plugins
     *
     * @return The common connection pool shared between plugins
     */
    public static SqlConnectionPool getSqlConnectionPool() {
        return sqlConnectionPool;
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
     * Gets the menu manager
     *
     * @return The menu manager
     * @deprecated use new v2 package
     */
    @Deprecated
    public static MenuManager getMenuManager() {
        return menuManager;
    }

    /**
     * @return The menu view interact manager of the updated menu system
     */
    public static MenuViewInteractManager getMenuViewInteractManager() {
        return menuViewInteractManager;
    }

    /**
     * @return The hotbar item registry class for handling events and hotbar items themselves
     */
    public static HotbarItem.Registry getHotbarItemRegistry() {
        return hotbarItemRegistry;
    }

    /**
     * Get the {@link ExecutorService} that is intended to be used for database operations.
     * It's important to use this service for database operations as when the server stops,
     * it will wait on the main thread to ensure all submitted database transactions are
     * completed so that there is no data lost when saving data near a server stop.
     *
     * @return shared {@link ExecutorService} for data operations
     */
    public static ExecutorService getDatabaseExecutor() {
        return databaseExecutorService;
    }

    /**
     * @return The protocol manager loaded in onEnable for protocol lib
     */
    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    /**
     * @return The chat pipeline
     */
    public static ChatPipeline getChatPipeline() {
        return chatPipeline;
    }

    public static PlayerCurrencyCache getPlayerCurrencyCache () {
        return currencyCache;
    }

    /**
     * @return The command manager for all commands issued to this plugin specifically
     */
    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * @return The sql statistic loader
     */
    public static SqlStatisticLoader getStatisticLoader() {
        return statisticLoader;
    }

    /**
     * @return The BungeeServerTransport util lib used for sending users to another server that are on the server you
     * are calling from.
     */
    public static BungeeServerTransport getServerTransport() {
        return serverTransport;
    }

    /**
     * Get the {@link SafeStopHandler} for the server that handles registering of
     * handlers that tell the control handler when it is safe to stop the server
     */
    public static SafeStopHandler getSafeStopHandler() {
        return safeStopHandler;
    }

    /**
     * Get the {@link ControlMessageHandler} for the server tha handles
     * receiving and registering what to do when a control message is received
     */
    public static ControlMessageHandler getControlMessageHandler() {
        return controlMessageHandler;
    }

    private static void registerSerializers() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Location.class, new LocationSerializer());
        module.addSerializer(ItemStack.class, new ItemStackSerializer());
        module.addSerializer(PlayerProfile.class, new ProfileSerializer());
        module.addDeserializer(Location.class, new LocationDeserializer());
        module.addDeserializer(ItemStack.class, new ItemStackDeserializer());
        module.addDeserializer(PlayerProfile.class, new ProfileDeserializer());
        JacksonSerializer.registerSerializerModule(module);
    }
}
