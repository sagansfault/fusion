package com.projecki.fusion.server;

import com.google.gson.JsonSyntaxException;
import com.projecki.fusion.FusionCore;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ServerData {

    private String serverName;
    private Map<String, String> fields = new HashMap<>();

    public ServerData(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

    protected final <T> Optional<T> getField(String field, Type type) {
        T value;

        try {
            value = FusionCore.GSON.fromJson(fields.get(field), type);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return Optional.empty();
        }

        return Optional.ofNullable(value);
    }

    protected final <T> Optional<T> getField(String field, Class<T> type) {
        return getField(field, (Type) type);
    }

    protected final void putField(String field, Object value) {
        fields.put(field, FusionCore.GSON.toJson(value));
    }

    void setServer(String serverName) {
        this.serverName = serverName;
    }

    void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    final Map<String, String> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "[servername=" + this.serverName + ", " + this.fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ")) + "]";
    }
}
