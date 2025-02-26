package org.nott.data.file;

import org.nott.SimpleTownyProduct;

import java.io.*;
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
    public void backUp(Map<String, String> d) {
        lock.writeLock().lock();
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

    }

    @Override
    public void saveOnShutDown() {

    }

    @Override
    public void runOnStart() {

    }
}
