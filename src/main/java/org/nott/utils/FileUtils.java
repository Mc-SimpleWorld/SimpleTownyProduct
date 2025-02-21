package org.nott.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.nott.SimpleTownyProduct;
import java.io.File;
import java.io.IOException;
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
}
