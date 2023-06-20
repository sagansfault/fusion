package com.projecki.fusion.currency.storage;

import com.projecki.fusion.currency.Currency;
import com.projecki.fusion.currency.CurrencyPair;
import com.projecki.fusion.sql.SqlConnectionPool;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLType;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SqlCurrencyStorage implements CurrencyStorage {

    private final SqlConnectionPool sqlConnectionPool;
    private final ExecutorService executor;
    private final String tableName;

    public SqlCurrencyStorage(@NotNull String tableName, @NotNull SqlConnectionPool sqlConnectionPool, @NotNull ExecutorService executor) {
        this.tableName = tableName;
        this.sqlConnectionPool = sqlConnectionPool;
        this.executor = executor;
    }

    public SqlCurrencyStorage(@NotNull String tableName, @NotNull SqlConnectionPool sqlConnectionPool) {
        this(tableName, sqlConnectionPool, new ThreadPoolExecutor(1, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>()));
    }


    /**
     * Get the stored balance for the player.
     * NOTE: Assume future will be executed async of tick loop.
     *
     * @param uuid the id of the balance to get
     * @return current balance stored
     */
    @Override
    public CompletableFuture<Long> getBalance(@NotNull Currency currency, @NotNull UUID uuid) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        String columnName = columnName(currency);

        executor.submit(() -> {

            try (Connection connection = sqlConnectionPool.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT " + columnName + " FROM " + tableName + " WHERE uuid = ?")) {

                    // populate statement
                    statement.setString(1, uuid.toString());

                    try (ResultSet resultSet = statement.executeQuery()) {
                        future.complete(resultSet.next() ? resultSet.getLong(columnName) : 0L);
                    }
                }
            } catch (SQLSyntaxErrorException exe) {
                sendSyntaxMessage(exe);
                future.completeExceptionally(exe);
            } catch (SQLException exe) {
                exe.printStackTrace();
                future.completeExceptionally(exe);
            }
        });

        return future;
    }

    /**
     * Set the player's stored balance to new value
     * <p>
     * WARNING: For most uses you should transact a player's balance to prevent possible data loss
     *
     * @param uuid      the id of the balance to update
     * @param newAmount the new balance for the player
     * @return a future that's completed once the balance update is completed
     */
    @Override
    public CompletableFuture<Void> setBalance(@NotNull Currency currency, @NotNull UUID uuid, long newAmount) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String columnName = columnName(currency);

        executor.submit(() -> {
            try (Connection connection = sqlConnectionPool.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tableName + " (uuid, " + columnName + ") VALUES(?, ?) ON DUPLICATE KEY UPDATE " + columnName + " = VALUES(" + columnName + ");")) {

                    statement.setString(1, uuid.toString());
                    statement.setLong(2, newAmount);

                    statement.executeUpdate();
                    future.complete(null);
                }
            } catch (SQLSyntaxErrorException exe) {
                sendSyntaxMessage(exe);
                future.completeExceptionally(exe);
            } catch (SQLException exe) {
                exe.printStackTrace();
                future.completeExceptionally(exe);
            }
        });

        return future;
    }

    /**
     * Increment or decrement a player's balance by a specified amount
     * <p>
     * This method is preferred for transactions over updateBalance, as it should only
     * apply increments or decrements in the backing storage as opposed to setting a new value
     *
     * @param uuid         id of user whose balance to update
     * @param amountChange increment or decrement change for balance
     * @return a future that's completed once the transaction is completed
     */
    @Override
    public CompletableFuture<Void> transact(@NotNull Currency currency, @NotNull UUID uuid, long amountChange) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String columnName = columnName(currency);

        executor.submit(() -> {
            try (Connection connection = sqlConnectionPool.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tableName + " (uuid, " + columnName + ") VALUES(?, ?) ON DUPLICATE KEY UPDATE " + columnName + " = " + columnName + " + VALUES(" + columnName + ");")) {

                    statement.setString(1, uuid.toString());
                    statement.setLong(2, amountChange);

                    statement.executeUpdate();
                    future.complete(null);
                }
            } catch (SQLSyntaxErrorException exe) {
                sendSyntaxMessage(exe);
                future.completeExceptionally(exe);
            } catch (SQLException exe) {
                exe.printStackTrace();
                future.completeExceptionally(exe);
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<Collection<CurrencyPair>> getCurrencies(@NotNull UUID uuid) {

        CompletableFuture<Collection<CurrencyPair>> future = new CompletableFuture<>();

        executor.submit(() -> {

            try (Connection connection = sqlConnectionPool.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE uuid = ?;")) {

                    // populate statement
                    statement.setString(1, uuid.toString());

                    try (ResultSet resultSet = statement.executeQuery()) {

                        if (!resultSet.next()) {
                            future.complete(List.of()); // return empty list
                            return;
                        }

                        List<CurrencyPair> currencies = new ArrayList<>();

                        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {

                            String name = resultSet.getMetaData().getColumnName(i);
                            if (resultSet.getMetaData().getColumnType(i) != Types.BIGINT) continue;

                            currencies.add(new CurrencyPair(name, resultSet.getLong(i)));
                        }

                        future.complete(currencies);
                    }
                }
            } catch (SQLSyntaxErrorException e) {
                sendSyntaxMessage(e);
                future.completeExceptionally(e);
            } catch (SQLException e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }

        });

        return future;
    }

    private void sendSyntaxMessage(SQLSyntaxErrorException exe) {
        Logger.getGlobal().warning("Fusion Currency: There was an SQL Syntax Error '" + exe.getMessage() +
                "'. Has the " + tableName + " table been set up?");
    }


    private String columnName(@NotNull Currency currency) {
        return currency.id();
    }

    @Override
    public void close() {
        sqlConnectionPool.shutdown();
        executor.shutdown();
    }
}
