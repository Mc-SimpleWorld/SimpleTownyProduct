package org.nott;

import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.AddonCommand;
import com.palmergames.bukkit.towny.object.TownBlockData;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownBlockTypeHandler;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.nott.command.ProductCommand;
import org.nott.model.Configuration;
import org.nott.model.Message;
import org.nott.model.SpecialTownBlock;
import org.nott.model.abstracts.BaseBlock;
import org.nott.model.PrivateTownBlock;
import org.nott.model.PublicTownBlock;
import org.nott.time.Timer;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
@Setter
public final class SimpleTownyProduct extends JavaPlugin {

    public static final String VERSION = "0.0.1";

    public static Logger logger;

    public Configuration configuration;

    public Message message;

    public static SimpleTownyProduct INSTANCE;

    public static BukkitAudiences MESSAGE_API;

    public static BukkitScheduler SCHEDULER;

    public static Economy ECONOMY;

    @Override
    public void onEnable() {
        // Plugin startup logic
        logger = getLogger();
        logger.info("SimpleTownyProduct starting...");
        this.registerServices();
        this.loadConfiguration();
        this.registerEvents();
        this.registerTownySubCommand();
        this.registerSpecialTownBlock();
        SCHEDULER.runTaskAsynchronously(this, Timer::run);
        logger.info("SimpleTownyProduct started.");
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new org.nott.listener.TownyEventListener(), this);
        this.getServer().getPluginManager().registerEvents(new org.nott.listener.BlockStealEventListener(), this);
    }

    private void registerSpecialTownBlock() {
        Configuration config = this.getConfiguration();
        SpecialTownBlock blockTypes = config.getBlockTypes();
        List<PrivateTownBlock> privates = blockTypes.getPrivates();
        List<PublicTownBlock> publics = blockTypes.getPublics();
        privates.forEach(SimpleTownyProduct::registerSubTownyBlockType);
        publics.forEach(SimpleTownyProduct::registerSubTownyBlockType);
    }

    private static void registerSubTownyBlockType(BaseBlock block) {
        if (TownBlockTypeHandler.exists(block.getName())) {
            logger.info("SimpleTownyProduct: TownBlockType " + block.getName() + " already exists.");
            return;
        }
        TownBlockType customPlot = new TownBlockType(block.getName(), new TownBlockData() {
            @Override
            public String getMapKey() {
                return block.getMapKey(); // A single character to be shown on the /towny map and /towny map hud
            }
            @Override
            public double getCost() {
                return block.getBasePrice();// A cost that will be paid to set the plot type.
            }
        });
        try {
            TownBlockTypeHandler.registerType(customPlot);
        } catch (TownyException e) {
            logger.severe(e.getMessage());
        }
    }

    private void registerTownySubCommand() {
        // Register Product command
        TownyCommandAddonAPI.addSubCommand(TownyCommandAddonAPI.CommandType.TOWN, "product", new ProductCommand());
        AddonCommand myCommand = new AddonCommand(TownyCommandAddonAPI.CommandType.TOWN, "product", new ProductCommand());
        myCommand.setTabCompletion(0, Arrays.asList("product", "p"));
        myCommand.setTabCompletion(1, Arrays.asList("doGain", "info", "trade"));
        TownyCommandAddonAPI.addSubCommand(myCommand);
    }

    private void registerServices() {
        INSTANCE = this;
        MESSAGE_API = BukkitAudiences.create(this);
        SCHEDULER = this.getServer().getScheduler();
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null){
            logger.severe("No economy plugin found. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        ECONOMY = rsp.getProvider();
    }

    public void loadConfiguration() {
        try {
            this.saveDefaultConfig();
            this.saveResource("config.yml", false);
            this.saveResource("language/message_en_US.yml", false);
            this.saveResource("language/message_zh_CN.yml", false);
            configuration = new Configuration();
            configuration.load();
            message = new Message();
            message.load();
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Error loading configuration", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logger.info("SimpleTownyProduct already shut down...");
    }

}
