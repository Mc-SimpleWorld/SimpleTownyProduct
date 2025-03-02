package org.nott.model;

import lombok.Data;
import org.nott.SimpleTownyProduct;
import org.nott.utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * @author Nott
 * @date 2025-2-21
 */
@Data
public class Message {

    private String pluginsDescription;

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

    private String mustStandInTown;

    private String mustStandInBlock;

    private String mustInOwnTown;

    private String noSpecialBlock;

    private String currentTown;

    private String clickToGain;

    private String specialBlock;

    private String specialType;

    private String whetherCoolDown;

    private String publicType;

    private String privateType;

    private String coolDown;

    private String productStorage;

    private String unCoolDown;

    private String gainCommandHover;

    private String notInAnyTown;

    private String notOnAnyBlock;

    private String successGainProduct;

    private String residentCantGain;

    private String checkConfigCorrect;

    private String confirmToSteal;

    private String confirmToStealTown;

    private String giveUpSteal;

    private String publicBlockCantSteal;

    private String targetCoolingDown;

    private String waitForNextSteal;

    private String notOpenSteal;

    private String stealProgressTitle;

    private String yourTownBeStealing;

    private String notAllowStealOwnTown;

    private String notAllowStealNationTown;

    private String thiefOutTownWarning;

    private String startStealBlockTitle;

    private String startStealBlockSubTitle;

    private String stealStillJailed;

    private String stealFailDeath;

    private String stealInterruptForOut;

    private String stealFailTitle;

    private String stealSuccessTitle;

    private String stealInterruptMessage;

    private String stealSuccessSubTitle;

    private String beStolenWarning;

    private List<String> commandHelp;

    private List<String> commandAdminHelp;


    public void load() throws Exception {
        SimpleTownyProduct.logger.info("Loading messages...");
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Configuration configuration = instance.getConfiguration();
        String locale = configuration.getLocale();
        File file = new File(instance.getDataFolder() + File.separator + "language" + File.separator + "message_" + locale + ".yml");
        Message message;
        if (file.exists()) {
            message = FileUtils.loadYamlFile(file.getPath(), Message.class);
        } else {
            file = new File(instance.getDataFolder() + File.separator + "language" + File.separator + "message_en_US.yml");
            if (file.exists()) {
                message = FileUtils.loadYamlFile(file.getPath(), Message.class);
            } else {
                throw new RuntimeException("No default message file found.");
            }
        }
        instance.setMessage(message);
        SimpleTownyProduct.logger.info("Configuration loaded.");
    }
}
