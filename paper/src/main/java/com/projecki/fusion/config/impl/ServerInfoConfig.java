package com.projecki.fusion.config.impl;

public class ServerInfoConfig {

    private String servergroup;
    private String hermesOrganization;

    public ServerInfoConfig() {}

    public ServerInfoConfig(String servergroup, String hermesOrganization) {
        this.servergroup = servergroup;
        this.hermesOrganization = hermesOrganization;
    }

    public String getServergroup() {
        return servergroup;
    }

    public String getHermesOrganization() {
        return hermesOrganization;
    }
}
