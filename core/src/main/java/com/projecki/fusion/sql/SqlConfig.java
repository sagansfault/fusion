package com.projecki.fusion.sql;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class SqlConfig {

    private String url = "";
    private String username = "";
    private String password = "";
    @JsonProperty("table_prefix")
    @SerializedName("table_prefix")
    private String tablePrefix;
    private final Map<String, String> mappings = Map.of();
    private final Map<String, Integer> executor = Map.of();

    public SqlConfig() {
    }

    public SqlConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public Map<String, String> getMappings() {
        return mappings;
    }

    public Map<String, Integer> getExecutor() {
        return executor;
    }
}
