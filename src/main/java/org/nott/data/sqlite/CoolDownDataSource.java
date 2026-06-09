package org.nott.data.sqlite;

import com.google.gson.Gson;
import org.nott.SimpleTownyProduct;
import org.nott.time.Timer;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CoolDownDataSource implements SQLiteDataSource {

    @Override
    public String getTableName() {
        return "cool_down";
    }

    @Override
    public String getCreateTableSQL() {
        return "CREATE TABLE IF NOT EXISTS cool_down (" +
                "key TEXT PRIMARY KEY," +
                "value TEXT NOT NULL" +
                ")";
    }

    @Override
    public Map<String, String> getDataInMemory() {
        HashMap<String, String> data = new HashMap<>();
        for (String uuid : Timer.timerMap.keySet()) {
            Timer timer = Timer.timerMap.get(uuid);
            long endTime = timer.getEndTime();
            long period = endTime - System.currentTimeMillis();
            if (period > 0) {
                data.put(uuid, period + "");
            }
        }
        return data;
    }

    @Override
    public void putDataToMemory(Map<String, String> data) throws SQLException {
        for (String key : data.keySet()) {
            String value = data.get(key);
            Timer timer = new Timer(key, Long.parseLong(value));
            timer.start();
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