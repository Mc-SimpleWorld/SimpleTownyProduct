package org.nott.data.sqlite;

import org.nott.SimpleTownyProduct;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLiteHandlerRegistrar {

    private final List<SQLiteDataHandler> handlers = new ArrayList<>();

    private Connection connection;

    private String databasePath;

    private SQLiteHandlerRegistrar() {
    }

    public static SQLiteHandlerRegistrar Builder() {
        return new SQLiteHandlerRegistrar();
    }

    public SQLiteHandlerRegistrar setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
        return this;
    }

    public SQLiteHandlerRegistrar register(SQLiteDataHandler... handlers) {
        this.handlers.addAll(Arrays.asList(handlers));
        return this;
    }

    private void initConnection() throws SQLException {
        String jdbcUrl = "jdbc:sqlite:" + databasePath;
        this.connection = DriverManager.getConnection(jdbcUrl);
        this.connection.setAutoCommit(true);
        SimpleTownyProduct.logger.info("SQLite connection established: " + jdbcUrl);
    }

    public SQLiteHandlerRegistrar build() {
        try {
            initConnection();
            for (SQLiteDataHandler handler : handlers) {
                if (handler == null) {
                    throw new IllegalStateException("SQLiteDataHandler is not registered.");
                }
                handler.setConnection(connection);
                handler.runOnStart();
                SimpleTownyProduct.logger.info("SQLiteDataHandler has been successfully registered and initialized: " + handler.getClass().getName());
                new Thread(handler::runOnBackground).start();
            }
        } catch (SQLException e) {
            SimpleTownyProduct.logger.severe("Failed to initialize SQLite: " + e.getMessage());
            throw new RuntimeException("SQLite initialization failed", e);
        }
        return this;
    }

    public void end() {
        for (SQLiteDataHandler handler : handlers) {
            handler.saveOnShutDown();
        }
        if (connection != null) {
            try {
                connection.close();
                SimpleTownyProduct.logger.info("SQLite connection closed");
            } catch (SQLException e) {
                SimpleTownyProduct.logger.severe("Failed to close SQLite connection: " + e.getMessage());
            }
        }
    }
}