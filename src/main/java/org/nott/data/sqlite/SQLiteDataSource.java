package org.nott.data.sqlite;

import java.sql.SQLException;
import java.util.Map;

public interface SQLiteDataSource {

    String getTableName();

    String getCreateTableSQL();

    Map<String, String> getDataInMemory();

    void putDataToMemory(Map<String, String> data) throws SQLException;

    void insertOrUpdate(String key, String value) throws SQLException;

    void delete(String key) throws SQLException;
}