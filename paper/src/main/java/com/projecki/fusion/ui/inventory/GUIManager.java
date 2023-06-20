package com.projecki.fusion.ui.inventory;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.GamePhase;
import com.projecki.fusion.ui.inventory.icon.Icon;
import com.projecki.fusion.util.concurrent.NonBlockingExecutor;
import com.projecki.fusion.util.concurrent.RefreshTask;
import com.projecki.fusion.util.concurrent.RefreshTaskExecutor;
import com.projecki.unversioned.window.WindowManager;
import com.projecki.unversioned.window.WindowService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * @since April 09, 2022
 * @author Andavin
 */
public class GUIManager implements WindowManager<GUIMenu>, PacketListener {

    static GUIManager INSTANCE;

    /**
     * Initialize the {@link GUIManager} singleton instance.
     *
     * @param plugin The {@link Plugin} that is initializing.
     */
    public static void initialize(Plugin plugin) {
        checkState(INSTANCE == null, "already initialized");
        INSTANCE = new GUIManager(plugin);
    }

    /**
     * Register the {@link PacketListener} and {@link Listener}
     * events with the {@link Plugin} and {@link ProtocolManager}.
     *
     * @param plugin The {@link Plugin} to use to register the events.
     * @param protocolManager The {@link ProtocolManager} to register
     *                        the {@link PacketListener} with.
     */
    public static void register(Plugin plugin, ProtocolManager protocolManager) {
        checkNotNull(INSTANCE, "not initialized");
        protocolManager.addPacketListener(INSTANCE);
        // Register the events for the menu
        Bukkit.getPluginManager().registerEvents(new MenuListener(INSTANCE), plugin);
    }

