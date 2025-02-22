package org.nott.model;

import lombok.Data;
import org.nott.SimpleTownyProduct;
import org.nott.utils.FileUtils;

import java.io.File;

/**
 * @author Nott
 * @date 2025-2-21
 */
@Data
public class Message {

    private String commonNoPermission;

    private String successClaimNewBlock;

    private String successSellBlock;

    private String failBalanceClaimBlock;

    private String notInTown;

    private String residentCanNotClaimBlock;

    private String residentCanNotGain;

    private String havePerTownTypes;

    private String havePerNationTypes;

    private String blockReachLimit;

    private String nationBlockReachLimit;

    private String cannotClaimNeutral;

    private String alreadyHaveRepelBlock;

    private String sendCommandToCheck;

    public void load() throws Exception{
        SimpleTownyProduct.logger.info("Loading messages...");
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Configuration configuration = instance.getConfiguration();
        String locale = configuration.getLocale();
        File file = new File(instance.getDataFolder() + File.separator + "language" + File.separator + "message_" + locale + ".yml");
        Message message;
        if(file.exists()){
            message = FileUtils.loadYamlFile(file.getPath(), Message.class);
        } else {
            file = new File(instance.getDataFolder() + File.separator + "language" + File.separator + "message_en_US.yml");
            if(file.exists()){
                message = FileUtils.loadYamlFile(file.getPath(), Message.class);
            } else {
                throw new RuntimeException("No default message file found.");
            }
        }
        instance.setMessage(message);
        SimpleTownyProduct.logger.info("Configuration loaded.");
    }
}
