package org.nott.utils;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
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
import org.nott.model.block.PlayerPlotBlock;
import org.nott.model.block.PrivateTownBlock;
import org.nott.model.block.PublicTownBlock;
import org.nott.model.block.SpecialTownBlock;
import org.nott.model.data.BlockCoolDownData;
import org.nott.model.data.LostResourceData;
import org.nott.model.data.SpecialBlockData;
import org.nott.model.data.TownSpecialBlockData;
import org.nott.time.Timer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

    public static PlayerPlotBlock findSpecialFromLocation(Player player) throws Exception{
        TownyAPI townyAPI = TownyAPI.getInstance();
        Location location = player.getLocation();
        Town town = townyAPI.getTown(location);
        String uid = town.getUUID().toString();
        TownBlock townBlock = townyAPI.getTownBlock(location);
        String typeName = townBlock.getTypeName();
        SpecialTownBlock blockTypes = SimpleTownyProduct.INSTANCE.configuration.getBlockTypes();
        List<PrivateTownBlock> privates = blockTypes.getPrivates();
        PrivateTownBlock privateTownBlock = privates.stream().filter(publicTownBlock -> publicTownBlock.getName().equals(typeName)).findFirst().orElse(null);
        if(privateTownBlock != null){
            PlayerPlotBlock plotBlock = new PlayerPlotBlock(false, privateTownBlock);
            String uuId = plotBlock.getBlock().generateUUId(townBlock);
            TownSpecialBlockData data = SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.get(uid);
            if(data == null){
                return null;
            }
            plotBlock.setSpecialBlockData(data.getSpecialBlocks().stream().filter(sb -> sb.getBlockUuid().equals(uuId)).findFirst().get());
            plotBlock.setPublic(false);
            return plotBlock;
        }
        return null;

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

    public static boolean isPrivateBlockInCoolDown(Town town, BaseBlock block) {
        String townId = town.getUUID().toString();
        TownSpecialBlockData data = SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.get(townId);
        if (data == null) {
            return false;
        }
        String uid = block.getUid();
        return data.getBlockCoolDowns().stream().anyMatch(r -> r.getBlockUuid().equals(uid));
    }

    public static boolean isPublicBlockInCoolDown(Player player, BaseBlock block) {
        List<BlockCoolDownData> blockCoolDowns = SimpleTownyProduct.PUBLIC_SPECIAL_DATA.getBlockCoolDowns();
        String uid = block.getUid();
        String playerId = player.getUniqueId().toString();
        return blockCoolDowns.stream().filter(r -> uid.equals(r.getBlockUuid()) && playerId.equals(r.getGainPlayerUid()))
                .findFirst().orElse(null) != null;
    }

    public static void setCoolDown(String key, Long val) {
        Timer.timerMap.remove(key);
        Timer timer = Timer.timers.stream().filter(r -> "key".equals(r.getKey())).findFirst().orElse(null);
        if(timer != null){
            Timer.timers.remove(timer);
        }
        new Timer(key, val).start();
    }

    public static String stolenKey(BaseBlock block, Town town){
        return block.getName() + ":" + town.getUUID();
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

    public static List<PlayerPlotBlock> getSpecialBlockFromTownBlock(Town town, boolean needsPublic) {
        // 从TownBlock中获取注册的特殊Block
        String townId = town.getUUID().toString();
        Collection<TownBlock> townBlocks = town.getTownBlocks();
        Configuration configuration = SimpleTownyProduct.INSTANCE.getConfiguration();
        List<PrivateTownBlock> privates = configuration.getBlockTypes().getPrivates();
        TownSpecialBlockData data = SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.get(townId);
        List<SpecialBlockData> specialBlocks = data.getSpecialBlocks();
        List<PlayerPlotBlock> plotBlocks = new ArrayList<>();
        for (TownBlock townBlock : townBlocks) {
            String uuId = new PrivateTownBlock().generateUUId(townBlock);
            SpecialBlockData blockData = specialBlocks.stream().filter(sb -> uuId.equals(sb.getBlockUuid())).findFirst().orElse(null);
            if(blockData == null) continue;
            PrivateTownBlock privateTownBlock = privates.stream().filter(p -> blockData.getType().equals(p.getName())).findFirst().orElse(null);
            if(privateTownBlock == null) continue;
            privateTownBlock.setUid(uuId);
            plotBlocks.add(new PlayerPlotBlock(townBlock, false, privateTownBlock, blockData));
        }
        if(needsPublic){
            List<PublicTownBlock> publics = configuration.getBlockTypes().getPublics();
            List<SpecialBlockData> publicSpecialDataSpecialBlocks = SimpleTownyProduct.PUBLIC_SPECIAL_DATA.getSpecialBlocks();
            String townName = SimpleTownyProduct.PUBLIC_SPECIAL_DATA.getTownName();
            Town publicTown = TownyAPI.getInstance().getTown(townName);
            Collection<TownBlock> blocks = publicTown.getTownBlocks();
            for (TownBlock block : blocks) {
                String uuId = new PublicTownBlock().generateUUId(block);
                SpecialBlockData blockData = publicSpecialDataSpecialBlocks.stream().filter(sb -> uuId.equals(sb.getBlockUuid())).findFirst().orElse(null);
                if(blockData == null) continue;
                PublicTownBlock publicTownBlock  = publics.stream().filter(p -> blockData.getType().equals(p.getName())).findFirst().orElse(null);
                if(publicTownBlock == null) continue;
                publicTownBlock.setUid(uuId);
                plotBlocks.add(new PlayerPlotBlock(block, true, publicTownBlock, blockData));
            }
        }
        return plotBlocks;
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

    public static void addSbData(BaseBlock block, Town town, TownBlock townBlock){
        String townId = town.getUUID().toString();
        TownSpecialBlockData data = SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.containsKey(townId) ? SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.get(townId)
                : TownSpecialBlockData.empty(town);
        boolean isNeutral = block instanceof PublicTownBlock;
        SpecialBlockData specialBlockData = new SpecialBlockData();
        specialBlockData.setBlockUuid(block.generateUUId(townBlock));
        specialBlockData.setType(block.getName());
        specialBlockData.setNeutral(isNeutral);
        specialBlockData.setClaimFromOther(false);
        if(!isNeutral){
            List<SpecialBlockData> specialBlocks = data.getSpecialBlocks();
            specialBlocks.add(specialBlockData);
            SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.put(townId, data);
        }else {
            List<SpecialBlockData> specialBlocks = SimpleTownyProduct.PUBLIC_SPECIAL_DATA.getSpecialBlocks();
            specialBlocks.add(specialBlockData);
        }

    }

    public static void removeSbData(BaseBlock block, Town town, TownBlock townBlock){
        String townId = town.getUUID().toString();
        String uuId = block.generateUUId(townBlock);
        boolean isNeutral = block instanceof PublicTownBlock;
        TownSpecialBlockData data = isNeutral ? SimpleTownyProduct.PUBLIC_SPECIAL_DATA : SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.get(townId);
        List<SpecialBlockData> specialBlocks = data.getSpecialBlocks();
        SpecialBlockData specialBlockData = specialBlocks.stream().filter(sb -> sb.getBlockUuid().equals(uuId)).findFirst().orElse(null);
        if(specialBlockData != null){
            specialBlocks.remove(specialBlockData);
        }
    }

    public static BaseBlock getBaseBlockFromSbData(SpecialBlockData specialBlockData) {
        boolean neutral = specialBlockData.isNeutral();
        String type = specialBlockData.getType();
        BaseBlock block;
        if(neutral){
            List<PublicTownBlock> publics = SimpleTownyProduct.INSTANCE.getConfiguration().getBlockTypes().getPublics();
            block = publics.stream().filter(publicTownBlock -> type.equals(publicTownBlock.getName())).findAny().orElse(null);
        }else {
            List<PrivateTownBlock> privates = SimpleTownyProduct.INSTANCE.getConfiguration().getBlockTypes().getPrivates();
            block = privates.stream().filter(privateTownBlock -> type.equals(privateTownBlock.getName())).findFirst().orElse(null);
        }
        return block;
    }

    public static List<String> formatBlockCommands(PrivateTownBlock privateTownBlock, LostResourceData lost) {
        List<String> gainCommand = privateTownBlock.getGainCommand();
        if(lost == null){
            return gainCommand;
        }

        return null;
    }

    public static List<BaseBlock> findSbFromTownBlocks(Town town, Player player) {
        // 如果在中立城镇
        List<Resident> residents = town.getResidents();
        boolean onlyNeedNeutral = false;
        for (Resident resident : residents) {
            if (resident.hasPermissionNode("towny.product.neutral")) {
                onlyNeedNeutral = true;
                break;
            }
        }

        List<BaseBlock> baseBlocks = new ArrayList<>();

        if(onlyNeedNeutral){
            TownSpecialBlockData publicSpecialData = SimpleTownyProduct.PUBLIC_SPECIAL_DATA;
            findBaseBlockIfNotCd(player, baseBlocks, publicSpecialData);
        }else {
            TownSpecialBlockData privateSbData = SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.get(town.getUUID().toString());
            findBaseBlockIfNotCd(player, baseBlocks, privateSbData);
        }
        return baseBlocks;
    }

    private static void findBaseBlockIfNotCd(Player player, List<BaseBlock> baseBlocks, TownSpecialBlockData publicSpecialData) {
        List<SpecialBlockData> blocks = publicSpecialData.getSpecialBlocks();
        for (SpecialBlockData block : blocks) {
            BlockCoolDownData coolDownData = publicSpecialData.getBlockCoolDowns().stream()
                    .filter(r -> player.getUniqueId().toString().equals(r.getGainPlayerUid())
                            && block.getBlockUuid().equals(r.getBlockUuid()))
                    .findFirst().orElse(null);
            if(coolDownData == null){
                BaseBlock baseBlock = getBaseBlockFromSbData(block);
                baseBlock.setUid(block.getBlockUuid());
                baseBlocks.add(baseBlock);
            }
        }
    }
}
