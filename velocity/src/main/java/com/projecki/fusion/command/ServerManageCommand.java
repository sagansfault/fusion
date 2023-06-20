package com.projecki.fusion.command;

import co.aikar.commands.VelocityCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.projecki.fusion.FusionVelocity;
import com.projecki.fusion.command.base.VelocityCommonBaseCommand;
import com.projecki.fusion.network.ServerRegistry;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.Set;

@CommandPermission("servermanage.admin")
@CommandAlias("servermanage|svm")
public class ServerManageCommand extends VelocityCommonBaseCommand {

    private final ProxyServer proxy;
    private final ServerRegistry serverRegistry;
    private final FusionVelocity plugin;

    public ServerManageCommand(VelocityCommandManager manager, ProxyServer proxy, ServerRegistry serverRegistry,
                               FusionVelocity plugin) {
        super(TextColor.color(0xFEEAFC), TextColor.color(0xF758E6), manager);
        this.proxy = proxy;
        this.serverRegistry = serverRegistry;
        this.plugin = plugin;
    }

    @Subcommand("list")
    @Description("List servers on network")
    public void onList(CommandSource sender) {
        var message = Component.text()
                .append(
                        getHeaderFooter(),
                        Component.newline(),
                        getTitle("Server List")
                );

        for (RegisteredServer server : proxy.getAllServers()) {

            message.append(
                    Component.newline(),
                    Component.text(" - ", secondaryColor),
                    getServerInfoMessage(server.getServerInfo())
            );
        }

        message.append(
                Component.newline(),
                getHeaderFooter()
        );

        sender.sendMessage(message);
    }

    @Subcommand("add")
    @Description("Register a server")
    @Syntax("<server name> <host> <port>")
    public void onAdd(CommandSource sender, String serverName, String host, int port) {

        var serverOpt = proxy.getServer(serverName);

        if (serverOpt.isPresent()) {
            sender.sendMessage(
                    Component.text().append(prefix,
                            Component.text("Another server with the name ", primaryColor),
                            Component.text(serverOpt.get().getServerInfo().getName(), secondaryColor),
                            Component.text(" already exists on the network", primaryColor))
            );
            return;
        }

        var server = new ServerRegistry.ServerInfo(serverName, host, port);

        serverRegistry.addServers(Set.of(server));

        sender.sendMessage(Component.text().append(prefix,
                Component.text("Added the server ", primaryColor),
                getServerInfoMessage(server.toServerInfo()),
                Component.text(" to the network", primaryColor)));

    }

    @Subcommand("remove")
    @Description("Unregister a server")
    @Syntax("<server name>")
    public void onRemove(CommandSource sender, String serverName) {

        var serverOpt = proxy.getServer(serverName);

        if (serverOpt.isEmpty()) {
            sender.sendMessage(getNoServerMessage(serverName));
            return;
        }

        serverRegistry.removeServers(serverName);

        sender.sendMessage(Component.text().append(prefix,
                Component.text("Removed the server ", primaryColor),
                getServerInfoMessage(serverOpt.get().getServerInfo()),
                Component.text(" from network", primaryColor)));
    }

    @Subcommand("edit")
    @Description("Edit a server")
    @Syntax("<server name> <host> <port>")
    public void onEdit(CommandSource sender, String serverName, String host, int port) {
        var serverOpt = proxy.getServer(serverName);

        if (serverOpt.isEmpty()) {
            sender.sendMessage(getNoServerMessage(serverName));
            return;
        }

        var originalServerInfo = serverOpt.get().getServerInfo();
        var serverInfo = new ServerRegistry.ServerInfo(serverName, host, port);

        serverRegistry.addServers(Set.of(serverInfo));

        sender.sendMessage(Component.text()
                .append(prefix,
                        Component.text("Edited server on the network from ", primaryColor),
                        getServerInfoMessage(originalServerInfo),
                        Component.text(" to ", primaryColor),
                        getServerInfoMessage(serverInfo.toServerInfo())));
    }

    @Subcommand("sync")
    @Description("Resync registry with ProCommon list")
    public void onSync(CommandSource sender) {
        proxy.getScheduler().buildTask(plugin, plugin::storeServers).schedule();

        sender.sendMessage(Component.text()
                .append(prefix,
                        Component.text("Resynced the server registry with the fusion server list",
                                primaryColor))
        );
    }

    private TextComponent getNoServerMessage(String serverName) {
        return Component.text().append(prefix,
                        Component.text("No server with the name ", primaryColor),
                        Component.text(serverName, secondaryColor),
                        Component.text(" exists", primaryColor))
                .build();
    }

    private TextComponent getServerInfoMessage(ServerInfo serverInfo) {
        var address = serverInfo.getAddress();

        return Component.text().append(
                        Component.text(serverInfo.getName() + " ", secondaryColor),
                        Component.text(address.getHostString(), primaryColor),
                        Component.text(":" + address.getPort(), NamedTextColor.GRAY))
                .build();
    }

}
