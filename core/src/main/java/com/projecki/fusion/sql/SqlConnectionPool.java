package com.projecki.fusion.sql;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.projecki.fusion.FusionCore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jooq.*;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.MappedTable;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static org.jooq.impl.DSL.using;

/**
 * A wrapper for {@link DataSource} to easily create connection pools
 * with using Hikari
 */
public class SqlConnectionPool {

    private HikariDataSource dataSource;
    private final ExecutorService executor;
    private final Settings settings = new Settings().withExecuteLogging(false);

    /**
     * Create a new {@link HikariDataSource} whose config is specified in {@code config}
     *
     * @param config config for the internal {@link DataSource}
     * @throws IllegalArgumentException if the {@link DataSource} cannot be created sucessfully
     */
    public SqlConnectionPool(SqlConfig config) {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setConnectionTimeout(5 * 1000);

        try {
            this.dataSource = new HikariDataSource(hikariConfig);
        } catch (IllegalArgumentException e) {
            this.executor = null;
            this.dataSource = null;
            FusionCore.LOGGER.error("Incorrect hikariCP config. Sql url/credentials are likely wrong");
            return;
        }

        Map<String, String> mappings = config.getMappings();
        Optional<MappedTable> tableMap = Optional.ofNullable(config.getTablePrefix()).map(p ->
                new MappedTable()
                        .withInputExpression(Pattern.compile("(.+)"))
                        .withOutput(p + "$1")
        );

        if (mappings.isEmpty()) {
            tableMap.ifPresent(m -> this.settings.setRenderMapping(
                    new RenderMapping().withSchemata(new MappedSchema().withTables(m))));
        } else {

            RenderMapping renderMapping = new RenderMapping();
            List<MappedSchema> schemata = renderMapping.getSchemata();
            mappings.forEach((i, o) -> {
                MappedSchema schemaMap = new MappedSchema().withInput(i).withOutput(o);
                tableMap.ifPresent(schemaMap::withTables);
                schemata.add(schemaMap);
            });

            if (!schemata.isEmpty()) {
                this.settings.setRenderMapping(renderMapping);
            }
        }

        Map<String, Integer> executor = config.getExecutor();
        int corePoolSize = executor.getOrDefault("corePoolSize", 5);
        int maxPoolSize = executor.getOrDefault("maximumPoolSize", 5);
        int keepAliveTime = executor.getOrDefault("keepAliveTime", 60);
        this.executor = new ThreadPoolExecutor(
                corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder()
                        .setNameFormat("jOOQ - %d")
                        .setUncaughtExceptionHandler((t, e) -> e.printStackTrace())
                        .build()
        );
    }

    /**
     * Create a new {@link DSLContext} with a simple configuration using a
     * {@link DataSource data source}, {@link SQLDialect#MARIADB MariaDB}
     * and an asynchronous {@link Executor executor provider}.
     *
     * @return The newly created DSLContext instance.
     */
    public DSLContext create() {
        DSLContext context = using(dataSource, SQLDialect.MYSQL);
        context.configuration().set(executor).set(settings);
        return context;
    }

    /**
     * Gets a connection from the internal {@link DataSource}
     * <p>
     * <b>Ensure you close a connection after use</b>
     *
     * @return an SQL connection
     * @throws SQLException if internal {@link DataSource} throws one
     */
    @NotNull
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Shuts down the internal {@link DataSource} and it's associated pool
     */
    public void shutdown() {
        MoreExecutors.shutdownAndAwaitTermination(executor, 5, TimeUnit.MINUTES);
        this.dataSource.close();
    }

    /**
     * Execute a jOOQ task asynchronously using the dedicated MySQL
     * thread pool. This is used primarily for operations where jOOQ
     * does not provide an {@link Query#executeAsync()} method such
     * as with {@link Batch} or when {@link ResultQuery#fetchOne()}.
     * <p>
     * Note that this method should not be used for operations other
     * than those pertaining to jOOQ and database interactions.
     * <br>
     * Also, note that this should <i>not</i> be used for jOOQ operations
     * that will be performed asynchronously via {@link ResultQuery#fetchAsync()}
     * or {@link Query#executeAsync()} as this would be a pointless
     * waste of resources in duplicating the asynchronous processes.
     *
     * @param runnable The operation to perform on the MySQL dedicated thread.
     */
    public void executeAsync(Runnable runnable) {
        this.executor.execute(() -> {

            try {
                runnable.run();
            } catch (Throwable e) {
                FusionCore.LOGGER.error(e.getMessage(), e);
            }
        });
    }

    /**
     * Execute a jOOQ task asynchronously using the dedicated MySQL
     * thread pool. This is used primarily for operations where jOOQ
     * does not provide an {@link Query#executeAsync()} method such
     * as with {@link Batch} or when {@link ResultQuery#fetchOne()}.
     * <p>
     * Note that this method should not be used for operations other
     * than those pertaining to jOOQ and database interactions.
     * <br>
     * Also, note that this should <i>not</i> be used for jOOQ operations
     * that will be performed asynchronously via {@link ResultQuery#fetchAsync()}
     * or {@link Query#executeAsync()} as this would be a pointless
     * waste of resources in duplicating the asynchronous processes.
     *
     * @param consumer The operation to perform on the MySQL dedicated thread.
     */
    public void executeAsync(Consumer<DSLContext> consumer) {
        this.executor.execute(() -> {

            try {
                consumer.accept(this.create());
            } catch (Throwable e) {
                FusionCore.LOGGER.error(e.getMessage(), e);
            }
        });
    }
}
