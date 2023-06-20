/*
 * This file is generated by jOOQ.
 */
package com.projecki.fusion.sql.generated;


import com.projecki.fusion.sql.generated.tables.PlayerSettings;
import com.projecki.fusion.sql.generated.tables.records.PlayerSettingsRecord;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * settings.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<PlayerSettingsRecord> KEY_PLAYER_SETTINGS_PRIMARY = Internal.createUniqueKey(PlayerSettings.PLAYER_SETTINGS, DSL.name("KEY_player_settings_PRIMARY"), new TableField[]{ PlayerSettings.PLAYER_SETTINGS.UUID, PlayerSettings.PLAYER_SETTINGS.NAMESPACE }, true);
}