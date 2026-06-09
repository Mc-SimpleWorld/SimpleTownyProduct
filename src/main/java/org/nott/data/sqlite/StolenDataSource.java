package org.nott.data.sqlite;

import org.nott.time.Timer;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class StolenDataSource implements SQLiteDataSource {

    @Override
    public String getTableName() {
        return "stolen";
    }

    @Override
    public String getCreateTableSQL() {
        return "CREATE TABLE IF NOT EXISTS stolen (" +
                "key TEXT PRIMARY KEY," +
                "value TEXT NOT NULL" +
                ")";
    }

    @Override
    public Map<String, String> getDataInMemory() {
        HashMap<String, String> data = new HashMap<>();
        for (String uuid : Timer.lostProductTownMap.keySet()) {
            Long rate = Timer.lostProductTownMap.get(uuid);
            if (rate > 0) {
                data.put(uuid, rate + "");
            }
        }
        return data;
    }

    @Override
    public void putDataToMemory(Map<String, String> data) throws SQLException {
        for (String key : data.keySet()) {
            String value = data.get(key);
            Timer.lostProductTownMap.put(key, Long.parseLong(value));
        }
    }

    @Override
    public void insertOrUpdate(String key, String value) throws SQLException {
        // Not needed for batch operations
    }

    @Override
    public void delete(String key) throws SQLException {
        // Not needed for batch operations
    }
}