    /**
     * Shutdown the {@link GUIManager} singleton instance.
     */
    public static void shutdown() {

        INSTANCE.taskExecutor.shutdown();
        try {
            INSTANCE.taskThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private final Plugin plugin;
    private final ListeningWhitelist clientbound, serverbound;
    private final AtomicInteger containerId = new AtomicInteger(1);
    private final Map<UUID, WindowEntry<GUIMenu>> menus = new ConcurrentHashMap<>();

    private final Thread taskThread;
    private final RefreshTaskExecutor taskExecutor;
    private final Executor executor = new NonBlockingExecutor("GUI Actions", 25);

    private GUIManager(Plugin plugin) {
        this.plugin = plugin;
        this.clientbound = ListeningWhitelist.newBuilder()
                .priority(ListenerPriority.NORMAL)
                .gamePhase(GamePhase.PLAYING)
                .options(Set.of(ListenerOptions.ASYNC))
                .types(
                        Server.SET_SLOT, // ClientboundContainerSetSlotPacket
                        Server.WINDOW_ITEMS // ClientboundContainerSetContentPacket
                ).build();
        this.serverbound = ListeningWhitelist.newBuilder()
                .priority(ListenerPriority.NORMAL)
                .gamePhase(GamePhase.PLAYING)
                .options(Set.of(ListenerOptions.ASYNC))
                .types(
                        Client.WINDOW_CLICK, // ServerboundContainerClickPacket
                        Client.ARM_ANIMATION, // ServerboundSwingPacket
                        Client.BLOCK_DIG, // ServerboundPlayerActionPacket
                        Client.BLOCK_PLACE, // ServerboundUseItemPacket
                        Client.USE_ITEM, // ServerboundUseItemOnPacket
                        Client.USE_ENTITY, // ServerboundInteractPacket
                        Client.ITEM_NAME, // ServerboundRenameItemPacket
                        Client.CLOSE_WINDOW // ServerboundContainerClosePacket
                ).build();
        this.taskExecutor = new GUITaskExecutor(this);
        this.taskThread = new Thread(taskExecutor, "GUI Refresh");
        this.taskThread.start();
    }

    /**
     * Create a new {@link GUIMenu} of the given
     * {@link InventoryType} and of the given size.
     * <p>
     * <b>Note</b>: Be careful with adjusting the size on certain
     * types of inventories (e.g. {@link InventoryType#ANVIL}) as
     * they may either throw errors/crash the client if the size
     * is not the default, or they may simply not work.
     * <br>
     * In addition, some menu types may or may not open at all
     * since they are not designed for the server to instruct
     * the client to open:
     * <ul>
     *     <li>{@link InventoryType#CRAFTING}</li>
     *     <li>{@link InventoryType#CREATIVE}</li>
     *     <li>{@link InventoryType#MERCHANT}</li>
     * </ul>
     * Behavior when attempting to use these types is completely
     * undefined by this API, and it is recommended not to use them.
     *
     * @param gui The {@link GUI} that is creating the menu.
     * @param title The title of the menu.
     * @param type The type of menu to create.
     * @param size The size of the menu to create.
     * @return The newly created {@link GUIMenu}.
     */
    GUIMenu createMenu(GUI gui, InventoryType type, Component title, int size) {
        return new GUIMenu(plugin, gui, title, type, size);
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public ListeningWhitelist getSendingWhitelist() {
        return clientbound;
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist() {
        return serverbound;
    }

    @Override
    public @NotNull WindowEntry windowEntry(Player player) {
        return WindowEntry.emptyIfNull(menus.get(player.getUniqueId()));
    }

    @Override
    public void execute(@NotNull Runnable command) {
        executor.execute(command);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        WindowService.INSTANCE.handlePacketSend(this, event);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        WindowService.INSTANCE.handlePacketReceive(this, event);
    }

    /**
     * Ensure that the given menu is cached for packet
     * listening and handling.
     *
     * @param menu The menu to cache.
     */
    void cache(GUIMenu menu) {

        checkState(menu.player() != null, "menu is not open");
        WindowEntry<GUIMenu> previous = menus.put(menu.player().getUniqueId(),
                new WindowEntry<>(menu.id(), menu, new Count()));
        if (previous != null) {
            // Cleanup the previous menu if it is present
            previous.window().cleanup();
        }
    }

    /**
     * Clear all the IDs from the given player after
     * they have closed an menu.
     *
     * @param player The player that closed the menu.
     */
    @Override
    public void clear(Player player) {
        this.menus.remove(player.getUniqueId());
    }

    private static final class GUITaskExecutor extends RefreshTaskExecutor {

        private static final long PERIOD = ONE_MILLIS * 50;
        private final GUIManager manager;
        private final Object refreshLock = new Object();

        GUITaskExecutor(GUIManager manager) {
            super(PERIOD);
            this.manager = manager;
        }

        @Override
        protected void execute(long currentTime) {

            for (WindowEntry<GUIMenu> entry : manager.menus.values()) {

                RefreshTask guiTask = entry.window().gui().refreshTask();
                if (guiTask != null && guiTask.shouldRun(currentTime)) {
                    guiTask.execute(currentTime);
                }

                for (Icon icon : entry.window().icons()) {

                    if (icon != null) {

                        RefreshTask iconTask = icon.refreshTask();
                        if (iconTask != null && iconTask.shouldRun(currentTime)) {
                            iconTask.execute(currentTime);
                        }

                        RefreshTask errorTask = icon.errorTask();
                        if (errorTask != null && errorTask.shouldRun(currentTime)) {
                            errorTask.execute(currentTime);
                        }
                    }
                }
            }
        }
    }

    private record MenuListener(GUIManager manager) implements Listener {

        @EventHandler
        public void onMove(PlayerMoveEvent event) {

            Player player = event.getPlayer();
            WindowEntry<GUIMenu> entry = manager.menus.get(player.getUniqueId());
            if (entry != null && !entry.window().allowMovement) {

                Location to = event.getTo();
                Location location = entry.window().location;
                if (!location.isWorldLoaded() ||
                    !to.isWorldLoaded() ||
                    !location.getWorld().equals(to.getWorld()) ||
                    NumberConversions.square(location.getX() - to.getX()) +
                    NumberConversions.square(location.getZ() - to.getZ()) > 3 * 3) {

                    manager.clear(player);
                    manager.execute(() -> {
                        try {
                            entry.window().close();
                            player.updateInventory();
                        } catch (Throwable e) {
                            Bukkit.getLogger().log(Level.SEVERE, "Exception caught while closing a menu", e);
                        }
                    });
                }
            }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {

            Player player = event.getPlayer();
            WindowEntry<GUIMenu> entry = manager.menus.remove(player.getUniqueId());
            if (entry != null) {
                manager.execute(() -> {
                    try {
                        entry.window().close();
                    } catch (Throwable e) {
                        Bukkit.getLogger().log(Level.SEVERE, "Exception caught while closing a menu", e);
                    }
                });
            }
        }
    }
}
