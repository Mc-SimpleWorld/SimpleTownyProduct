package org.nott.data.file;

import org.nott.SimpleTownyProduct;
import org.nott.model.Configuration;
import org.nott.time.Timer;
import org.nott.utils.FileUtils;

import java.io.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Nott
 * @date 2025-2-26
 */
public class DataFileHandler implements DataHandler<Map<String, String>, File> {

    private final File file;


    // todo 多文件注册
//    private final Map dataSource;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public DataFileHandler(File file) {
        this.file = file;
    }


    @Override
    public Map<String, String> read() {
        SimpleTownyProduct.logger.info(this.file.getName() + "Read data Start...");
        lock.readLock().lock();
        try {
            SimpleTownyProduct.logger.info(this.file.getName() + "Read data End...");
            return FileUtils.readKeyValueFile(file);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void write(Map<String, String> d) {
        SimpleTownyProduct.logger.info("Write data Start...");
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
        SimpleTownyProduct.logger.info("Write data finish...");
    }

    @Override
    public void runOnBackground() {
        Configuration configuration = SimpleTownyProduct.INSTANCE.getConfiguration();
        Integer backUp = configuration.getDataBase().getBackUp();
        if (backUp <= 0) {
            return;
        }
        while (true) {
            this.saveData();
            try {
                Thread.sleep(Duration.ofMinutes(backUp));
            } catch (InterruptedException e) {
                SimpleTownyProduct.logger.severe(e.getMessage());
            }
        }
    }

    @Override
    public void saveOnShutDown() {
        saveData();
        SimpleTownyProduct.logger.info(this.file.getName() + "Store data successfully..");
    }

    private void saveData() {
        Map<String, String> data = this.read();
        if (data.isEmpty()) {
            data = new HashMap<>();
        }
        if (file.getName().contains("cooldown")) {
            for (String uuid : Timer.timerMap.keySet()) {
                Timer timer = Timer.timerMap.get(uuid);
                long endTime = timer.getEndTime();
                long period = endTime - System.currentTimeMillis();
                if (period > 0) {
                    data.put(uuid, period + "");
                }
            }
        }
        if (file.getName().contains("stolen")) {
            for (String uuid : Timer.lostProductTownMap.keySet()) {
                Long rate = Timer.lostProductTownMap.get(uuid);
                if (rate > 0) {
                    data.put(uuid, rate + "");

                }
            }
        }

        this.write(data);
    }

    @Override
    public void runOnStart() {
        Map<String, String> read = this.read();
        if (file.getName().contains("cooldown")) {
            for (String key : read.keySet()) {
                String data = read.get(key);
                Timer timer = new Timer(key, Long.parseLong(data));
                timer.start();
            }
        }
        if (file.getName().contains("stolen")) {
            for (String key : read.keySet()) {
                String data = read.get(key);
                Timer.lostProductTownMap.put(key, Long.parseLong(data));
            }
        }
        SimpleTownyProduct.logger.info(this.file.getName() + "Set up data successfully..");
    }
}
