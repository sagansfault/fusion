/*
 * This file is generated by jOOQ.
 */
package com.projecki.fusion.sql.generated.tables;


import com.projecki.fusion.sql.generated.Keys;
import com.projecki.fusion.sql.generated.Settings;
import com.projecki.fusion.sql.generated.tables.records.PlayerSettingsRecord;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row3;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PlayerSettings extends TableImpl<PlayerSettingsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>settings.player_settings</code>
     */
    public static final PlayerSettings PLAYER_SETTINGS = new PlayerSettings();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PlayerSettingsRecord> getRecordType() {
        return PlayerSettingsRecord.class;
    }

    /**
     * The column <code>settings.player_settings.uuid</code>.
     */
    public final TableField<PlayerSettingsRecord, byte[]> UUID = createField(DSL.name("uuid"), SQLDataType.BINARY(16).nullable(false), this, "");

    /**
     * The column <code>settings.player_settings.namespace</code>.
     */
    public final TableField<PlayerSettingsRecord, String> NAMESPACE = createField(DSL.name("namespace"), SQLDataType.VARCHAR(20).nullable(false), this, "");

    /**
     * The column <code>settings.player_settings.id</code>.
     */
    public final TableField<PlayerSettingsRecord, String> ID = createField(DSL.name("id"), SQLDataType.VARCHAR(16).nullable(false), this, "");

    private PlayerSettings(Name alias, Table<PlayerSettingsRecord> aliased) {
        this(alias, aliased, null);
    }

    private PlayerSettings(Name alias, Table<PlayerSettingsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>settings.player_settings</code> table reference
     */
    public PlayerSettings(String alias) {
        this(DSL.name(alias), PLAYER_SETTINGS);
    }

    /**
     * Create an aliased <code>settings.player_settings</code> table reference
     */
    public PlayerSettings(Name alias) {
        this(alias, PLAYER_SETTINGS);
    }

    /**
     * Create a <code>settings.player_settings</code> table reference
     */
    public PlayerSettings() {
        this(DSL.name("player_settings"), null);
    }

    public <O extends Record> PlayerSettings(Table<O> child, ForeignKey<O, PlayerSettingsRecord> key) {
        super(child, key, PLAYER_SETTINGS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Settings.SETTINGS;
    }

    @Override
    public UniqueKey<PlayerSettingsRecord> getPrimaryKey() {
        return Keys.KEY_PLAYER_SETTINGS_PRIMARY;
    }

    @Override
    public PlayerSettings as(String alias) {
        return new PlayerSettings(DSL.name(alias), this);
    }

    @Override
    public PlayerSettings as(Name alias) {
        return new PlayerSettings(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public PlayerSettings rename(String name) {
        return new PlayerSettings(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public PlayerSettings rename(Name name) {
        return new PlayerSettings(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<byte[], String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
