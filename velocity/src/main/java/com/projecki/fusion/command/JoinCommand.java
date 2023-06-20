package com.projecki.fusion.command;

import co.aikar.commands.VelocityCommandManager;
import co.aikar.commands.annotation.*;
import com.projecki.fusion.command.base.VelocityCommonBaseCommand;
import com.projecki.fusion.network.PlayerStorage;
import com.projecki.fusion.util.NameResolver;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

@CommandAlias("join")
@CommandPermission("proxy.join")
public class JoinCommand extends VelocityCommonBaseCommand {

    private final ProxyServer proxyServer;
    private final PlayerStorage playerStorage;
    private final NameResolver nameResolver;

    public JoinCommand(VelocityCommandManager manager, ProxyServer proxyServer, PlayerStorage playerStorage, NameResolver nameResolver) {
        super(TextColor.color(0xAFFAD4), TextColor.color(0x44F39A), manager);
        this.proxyServer = proxyServer;
        this.playerStorage = playerStorage;
        this.nameResolver = nameResolver;
    }

    @Default
    @Description("Join a player's server")
    @Syntax("<name>")
    public void onFind(Player player, String playerName) {

        nameResolver.resolveUuid(playerName)
                .thenAccept(uuid -> {
                    if (uuid.isPresent()) {
                        playerStorage.getPlayerServer(uuid.get())
                                .thenAccept(serverName -> {
                                    if (serverName.isPresent()) {
                                        var serverOpt = proxyServer.getServer(serverName.get());
                                        if (serverOpt.isPresent()) {
                                            player.createConnectionRequest(serverOpt.get()).connect();
                                        } else {
                                            player.sendMessage(
                                                    Component.text("Unable to connect to the server the player is on.", primaryColor)
                                            );
                                        }
                                    } else {
                                        player.sendMessage(
                                                Component.text("Unable to find the server the player is on.", primaryColor)
                                        );
                                    }
                                });
                    } else {
                        player.sendMessage(
                                Component.text("There is no player online by that name.", primaryColor)
                        );
                    }
                });
    }

}
