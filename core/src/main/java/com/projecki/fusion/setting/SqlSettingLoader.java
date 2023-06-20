package com.projecki.fusion.setting;

import com.projecki.fusion.sql.SqlConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class SqlSettingLoader extends SettingLoader {

    /*
     * NOTES: examples below on how to use jOOQ to replace
     * the boilerplate code from java.sql
     *
     * The changed are commented out in order to prevent an issues
     * for anyone currently using fusion core needing to update their
     * SQL config with schema mappings
     *
     * A couple of things:
     * 1. The executor isn't really needed, but can be there as in the examples
     *    One is provided by default in the SqlConnectionPool
     * 2. We should be using CompletionStage whenever possible in keeping with
     *    Java coding conventions rather than CompletableFuture
     *    CompletionStage can easily be made into a CompletableFuture if needed
     *    as shown below
     */


    //create table player_settings
    //(
    //    uuid      binary(16)  not null,
    //    namespace varchar(20) not null,
    //    id        varchar(16) not null,
    //    constraint player_settings_pk
    //        primary key (uuid, namespace)
    //);

    public static final String SCHEMA = "CREATE TABLE IF NOT EXISTS player_settings(uuid BINARY(16) NOT NULL, namespace VARCHAR(20) NOT NULL, id VARCHAR(16) NOT NULL, PRIMARY KEY (uuid, namespace));";
    private static final String INSERT = "INSERT INTO player_settings (uuid, namespace, id) VALUES (UNHEX(?), ?, ?) ON DUPLICATE KEY UPDATE id=VALUES(id);";
    private static final String RETRIEVE = "SELECT namespace, id FROM player_settings WHERE uuid=UNHEX(?);";

    private final SqlConnectionPool connectionPool;
    private final ExecutorService executor;

    public SqlSettingLoader(ExecutorService executor, SqlConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        this.executor = executor;
    }

    @Override
    protected CompletableFuture<Map<String, String>> loadImpl(UUID parentId) {
//        return connectionPool.create().selectFrom(PLAYER_SETTINGS)
//                .where(PLAYER_SETTINGS.UUID.eq(UUIDUtil.toBytes(parentId)))
//                .fetchAsync(executor)
//                .thenApply(records -> records.stream().collect(
//                        toMap(PlayerSettingsRecord::getNamespace, PlayerSettingsRecord::getId)))
//                .toCompletableFuture();

        CompletableFuture<Map<String, String>> future = new CompletableFuture<>();
        executor.submit(() -> {
            try (Connection conn = connectionPool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(RETRIEVE)) {
                ps.setString(1, parentId.toString().replaceAll("-", ""));

                try (ResultSet resultSet = ps.executeQuery()) {
                    Map<String, String> found = new HashMap<>();
                    while (resultSet.next()) {
                        String namespace = resultSet.getString("namespace");
                        String id = resultSet.getString("id");
                        found.put(namespace, id);
                    }
                    future.complete(found);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        return future;
    }

    @Override
    protected CompletableFuture<Void> saveImpl(UUID parentId, Map<String, String> toSave) {
//        DSLContext ctx = connectionPool.create();
//        byte[] idBytes = UUIDUtil.toBytes(parentId);
//        List<Insert> inserts = (List) toSave.entrySet().stream()
//                .map(e -> ctx.insertInto(PLAYER_SETTINGS)
//                        .set(PLAYER_SETTINGS.UUID, idBytes)
//                        .set(PLAYER_SETTINGS.NAMESPACE, e.getKey())
//                        .set(PLAYER_SETTINGS.ID, e.getValue()))
//                .toList();
//        CompletableFuture<Void> future = new CompletableFuture<>();
//        ctx.batch(inserts).executeAsync(executor)
//                .thenAccept(__ -> future.complete(null));
//        return future;

        CompletableFuture<Void> future = new CompletableFuture<>();
        executor.submit(() -> {
            try (Connection conn = connectionPool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(INSERT)) {
                for (Map.Entry<String, String> entry : toSave.entrySet()) {
                    String namespace = entry.getKey();
                    String id = entry.getValue();
                    ps.setString(1, parentId.toString().replaceAll("-", ""));
                    ps.setString(2, namespace);
                    ps.setString(3, id);
                    ps.addBatch();
                }

                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                e.printStackTrace();
                future.complete(null);
            }
        });

        return future;
    }
}
