package com.projecki.fusion.network;

import com.projecki.fusion.util.SkinPair;
import com.projecki.fusion.util.SkinResolver;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.util.GameProfile;

import java.util.Optional;

public class SkinCacheListener {

    private final SkinResolver.SkinStorage skinStorage;

    public SkinCacheListener(SkinResolver.SkinStorage skinStorage) {
        this.skinStorage = skinStorage;
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        getSkinProperty(event.getPlayer().getGameProfile())
                .ifPresent(prop ->
                        skinStorage.storeSkin(event.getPlayer().getUniqueId(),
                                new SkinPair(prop.getValue(), prop.getSignature())));
    }

    private Optional<GameProfile.Property> getSkinProperty(GameProfile gameProfile) {
        return gameProfile.getProperties().stream()
                .filter(prop -> prop.getName().equals("textures"))
                .findAny();
    }
}
