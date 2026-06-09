package org.nott.data.sqlite;

import com.google.gson.Gson;
import org.nott.model.StealActivity;
import org.nott.time.Timer;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class StealActivityDataSource implements SQLiteDataSource {

    private static final Gson GSON = new Gson();

    @Override
    public String getTableName() {
        return "steal_activity";
    }

    @Override
    public String getCreateTableSQL() {
        return "CREATE TABLE IF NOT EXISTS steal_activity (" +
                "key TEXT PRIMARY KEY," +
                "value TEXT NOT NULL" +
                ")";
    }

    @Override
    public Map<String, String> getDataInMemory() {
        HashMap<String, String> data = new HashMap<>();
        for (String act : Timer.runningStealActivity.keySet()) {
            data.put(act, GSON.toJson(Timer.runningStealActivity.get(act)));
        }
        return data;
    }

    @Override
    public void putDataToMemory(Map<String, String> data) throws SQLException {
        for (String key : data.keySet()) {
            String activityJson = data.get(key);
            Timer.runningStealActivity.put(key, GSON.fromJson(activityJson, StealActivity.class));
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