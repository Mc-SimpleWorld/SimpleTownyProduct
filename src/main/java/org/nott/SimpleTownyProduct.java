package org.nott;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SimpleTownyProduct extends JavaPlugin {

    public static final String VERSION = "0.0.1";

    public static Logger logger;

    public static YamlConfiguration CONFIG;

    public static SimpleTownyProduct INSTANCE;

    public static BukkitAudiences MESSAGE_API;

    public static BukkitScheduler SCHEDULER;

    public static Economy ECONOMY;

    @Override
    public void onEnable() {
        // Plugin startup logic
        logger = getLogger();
        logger.info("SimpleTownyProduct starting...");
        this.loadConfig();
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
        logger.info("SimpleTownyProduct started.");
    }

    public void loadConfig() throws RuntimeException{
        saveDefaultConfig();
        YamlConfiguration config = new YamlConfiguration();
        File configFile = new File(this.getDataFolder() + File.separator + "config.yml");
        if(!configFile.exists()){
            saveResource("config.yml",false);
        }
        try {
            config.load(configFile);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error loading config file", e);
            throw new RuntimeException(e);
        }
        CONFIG = config;
        logger.info("Config loaded.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logger.info("SimpleTownyProduct already shut down...");
    }
}
