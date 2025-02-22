package org.nott.time;

import java.util.concurrent.ConcurrentHashMap;

public class Timer {

    public static ConcurrentHashMap<String,Long> townProductTime = new ConcurrentHashMap<>();

    public static boolean addTownProductTime(String townName, long currentTime) {
        boolean result = false;
        townName = townName.toLowerCase();
        Long lastTime = townProductTime.get(townName);
        if (lastTime == null || lastTime < currentTime) {
            townProductTime.put(townName, currentTime);
            result = true;
        }
        return result;
    }


}
