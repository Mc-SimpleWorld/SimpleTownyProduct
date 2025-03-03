package org.nott;

import com.google.gson.Gson;
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
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.nott.command.ProductAdminCommand;
import org.nott.command.ProductCommand;
import org.nott.data.file.DataFileHandler;
import org.nott.data.file.DataHandlerRegistrar;
import org.nott.data.file.DataSource;
import org.nott.model.*;
import org.nott.model.abstracts.BaseBlock;
import org.nott.model.enums.DbTypeEnum;
import org.nott.time.Timer;
import org.nott.utils.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
@Setter
public final class SimpleTownyProduct extends JavaPlugin {

    public static String VERSION;

    public static Logger logger;

    public Configuration configuration;

    public Message message;

    public static SimpleTownyProduct INSTANCE;

    public static BukkitAudiences MESSAGE_API;

    public static BukkitScheduler SCHEDULER;

    public static Economy ECONOMY;

    public DataHandlerRegistrar dataHandlerRegistrar;

    public List<BukkitTask> tasks = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        logger = getLogger();
        logger.info("SimpleTownyProduct starting...");
        this.readVersionInfo();
        this.registerServices();
        this.loadConfigurationAndMessage();
        this.registerEvents();
        this.registerTownySubCommand();
        this.registerSpecialTownBlock();
        this.registerDataHandler();
        this.runBackTask();
        logger.info("SimpleTownyProduct started.");
    }

    private void runBackTask() {
        BukkitTask bukkitTask = SCHEDULER.runTaskAsynchronously(this, Timer::run);
        this.tasks.add(bukkitTask);
    }

    private void readVersionInfo() {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model read = reader.read(new FileReader("pom.xml"));
            VERSION = read.getVersion();
        } catch (Exception e) {
            logger.info("Read version failed: " + e.getMessage());
            this.onDisable();
        }
    }

    private void registerDataHandler() {
        String storageType = this.configuration.getDataBase().getStorageType();
        if (storageType.equalsIgnoreCase(DbTypeEnum.FILE.name())) {
            this.saveResource("data/cooldown.txt", false);
            this.saveResource("data/stolen.txt", false);
            this.saveResource("data/steal-activity.txt", false);
            String coolDownFilePath = this.getDataFolder() + File.separator + "data" + File.separator + "cooldown.txt";
            String stolenFilePath = this.getDataFolder() + File.separator + "data" + File.separator + "stolen.txt";
            String stealActivityPath = this.getDataFolder() + File.separator + "data" + File.separator + "steal-activity.txt";
            File coolDownFile = new File(coolDownFilePath);
            File stolenFile = new File(stolenFilePath);
            File stealActivityFile = new File(stealActivityPath);
            this.dataHandlerRegistrar = DataHandlerRegistrar.Builder()
                    .register(DataFileHandler.build(coolDownFile, new DataSource<>() {
                        @Override
                        public Map<String, String> getDataInMemory() {
                            HashMap<String, String> data = new HashMap<>();
                            for (String uuid : Timer.timerMap.keySet()) {
                                Timer timer = Timer.timerMap.get(uuid);
                                long endTime = timer.getEndTime();
                                long period = endTime - System.currentTimeMillis();
                                if (period > 0) {
                                    data.put(uuid, period + "");
                                }
                            }
                            return data;
                        }

                        @Override
                        public void putDataToMemory() {
                            Map<String, String> read = FileUtils.readByKeyValue(coolDownFile);
                            for (String key : read.keySet()) {
                                String data = read.get(key);
                                Timer timer = new Timer(key, Long.parseLong(data));
                                timer.start();
                            }
                        }
                    }))
                    .register(DataFileHandler.build(stolenFile, new DataSource<>() {
                        @Override
                        public Map<String, String> getDataInMemory() {
                            HashMap<String, String> data = new HashMap<>();
                            for (String uuid : Timer.lostProductTownMap.keySet()) {
                                Long rate = Timer.lostProductTownMap.get(uuid);
                                if (rate > 0) {
                                    data.put(uuid, rate + "");
                                }
                            }
                            return data;
                        }

                        @Override
                        public void putDataToMemory() {
                            Map<String, String> value = FileUtils.readByKeyValue(stolenFile);
                            for (String key : value.keySet()) {
                                String data = value.get(key);
                                Timer.lostProductTownMap.put(key, Long.parseLong(data));
                            }
                        }
                    }))
                    .register(DataFileHandler.build(stolenFile, new DataSource<>() {
                        @Override
                        public Map<String, String> getDataInMemory() {
                            HashMap<String, String> data = new HashMap<>();
                            for (String act : Timer.runningStealActivity.keySet()) {
                                data.put(act, new Gson().toJson(Timer.runningStealActivity.get(act)));
                            }
                            return data;
                        }

                        @Override
                        public void putDataToMemory() {
                            Map<String, String> value = FileUtils.readByKeyValue(stealActivityFile);
                            for (String key : value.keySet()) {
                                String activityJson = value.get(key);
                                Timer.runningStealActivity.put(key, new Gson().fromJson(activityJson, StealActivity.class));
                            }
                        }
                    }))
                    .build();
            logger.info("DataHandle Register success, type : [%s]".formatted(storageType));
        } else {
            throw new IllegalArgumentException("Not Support Other Storage Type except 'File'.");
        }
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
//        TownyCommandAddonAPI.addSubCommand(TownyCommandAddonAPI.CommandType.TOWN, "product", new ProductCommand());
        AddonCommand myCommand = new AddonCommand(TownyCommandAddonAPI.CommandType.TOWN, "product", new ProductCommand());
        // TODO fix tab complete
        myCommand.setTabCompletion(0, Arrays.asList("product"));
        myCommand.setTabCompletion(1, Arrays.asList("gain", "info", "trade"));
        TownyCommandAddonAPI.addSubCommand(myCommand);
        TownyCommandAddonAPI.addSubCommand(TownyCommandAddonAPI.CommandType.TOWNYADMIN, "product", new ProductAdminCommand());
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

    public void loadConfigurationAndMessage() {
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
        // 数据持久化
        if(this.dataHandlerRegistrar != null){
            this.dataHandlerRegistrar.end();
        }
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        logger.info("SimpleTownyProduct already shut down...");
    }

}
