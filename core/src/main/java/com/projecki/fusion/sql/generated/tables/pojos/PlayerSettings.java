/*
 * This file is generated by jOOQ.
 */
package com.projecki.fusion.sql.generated.tables.pojos;


import java.io.Serializable;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PlayerSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    private final byte[] uuid;
    private final String namespace;
    private final String id;

    public PlayerSettings(PlayerSettings value) {
        this.uuid = value.uuid;
        this.namespace = value.namespace;
        this.id = value.id;
    }

    public PlayerSettings(
            byte[] uuid,
            String namespace,
            String id
    ) {
        this.uuid = uuid;
        this.namespace = namespace;
        this.id = id;
    }

    /**
     * Getter for <code>settings.player_settings.uuid</code>.
     */
    public byte[] getUuid() {
        return this.uuid;
    }

    /**
     * Getter for <code>settings.player_settings.namespace</code>.
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * Getter for <code>settings.player_settings.id</code>.
     */
    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PlayerSettings (");

        sb.append("[binary...]");
        sb.append(", ").append(namespace);
        sb.append(", ").append(id);

        sb.append(")");
        return sb.toString();
    }
}
