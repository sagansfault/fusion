package com.projecki.fusion.listener;

import com.projecki.fusion.config.MotdContainer;
import com.projecki.fusion.network.PlayerStorage;
import com.projecki.fusion.util.ValueCache;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;

import java.time.Duration;

public class PingListener {

    private final ValueCache<Long> playerCountCache;
    private final MotdContainer motdContainer;

    public PingListener(PlayerStorage playerStorage, MotdContainer motdContainer) {
        playerCountCache = ValueCache.create(Duration.ofMillis(500), playerStorage::getPlayerCount);
        this.motdContainer = motdContainer;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPing(ProxyPingEvent event) {
        event.setPing(
                event.getPing().asBuilder()
                        .description(motdContainer.getMotd())
                        .onlinePlayers((playerCountCache.getValue().intValue()))
                        .build()
        );
    }

}
