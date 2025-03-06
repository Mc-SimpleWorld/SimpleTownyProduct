package org.nott.data.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import org.nott.SimpleTownyProduct;
import org.nott.model.Configuration;
import org.nott.model.activity.StealActivity;
import org.nott.model.data.StealActivitiesData;
import org.nott.model.data.TownSpecialBlockData;
import org.nott.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

/**
 * @author Nott
 * @date 2025-3-5
 */
public class TownSbDataFileHandler implements DataHandler<TownSpecialBlockData, File> {

    String pbPath = SimpleTownyProduct.INSTANCE.getDataFolder() + File.separator + "data" + File.separator + "PublicBlock.yml";
    String saPath = SimpleTownyProduct.INSTANCE.getDataFolder() + File.separator + "data" + File.separator + "StealActivities.yml";

    @Override
    public TownSpecialBlockData read(File file) {
        try {
            return FileUtils.loadYamlFile(file.getPath(), TownSpecialBlockData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(TownSpecialBlockData d, File file) {
        if (!file.exists()) return;
        FileUtils.writeByYaml(d, file);
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

    private void saveData() {
        TownyAPI townyAPI = TownyAPI.getInstance();
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Set<String> keySet = SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.keySet();
        for (String townId : keySet) {
            Town town = townyAPI.getTown(UUID.fromString(townId));
            if (town == null) continue;
            String name = town.getName();
            File file = new File(instance.getDataFolder() + File.separator + "data" + File.separator + "town" + File.separator + name + ".yml");
            if (!file.exists()) {
                file.mkdirs();
            }
            TownSpecialBlockData data = SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.get(townId);
            this.write(data, file);
        }
        TownSpecialBlockData publicSpecialData = SimpleTownyProduct.PUBLIC_SPECIAL_DATA;
        this.write(publicSpecialData, new File(pbPath));
        // StealActivity data
        FileUtils.writeByYaml(SimpleTownyProduct.STEAL_ACTIVITIES, new File(saPath));
    }

    @Override
    public void saveOnShutDown() {
        this.saveData();
    }

    @Override
    public void runOnStart() {
        TownyAPI townyAPI = TownyAPI.getInstance();
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        List<Town> towns = townyAPI.getTowns();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        instance.saveResource(pbPath, false);
        instance.saveResource(saPath, false);
        File file = new File(instance.getDataFolder() + File.separator + "data" + File.separator + "town");
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            TownSpecialBlockData data;
            for (Town town : towns) {
                String name = town.getName();
                String uid = town.getUUID().toString();
                file = new File(instance.getDataFolder() + File.separator + "data" + File.separator + "town" + File.separator + name + ".yml");

//                if (!file.exists()) {
//                    file.mkdirs();
//                    data = TownSpecialBlockData.empty(town);
//                    mapper.writeValue(file, data);
//                    return;
//                }
                data = this.read(file);
                SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.put(uid, data);
            }

            file = new File(pbPath);
            data = mapper.readValue(file, TownSpecialBlockData.class);
            if (data != null) {
                SimpleTownyProduct.PUBLIC_SPECIAL_DATA = data;
            }
            // StealActivity data
            file = new File(saPath);
            List<StealActivitiesData> stealActivitiesData = mapper.readValue(file, List.class);
            if (stealActivitiesData != null) {
                SimpleTownyProduct.STEAL_ACTIVITIES.addAll(stealActivitiesData);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
