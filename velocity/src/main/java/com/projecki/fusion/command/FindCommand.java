package com.projecki.fusion.command;

import co.aikar.commands.VelocityCommandManager;
import co.aikar.commands.annotation.*;
import com.projecki.fusion.command.base.VelocityCommonBaseCommand;
import com.projecki.fusion.component.ComponentBuilder;
import com.projecki.fusion.network.PlayerStorage;
import com.projecki.fusion.util.NameResolver;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@CommandAlias("find")
@CommandPermission("proxy.find")
public class FindCommand extends VelocityCommonBaseCommand {

    private final ProxyServer proxyServer;
    private final PlayerStorage playerStorage;
    private final NameResolver nameResolver;

    public FindCommand(VelocityCommandManager manager, ProxyServer proxyServer, PlayerStorage playerStorage, NameResolver nameResolver) {
        super(TextColor.color(0xAFFAD4), TextColor.color(0x44F39A), manager);
        this.proxyServer = proxyServer;
        this.playerStorage = playerStorage;
        this.nameResolver = nameResolver;
    }

    @Default
    @Description("Find a player on the network")
    @Syntax("<name>")
    public void onFind(CommandSource commandSource, String playerName) {

        var uuidFuture = nameResolver.resolveUuid(playerName);
        var nameFuture = nameResolver.resolveRealName(playerName);

        CompletableFuture.allOf(uuidFuture, nameFuture)
                .thenRun(() -> {
                    var uuid = uuidFuture.getNow(Optional.empty());
                    var name = nameFuture.getNow(playerName);

                    if (uuid.isPresent()) {
                        var serverFuture = playerStorage.getPlayerServer(uuid.get());
                        var proxyFuture = playerStorage.getPlayerProxy(uuid.get());
                        var heartbeatFuture = playerStorage.getLastHeartbeat(uuid.get());

                        CompletableFuture.allOf(serverFuture, proxyFuture, heartbeatFuture)
                                .thenRun(() -> {
                                    if (heartbeatFuture.getNow(Optional.empty()).orElse(0L) + (1000 * 10) > System.currentTimeMillis()) {
                                        var message = ComponentBuilder.builder()
                                                .content(getTitle(name))
                                                .content(Component.newline())
                                                .content("- Server ", secondaryColor)
                                                .content(
                                                        Component.text(serverFuture.getNow(Optional.empty()).orElse("EMPTY"), primaryColor)
                                                                .hoverEvent(HoverEvent.showText(Component.text("Click to join server", secondaryColor)))
                                                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + playerName))
                                                )
                                                .content(Component.newline())
                                                .content("- Proxy ", secondaryColor)
                                                .content(proxyFuture.getNow(Optional.empty()).orElse("EMPTY"), primaryColor)
                                                .content(Component.newline())
                                                .content("- UUID ", secondaryColor)
                                                .content(uuid.get().toString(), primaryColor);

                                        commandSource.sendMessage(message.toComponent());
                                    } else {
                                        commandSource.sendMessage(
                                                Component.text("That player is not online.", primaryColor)
                                        );
                                    }

                                });

                    } else {
                        commandSource.sendMessage(
                                Component.text("No player exists or has logged on with that name.", primaryColor)
                        );
                    }
                });


    }

}
