package org.nott.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.nott.SimpleTownyProduct;
import org.nott.exception.VersionNotCorrectException;
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

    private boolean blockCanBeSteal;

    private Double stealRate;

    private String stealNeedStandInTime;

    private String stealCoolDown;

    private boolean stealNeedInBlock;

    private boolean gainPrivateNeedStandInBlock;

    private boolean gainPrivateNeedStandInTown;

    private boolean gainPrivateNeedStandInNation;

    private boolean canBePlundered;

    private Integer stealTempOutSecond;

    private Integer plunderGainTimes;

    private SpecialTownBlock blockTypes;

    public void load() throws Exception {
        SimpleTownyProduct.logger.info("Loading configuration...");
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Configuration configuration = FileUtils.loadYamlFile(instance.getDataFolder() + File.separator + "config.yml", Configuration.class);
        String versionInConfig = configuration.getVersion();
        if(!SimpleTownyProduct.VERSION.equals(versionInConfig)){
            throw new VersionNotCorrectException("Get Wrong plugins version except %s, get %s, dont change version in config.yml or check your plugins jar file.".formatted(SimpleTownyProduct.VERSION, versionInConfig));
        }
        instance.setConfiguration(configuration);
        SimpleTownyProduct.logger.info("Configuration loaded.");
    }
}
