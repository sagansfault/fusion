package com.projecki.fusion.statistic;

import com.google.common.base.Strings;
import com.projecki.fusion.sql.SqlConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class SqlStatisticLoader extends StatisticLoader {

    private final SqlConnectionPool connectionPool;
    private final ExecutorService executor;

    public SqlStatisticLoader(ExecutorService executor, SqlConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        this.executor = executor;
    }

    @Override
    protected CompletableFuture<Map<String, Long>> loadImpl(UUID parentId, String namespace) {
        CompletableFuture<Map<String, Long>> future = new CompletableFuture<>();

        executor.submit(() -> {
            final String retrieve = getRetrieve(namespace);
            try (Connection conn = connectionPool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(retrieve)) {
                ps.setString(1, parentId.toString().replaceAll("-", ""));

                try (ResultSet rs = ps.executeQuery()) {
                    Map<String, Long> found = new HashMap<>();
                    while (rs.next()) {
                        ResultSetMetaData rsMetaData = rs.getMetaData();
                        int columns = rsMetaData.getColumnCount();
                        for (int i = 0; i < columns; i++) {
                            int index = i + 1; // these index starting at 1
                            String columnName = rsMetaData.getColumnName(index);
                            Long value = rs.getLong(columnName);
                            found.put(columnName, value);
                        }
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
    protected CompletableFuture<Void> saveImpl(UUID parentId, String namespace, Map<String, Long> statistics) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        final String insert = getInsert(namespace, statistics);

        executor.submit(() -> {
            try (Connection conn = connectionPool.getConnection();
                 PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setString(1, parentId.toString().replaceAll("-", ""));

                int columnIndex = 2;
                for (Map.Entry<String, Long> entry : statistics.entrySet()) {
                    Long value = entry.getValue();
                    ps.setLong(columnIndex, value);
                    columnIndex++;
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

    private String getRetrieve(String namespace) {
        return "SELECT * FROM " + namespace + " WHERE uuid=UNHEX(?);";
    }

    private String getInsert(String namespace, Map<String, Long> statistics) {
        Set<String> keyset = statistics.keySet();
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append(namespace);
        builder.append(" (");
        builder.append(String.join(", ", keyset));
        builder.append(") VALUES (UNHEX(?)");
        builder.append(Strings.repeat(", ?", keyset.size()));
        builder.append(") ON DUPLICATE KEY UPDATE ");
        builder.append(keyset.stream().map(id -> id + "=VALUES(" + id + ")").collect(Collectors.joining(", ")));
        builder.append(";");
        return builder.toString();
    }
}
