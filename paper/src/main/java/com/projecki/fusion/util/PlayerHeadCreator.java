package com.projecki.fusion.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerHeadCreator {

    private final SkinResolver skinResolver;

    public PlayerHeadCreator(SkinResolver skinResolver) {
        this.skinResolver = skinResolver;
    }

    /**
     * Get the head of a specified player
     * <p>
     * This method completes while running in the Bukkit
     * main thread, so it's safe to do inventory manipulation
     * inside a {@code thenAccept()} chained to this returned
     * future
     *
     * @param headOwner owner of the head whose skin will be on the head
     * @return future containing {@link ItemStack} of the player's head
     */
    public CompletableFuture<ItemStack> getHead(UUID headOwner) {

        skinResolver.resolveSkin(headOwner);

        var skull = new ItemStack(Material.PLAYER_HEAD);

        return skinResolver.resolveSkin(headOwner)
                .thenAccept(opt -> opt.ifPresent(pair -> {
                    Bukkit.getUnsafe().modifyItemStack(skull,
                            String.format("{SkullOwner:{Id:\"%s\",Properties:{textures:[{Value:\"%s\"}]}}}",
                                    headOwner.toString(), pair.skin()));
                }))
                .thenApply(v -> skull);
    }
}
