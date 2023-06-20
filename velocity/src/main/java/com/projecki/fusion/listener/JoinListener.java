package com.projecki.fusion.listener;

import com.projecki.fusion.network.PlayerStorage;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


public class JoinListener {

    private final PlayerStorage playerStorage;

    public JoinListener(PlayerStorage playerStorage) {
        this.playerStorage = playerStorage;
    }

    @Subscribe
    public void onJoin(LoginEvent event) {
        var playerUuid = event.getPlayer().getUniqueId();

        // if a player is already logged onto the network, don't allow them to join again
        playerStorage.getPlayerServer(playerUuid)
                .thenAccept(opt -> {
                    if (opt.isPresent())
                        event.getPlayer().disconnect(
                                Component.text("You're already connected.")
                                        .color(NamedTextColor.RED)
                        );
                });
    }

}
