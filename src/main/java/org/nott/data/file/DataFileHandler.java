package org.nott.data.file;

import org.nott.SimpleTownyProduct;
import org.nott.model.Configuration;
import org.nott.time.Timer;

import java.io.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Nott
 * @date 2025-2-26
 */
public class DataFileHandler implements DataHandler<Map<String,String>, File> {

    private final File file;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public DataFileHandler(File file) {
        this.file = file;
    }

    public static Map<String,String> readKeyValueFile(File file) {
        Map<String,String> kvMap = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null){
                String[] keyVal = line.split("=");
                if(keyVal.length == 2){
                    kvMap.put(keyVal[0].trim(), keyVal[1].trim());
                }
            }
        } catch (Exception e){
            SimpleTownyProduct.logger.severe(e.getMessage());
        }
        return kvMap;
    }


    @Override
    public Map<String, String> read() {
        lock.readLock().lock();
        try {
            return readKeyValueFile(file);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void write(Map<String, String> d) {
        lock.writeLock().lock();
        // TODO 判断是否需要清空原数据
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, String> entry : d.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            SimpleTownyProduct.logger.severe(e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void runOnBackground() {
        Configuration configuration = SimpleTownyProduct.INSTANCE.getConfiguration();
        Integer backUp = configuration.getDataBase().getBackUp();
        if(backUp == 0){
            return;
        }
        while (true){
            this.saveData();
            try {
                Thread.sleep(Duration.ofMinutes(backUp));
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @Override
    public void saveOnShutDown() {
        saveData();
        SimpleTownyProduct.logger.info("Store data successfully..");
    }

    private void saveData() {
        Map<String, String> data = this.read();
        if(data.isEmpty()){
            data = new HashMap<>();
        }
        for (String uuid : Timer.timerMap.keySet()) {
            Timer timer = Timer.timerMap.get(uuid);
            long endTime = timer.getEndTime();
            long startTime = timer.getStartTime();
            long period = endTime - startTime;
            data.put(uuid, period + "");
        }
        this.write(data);
    }

    @Override
    public void runOnStart() {
        Map<String, String> read = this.read();
        for (String key : read.keySet()) {
            String data = read.get(key);
            Timer timer = new Timer(key, Long.parseLong(data));
            timer.start();
        }
        SimpleTownyProduct.logger.info("Set up data successfully..");
    }
}
