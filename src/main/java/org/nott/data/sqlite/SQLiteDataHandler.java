package org.nott.data.sqlite;

import org.nott.SimpleTownyProduct;
import org.nott.model.Configuration;

import java.sql.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class SQLiteDataHandler {

    private final SQLiteDataSource dataSource;

    private Connection connection;

    private SQLiteDataHandler(SQLiteDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static SQLiteDataHandler build(SQLiteDataSource dataSource) {
        return new SQLiteDataHandler(dataSource);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        this.initTable();
    }

    private void initTable() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(dataSource.getCreateTableSQL());
            SimpleTownyProduct.logger.info("SQLite table " + dataSource.getTableName() + " initialized successfully");
        } catch (SQLException e) {
            SimpleTownyProduct.logger.severe("Failed to create table " + dataSource.getTableName() + ": " + e.getMessage());
        }
    }

    public Map<String, String> read() {
        Map<String, String> result = new HashMap<>();
        String sql = "SELECT key, value FROM " + dataSource.getTableName();
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            SimpleTownyProduct.logger.severe("Failed to read from " + dataSource.getTableName() + ": " + e.getMessage());
        }
        return result;
    }

    public void write(Map<String, String> data) {
        String deleteSql = "DELETE FROM " + dataSource.getTableName();
        String insertSql = "INSERT INTO " + dataSource.getTableName() + " (key, value) VALUES (?, ?)";
        try {
            connection.setAutoCommit(false);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(deleteSql);
            }
            try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    pstmt.setString(1, entry.getKey());
                    pstmt.setString(2, entry.getValue());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                SimpleTownyProduct.logger.severe("Failed to rollback transaction: " + ex.getMessage());
            }
            SimpleTownyProduct.logger.severe("Failed to write to " + dataSource.getTableName() + ": " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                SimpleTownyProduct.logger.severe("Failed to set auto commit: " + e.getMessage());
            }
        }
    }

    public void runOnBackground() {
        Configuration configuration = SimpleTownyProduct.INSTANCE.getConfiguration();
        Integer backUp = configuration.getDataBase().getBackUp();
        if (backUp <= 0) {
            return;
        }
        while (true) {
            saveData();
            try {
                Thread.sleep(Duration.ofMinutes(backUp));
            } catch (InterruptedException e) {
                SimpleTownyProduct.logger.severe(e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void saveOnShutDown() {
        saveData();
        SimpleTownyProduct.logger.info("SQLite table " + dataSource.getTableName() + " data stored successfully");
    }

    private void saveData() {
        Map<String, String> data = read();
        if (data == null) {
            data = new HashMap<>();
        }
        data.putAll(dataSource.getDataInMemory());
        write(data);
    }

    public void runOnStart() {
        Map<String, String> data = read();
        try {
            dataSource.putDataToMemory(data);
            SimpleTownyProduct.logger.info("SQLite table " + dataSource.getTableName() + " data loaded successfully");
        } catch (SQLException e) {
            SimpleTownyProduct.logger.severe("Failed to load data from " + dataSource.getTableName() + ": " + e.getMessage());
        }
    }

    public void insertOrUpdate(String key, String value) {
        String sql = "INSERT OR REPLACE INTO " + dataSource.getTableName() + " (key, value) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            SimpleTownyProduct.logger.severe("Failed to insert/update " + dataSource.getTableName() + ": " + e.getMessage());
        }
    }

    public void delete(String key) {
        String sql = "DELETE FROM " + dataSource.getTableName() + " WHERE key = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            SimpleTownyProduct.logger.severe("Failed to delete from " + dataSource.getTableName() + ": " + e.getMessage());
        }
    }
}