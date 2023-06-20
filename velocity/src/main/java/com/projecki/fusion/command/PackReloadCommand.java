package com.projecki.fusion.command;

import co.aikar.commands.VelocityCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.projecki.fusion.command.base.VelocityCommonBaseCommand;
import com.projecki.fusion.message.MessageClient;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

@CommandAlias("packreload")
@CommandPermission("proxy.packreload")
public class PackReloadCommand extends VelocityCommonBaseCommand {

    private static final String UPDATE_CHANNEL = "pack-reload";

    private final ProxyServer proxyServer;
    private final MessageClient messageClient;

    public PackReloadCommand(VelocityCommandManager manager, ProxyServer proxyServer, MessageClient messageClient) {
        super(TextColor.color(0xFFCECC), TextColor.color(0xFF544D), manager);
        this.proxyServer = proxyServer;
        this.messageClient = messageClient;

        messageClient.subscribe(UPDATE_CHANNEL);
        messageClient.registerMessageListener(PackReloadMessage.class, (c, m) -> reloadPack());
    }

    @Default
    @Description("Reload the pack on all online proxies")
    public void onPackReload(CommandSource commandSource) {
        commandSource.sendMessage(
                Component.text("Reloading packs on all proxies.", primaryColor)
        );
        messageClient.send(UPDATE_CHANNEL, new PackReloadMessage());
    }

    private void reloadPack() {
        var source = proxyServer.getConsoleCommandSource();
        proxyServer.getCommandManager().executeAsync(source, "frp reload");
        proxyServer.getCommandManager().executeAsync(source, "frp generatehashes");
    }


    public class PackReloadMessage implements MessageClient.Message {
    }

}
