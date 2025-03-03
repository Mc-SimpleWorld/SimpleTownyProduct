package org.nott.utils;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.nott.SimpleTownyProduct;
import org.nott.exception.ConfigWrongException;
import org.nott.model.*;
import org.nott.model.abstracts.BaseBlock;
import org.nott.time.TimePeriod;
import org.nott.time.Timer;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProductUtils {

    public static PlayerPlotBlock findSpecialTownBlock(String typeName){
        SpecialTownBlock blockTypes = SimpleTownyProduct.INSTANCE.configuration.getBlockTypes();
        List<PublicTownBlock> publics = blockTypes.getPublics();
        PublicTownBlock find = publics.stream().filter(publicTownBlock -> publicTownBlock.getName().equals(typeName)).findFirst().orElse(null);
        if(find != null){
            return new PlayerPlotBlock(true, find);
        }
        List<PrivateTownBlock> privates = blockTypes.getPrivates();
        PrivateTownBlock privateTownBlock = privates.stream().filter(privateTownBlock1 -> privateTownBlock1.getName().equals(typeName)).findFirst().orElse(null);
        if(privateTownBlock != null){
            return new PlayerPlotBlock(false, privateTownBlock);
        }
        return null;

    }

    public static PlayerPlotBlock findSpecialTownBlock(String typeName, Town town){
        PlayerPlotBlock block = findSpecialTownBlock(typeName);
        if(block == null){
            return null;
        }
        Collection<TownBlock> townBlocks = town.getTownBlocks();
        TownBlock townBlock = townBlocks.stream().filter(tb -> typeName.equals(tb.getTypeName())).findFirst().orElse(null);
        if(townBlock == null){
            return null;
        }
        return block;

    }

    public static Long calculatedBlockCapacity(BaseBlock block, Town town) throws ConfigWrongException{
        Integer baseGainNumber = block.getBaseGainNumber();
        if(baseGainNumber <= 0){
            throw new ConfigWrongException("Base Gain Number except > 0,get " + baseGainNumber);
        }
        Double townLevelExponent = block.getTownLevelExponent();
        if (townLevelExponent <= 0){
            townLevelExponent = 1D;
        }
        int levelNumber = town.getLevelNumber();
        double result = baseGainNumber * levelNumber * townLevelExponent;
        return result > 0.00D ? Math.round(result)  : 0L;
    }

    public static List<String> formatBlockCommands(BaseBlock block, Town town) throws ConfigWrongException {
        Long blockCapacity = calculatedBlockCapacity(block, town);
        if(Timer.lostProductTownMap.containsKey(town.getUUID().toString())){
            blockCapacity -= Timer.lostProductTownMap.get(town.getUUID().toString());
        }
        return formatBlockCommands(block, blockCapacity);
    }

    public static List<String> formatBlockCommands(BaseBlock block, long blockCapacity) throws ConfigWrongException {
        // 计算产能
        if(blockCapacity <= 0L){
            throw new ConfigWrongException("Calculated Block Capacity get 0,please check you product block config.");
        }
        return block.getGainCommand().stream().map(command -> command.replaceAll("\\{\\{PRODUCT_NUMBER}}", blockCapacity + "")).toList();
    }

    public static boolean isInCoolDown(String key) {
        return Timer.timerMap.containsKey(key);
    }

    public static void setCoolDown(String key, Long val) {
        Timer.timerMap.remove(key);
        Timer timer = Timer.timers.stream().filter(r -> "key".equals(r.getKey())).findFirst().orElse(null);
        if(timer != null){
            Timer.timers.remove(timer);
        }
        new Timer(key, val).start();
    }

    public static Long getCoolDown(String key) {
        if (!isInCoolDown(key)) {
            return 0L;
        }
        Timer timer = Timer.timerMap.get(key);
        return timer.getEndTime() - System.currentTimeMillis();
    }

    public static String stolenKey(BaseBlock block, Town town){
        return block.getName() + ":" + town.getUUID();
    }

    public static String stolenKey(Town town){
        return "stolen" + ":" + town.getUUID();
    }

    public static String playerKey(Player player){
        return player.getUniqueId().toString();
    }

    public static String stealActivityKey(Player player){
        return Timer.STEAL_KEY + player.getUniqueId();
    }

    public static String blockKey(BaseBlock block, Town town){
        return block.getName() + ":" + town.getUUID();
    }

    public static String publicBlockKey(BaseBlock block, Player player){
        return block.getName() + ":" + player.getUniqueId();
    }

    public static boolean isSpecialBlock(TownBlock townBlock){
        Configuration configuration = SimpleTownyProduct.INSTANCE.getConfiguration();
        SpecialTownBlock blockTypes = configuration.getBlockTypes();
        return blockTypes.getPrivates().stream().anyMatch(block -> block.getName().equals(townBlock.getTypeName())) ||
                blockTypes.getPublics().stream().anyMatch(block -> block.getName().equals(townBlock.getTypeName()));
    }

    public static void executeCommand(Player player, List<String> command) {
        command.forEach(s -> {
            String realCommand = PlaceholderAPI.setPlaceholders(player, s);
            if (realCommand.startsWith("[console]")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), realCommand.substring(9));
                return;
            }
            if (realCommand.startsWith("[player]")) {
                Bukkit.dispatchCommand(player, realCommand.substring(8));
                return;
            }

        });
    }

    public static void addCoolDown(String uuid, BaseBlock block) {
        Timer timer = new Timer(uuid, block.getGainCoolDown());
        timer.start();
    }

    public static void addCoolDown(String uuid, long cool) {
        Timer timer = new Timer(uuid, cool);
        timer.start();
    }

    public static List<PlayerPlotBlock> getSpecialBlockFromTownBlock(Collection<TownBlock> townBlocks, boolean needsPublic) {
        // 从TownBlock中获取注册的特殊Block
        Configuration configuration = SimpleTownyProduct.INSTANCE.getConfiguration();
        SpecialTownBlock blockTypes = configuration.getBlockTypes();
        List<PrivateTownBlock> privates = blockTypes.getPrivates();
        List<PlayerPlotBlock> plotBlockList = privates.stream().filter(sb -> townBlocks.stream().anyMatch(tb -> tb.getTypeName().equals(sb.getName())))
                .map(sb -> new PlayerPlotBlock(false, sb)).toList();
        List<PlayerPlotBlock> list = new ArrayList<>(plotBlockList);
        if(needsPublic){
            List<PublicTownBlock> publics = blockTypes.getPublics();
            list.addAll(publics.stream().map(sb -> new PlayerPlotBlock(true, sb)).toList());
        }
        return list;
    }

    public static PlayerPlotBlock getSpecialBlockPlayerLoc(Player player){
        TownyAPI townyAPI = TownyAPI.getInstance();
        Location location = player.getLocation();
        TownBlock townBlock = townyAPI.getTownBlock(location);
        if(townBlock == null){
            return null;
        }
        PlayerPlotBlock plotBlock = findSpecialTownBlock(townBlock.getTypeName());
        return plotBlock;
    }
}
