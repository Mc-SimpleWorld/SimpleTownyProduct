package org.nott.data.file;

import org.nott.SimpleTownyProduct;
import org.nott.model.Configuration;
import org.nott.utils.FileUtils;

import java.io.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nott
 * @date 2025-2-26
 */
public class DataFileHandler implements DataHandler<Map<String, String>, File> {

    private final File file;

    private final DataSource<Map<String, String>> dataSource;

    private DataFileHandler(File file, DataSource<Map<String, String>> dataSource) {
        this.file = file;
        this.dataSource = dataSource;
    }

    public static DataFileHandler build(File file, DataSource<Map<String, String>> dataSource){
        return new DataFileHandler(file, dataSource);
    }


    @Override
    public Map<String, String> read() {
        return FileUtils.readByKeyValue(file);
    }

    @Override
    public void write(Map<String, String> d) {
        FileUtils.writeByKeyValue(file, d);
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
        SimpleTownyProduct.logger.info(this.file.getName() + " Store data successfully..");
    }

    private void saveData() {
        Map<String, String> data = this.read();
        if (data.isEmpty()) {
            data = new HashMap<>();
        }
        data.putAll(this.dataSource.getDataInMemory());

        this.write(data);
    }

    @Override
    public void runOnStart() {
        this.dataSource.putDataToMemory();
        SimpleTownyProduct.logger.info(this.file.getName() + " Set up data successfully..");
    }
}
