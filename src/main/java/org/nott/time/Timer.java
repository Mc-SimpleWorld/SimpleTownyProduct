package org.nott.time;

import com.palmergames.bukkit.towny.object.Town;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nott.SimpleTownyProduct;
import org.nott.exception.ConfigWrongException;
import org.nott.exception.TimeFormatException;
import org.nott.model.StealActivity;

import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

@Data
public class Timer implements Comparable<Timer>{

    private String key;

    private long startTime;

    private long endTime;

    public static final String STEAL_KEY = "STEAL:";

    public static final PriorityBlockingQueue<Timer> timers = new PriorityBlockingQueue<>();

    public static final Map<String, Timer> timerMap = new ConcurrentHashMap<>();

    public static final Map<String, Long> lostProductTownMap = new ConcurrentHashMap<>();

    public static final Map<String, StealActivity> runningStealActivity = new ConcurrentHashMap<>();

    public static final Map<Player, List<Component>> email = new ConcurrentHashMap<>();

    public static void run(){
        run0();
    }

    public static void run0() {
        while (true){
            try {
                Timer frist = timers.peek();
                if(frist == null){
                    Thread.sleep(5 * 1000);
                    continue;
                }
                long currentTimeMillis = System.currentTimeMillis();
                long fristEndTime = frist.getEndTime();
                if(currentTimeMillis >= fristEndTime){
                    Timer take = timers.take();
                    timerMap.remove(take.getKey());
                }
            } catch (InterruptedException e) {
                SimpleTownyProduct.logger.severe("Timer run method interrupted: " + e.getMessage());
            }
        }
    }

    public Timer(String key, String coolDown) {
        this.key = key;
        this.startTime = System.currentTimeMillis();
        Long timeVal = 0L;
        try {
            timeVal = TimePeriod.fromStringGetVal(coolDown);
        } catch (ConfigWrongException e) {
            throw new TimeFormatException(e);
        }
        if(timeVal == 0){
            throw new TimeFormatException("Time period value is 0.");
        }
        this.endTime = timeVal + this.startTime;
    }

    public Timer(String key, long endTime) {
        this.key = key;
        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime + endTime;
    }

    public void start() {
        timers.add(this);
        timerMap.put(key, this);
    }


    @Override
    public int compareTo(@NotNull Timer o) {
        return Long.compare(this.endTime, o.endTime);
    }
}
