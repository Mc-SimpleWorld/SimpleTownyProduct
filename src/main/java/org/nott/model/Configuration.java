package org.nott.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.nott.SimpleTownyProduct;
import org.nott.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Nott
 * @date 2025-2-21
 */
@Data
public class Configuration {

    private String version;

    private Integer perTownTypes;

    private String prefix;

    private String locale;

    private boolean residentCanGain;

    private boolean residentCanTrade;

    private boolean gainNeedStandInBlock;

    private boolean gainNeedStandInTown;

    private boolean gainNeedStandInNation;

    private boolean canBePlundered;

    private Integer plunderGainTimes;

    private boolean haveNeutralityBlock;

    private SpecialTownBlock blockTypes;

    public void load() throws Exception {
        SimpleTownyProduct.logger.info("Loading configuration...");
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Configuration configuration = FileUtils.loadYamlFile(instance.getDataFolder() + File.separator + "config.yml", Configuration.class);
        instance.setConfiguration(configuration);
        SimpleTownyProduct.logger.info("Configuration loaded.");
    }
}
