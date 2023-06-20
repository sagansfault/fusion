package com.projecki.fusion.config.impl;

public class VelocityConfig {

    // redis config
    private String redisAddress;
    private String redisPort;
    private String redisPassword;

    // multi-proxy
    private String proxyId;

    // voting config
    private int votifierPort;

    private String organization = "none";

    public VelocityConfig() {}

    public String getRedisAddress() {
        return redisAddress;
    }

    public String getRedisPort() {
        return redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public String getProxyId() {
        return proxyId;
    }

    public int getVotifierPort() {
        return votifierPort;
    }

    public String getOrganization() {
        return organization;
    }
}
