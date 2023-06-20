package com.projecki.fusion.user;

import com.projecki.fusion.object.ModularObject.InitializationStep;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;

/**
 * @since May 27, 2022
 * @author Andavin
 */
public class PaperUsers implements Users<PaperUser, Player>, Listener {

    private final List<PaperUser> online = new CopyOnWriteArrayList<>();
    private final List<PaperUser> onlineView = unmodifiableList(online);
    private final Map<UUID, PaperUser> users = new ConcurrentHashMap<>();
    private final Collection<PaperUser> userView = unmodifiableCollection(users.values());

    /**
     * Get the {@link PaperUser} by their {@link UUID}.
     *
     * @param uuid The {@link UUID} to get the {@link PaperUser} by.
     * @return The {@link PaperUser}.
     */
    public Optional<PaperUser> get(UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

    @Override
    public @NotNull PaperUser get(Player player) {
        return users.computeIfAbsent(player.getUniqueId(), __ -> {
            PaperUser user = new PaperUser();
            user.reference(player);
            user.initialize();
            online.add(user);
            return user;
        });
    }

    @Override
    public List<PaperUser> getOnline() {
        return onlineView;
    }

    @Override
    public Collection<PaperUser> getAll() {
        return userView;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void on(AsyncPlayerPreLoginEvent event) {
        PaperUser user = new PaperUser(event.getPlayerProfile());
        user.initialize(InitializationStep.SETUP);
        this.users.put(user.uuid(), user);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void on(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        PaperUser user = users.get(player.getUniqueId());
        user.reference(player);
        user.initialize(InitializationStep.CREATE);
        this.online.add(user);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void on(PlayerJoinEvent event) {
        this.get(event.getPlayer()).initialize(InitializationStep.ENABLE);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerQuitEvent event) {

        PaperUser user = users.remove(event.getPlayer().getUniqueId());
        if (user != null) {
            online.remove(user);
            user.destroy();
        }
    }
}
