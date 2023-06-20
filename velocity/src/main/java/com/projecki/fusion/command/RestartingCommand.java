package com.projecki.fusion.command;

import co.aikar.commands.VelocityCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.projecki.fusion.command.base.VelocityCommonBaseCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.time.Duration;

@CommandAlias("restarting")
@CommandPermission("proxy.restarting")
public class RestartingCommand extends VelocityCommonBaseCommand {

    private final Object plugin;
    private final ProxyServer proxyServer;
    private final Component relogMessage;

    private ScheduledTask messageTask;

    public RestartingCommand(VelocityCommandManager manager, Object plugin, ProxyServer proxyServer) {
        super(TextColor.color(0xFFCECC), TextColor.color(0xFF544D), manager);
        this.plugin = plugin;
        this.proxyServer = proxyServer;

        relogMessage =
                getHeaderFooter()
                        .append(Component.newline())
                        .append(
                                offsetMessage(
                                        Component.text("    THIS PROXY IS RESTARTING. PLEASE RE-LOGIN")
                                                .decoration(TextDecoration.BOLD, true)
                                                .color(primaryColor)

                                )
                        )
                        .append(Component.newline())
                        .append(getHeaderFooter());
    }

    private void startTask() {
        messageTask = proxyServer.getScheduler()
                .buildTask(plugin, new AlertTask())
                .repeat(Duration.ofMinutes(5L))
                .schedule();
    }

    private void stopTask() {
        if (messageTask != null) {
            messageTask.cancel();
            messageTask = null;
        }
    }

    private void sendStatusMessage(CommandSource audience) {
        audience.sendMessage(
                prefix.append(
                                Component.text("This proxy is set to ")
                                        .color(primaryColor)
                        )
                        .append(
                                Component.text((messageTask == null ? "NOT " : "") + "RESTARTING")
                                        .color(secondaryColor)
                                        .decorate(TextDecoration.BOLD)
                        )
        );
    }

    @Subcommand("set")
    @Description("Set if this proxy is restarting")
    @Syntax("<true/false>")
    public void onSet(CommandSource commandIssuer, boolean restarting) {
        if (restarting) {
            stopTask();
            startTask();
        } else {
            stopTask();
        }

        sendStatusMessage(commandIssuer);
    }

    @Subcommand("status")
    @Description("Check if this proxy is restarting")
    public void onStatus(CommandSource commandIssuer) {
        sendStatusMessage(commandIssuer);
    }

    @Subcommand("message")
    @Description("Send the relog message to players on the proxy")
    public void onMessage() {
        proxyServer.getAllPlayers().forEach(p -> p.sendMessage(relogMessage));
    }

    public class AlertTask implements Runnable {

        @Override
        public void run() {
            proxyServer.sendMessage(relogMessage);
        }
    }

}
