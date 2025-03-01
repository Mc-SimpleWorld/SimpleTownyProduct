package org.nott.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.nott.SimpleTownyProduct;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Nott
 * @date 2025-2-21
 */
public class FileUtils {

    static Logger logger = SimpleTownyProduct.logger;

    public static <T> T loadYamlFile(String path, Class<T> clazz) throws IOException {
        File file = new File(path);
        if(!file.exists()){
            logger.log(Level.SEVERE, "File not found: " + path);
            throw new IOException("File not found: " + path);
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.findAndRegisterModules();
        try {
            return mapper.readValue(file, clazz);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading yaml file", e);
            throw e;
        }
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

    public static void emptyTxt(File file) throws IOException {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
        } finally {
            if(fileWriter != null){
                fileWriter.close();
            }
        }
    }
}
