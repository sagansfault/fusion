package com.projecki.fusion;

import com.projecki.fusion.config.impl.ServerInfoConfig;
import com.projecki.fusion.util.NetworkingUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ServerInfo {

    private final String serverGroup;
    private final String serverName;
    private final String thisIp;
    private final String thisPort;
    private final String hermesOrganization;

    private ServerInfo(String serverGroup, String serverName, String hermesOrganization, String thisIp, String thisPort) {
        this.serverGroup = serverGroup;
        this.serverName = serverName;
        this.hermesOrganization = hermesOrganization;
        this.thisIp = thisIp;
        this.thisPort = thisPort;
    }

    public static CompletableFuture<Optional<ServerInfo>> get(FusionPaper plugin, ServerInfoConfig serverInfoConfig) {

        String port = String.valueOf(plugin.getServer().getPort());
        return NetworkingUtil.getIp()
                .exceptionally(e -> null)
                .thenApply(Optional::ofNullable)
        .thenCompose(ipOptional -> {
            if (ipOptional.isPresent()) {
                String ip = ipOptional.get();
                return FusionCore.getServerName(FusionPaper.getRedisCommands(), ipOptional.get(), port).thenCompose(fstTry -> {
                    if (fstTry.isPresent()) {
                        Optional<ServerInfo> serverInfoOptional = Optional.of(new ServerInfo(
                                serverInfoConfig.getServergroup(),
                                fstTry.get(),
                                serverInfoConfig.getHermesOrganization(),
                                ip,
                                port
                        ));
                        return CompletableFuture.completedFuture(serverInfoOptional);
                    } else {
                        InetAddress inetAddress;
                        try {
                            inetAddress = InetAddress.getLocalHost();
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                            return CompletableFuture.completedFuture(Optional.empty());
                        }
                        String address = inetAddress.getHostAddress();
                        return FusionCore.getServerName(FusionPaper.getRedisCommands(), address, port).thenCompose(sndTry -> {
                            if (sndTry.isPresent()) {
                                Optional<ServerInfo> serverInfoOptional = Optional.of(new ServerInfo(
                                        serverInfoConfig.getServergroup(),
                                        sndTry.get(),
                                        serverInfoConfig.getHermesOrganization(),
                                        ip,
                                        port
                                ));
                                return CompletableFuture.completedFuture(serverInfoOptional);
                            } else {
                                return FusionCore.getServerName(FusionPaper.getRedisCommands(), "127.0.0.1", port).thenCompose(trdTry -> {
                                    if (trdTry.isPresent()) {
                                        Optional<ServerInfo> serverInfoOptional = Optional.of(new ServerInfo(
                                                serverInfoConfig.getServergroup(),
                                                trdTry.get(),
                                                serverInfoConfig.getHermesOrganization(),
                                                ip,
                                                port
                                        ));
                                        return CompletableFuture.completedFuture(serverInfoOptional);
                                    } else {
                                        return CompletableFuture.completedFuture(Optional.empty());
                                    }
                                });
                            }
                        });
                    }
                });
            } else {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        });
    }

    public String getServerGroup() {
        return serverGroup;
    }

    public String getServerName() {
        return serverName;
    }

    public String getHermesOrganization() {
        return hermesOrganization;
    }

    public String getThisIp() {
        return thisIp;
    }

    public String getThisPort() {
        return thisPort;
    }
}
