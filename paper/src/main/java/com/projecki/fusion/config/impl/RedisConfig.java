package com.projecki.fusion.config.impl;

public class RedisConfig {

    private String host;
    private int port;
    private String password;
    private int database;

    public RedisConfig() {}

    public RedisConfig(String host, int port, String password, int database) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.database = database;
    }

    public RedisConfig(String host, int port, String password) {
        this(host, port, password, 0);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }
}
