package com.projecki.fusion.network;

import com.projecki.fusion.util.NameResolver;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;

/**
 * Listens for player joins and puts player's uuid and
 * name in the on-premesis player mappings cache
 */
public class NameCacheListener {

    private final NameResolver.NameResolverStorage storage;

    public NameCacheListener(NameResolver.NameResolverStorage storage) {
        this.storage = storage;
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        storage.store(event.getPlayer().getUsername(), event.getPlayer().getUniqueId());
    }
}
