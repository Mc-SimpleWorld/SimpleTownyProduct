package org.nott.utils;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.nott.SimpleTownyProduct;
import org.nott.model.*;
import org.nott.model.abstracts.BaseBlock;
import org.nott.time.Timer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProductUtils {

    public static PlayerPlotBlock findSpecialTownBlock(String name){
        SpecialTownBlock blockTypes = SimpleTownyProduct.INSTANCE.configuration.getBlockTypes();
        List<PublicTownBlock> publics = blockTypes.getPublics();
        PublicTownBlock find = publics.stream().filter(publicTownBlock -> publicTownBlock.getName().equals(name)).findFirst().orElse(null);
        if(find != null){
            return new PlayerPlotBlock(true, find);
        }
        List<PrivateTownBlock> privates = blockTypes.getPrivates();
        PrivateTownBlock privateTownBlock = privates.stream().filter(privateTownBlock1 -> privateTownBlock1.getName().equals(name)).findFirst().orElse(null);
        if(privateTownBlock != null){
            return new PlayerPlotBlock(false, privateTownBlock);
        }
        return null;

    }

    public static boolean isInCoolDown(String key, BaseBlock Block) {
        return Timer.timerMap.containsKey(key);
    }

    public static void executeCommand(Player player, BaseBlock block, List<String> command) {
        // do something
        SimpleTownyProduct.logger.info("doGain in PrivateTownBlock");
        command.forEach(s -> {
            if (s.startsWith("[console]")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.substring(9));
                return;
            }
            if (s.startsWith("[player]")) {
                Bukkit.dispatchCommand(player, s.substring(8));
                return;
            }

        });
    }

    public static void addCoolDown4Town(Town town, BaseBlock block) {
        Timer timer = new Timer(town.getUUID().toString() , block.getGainCoolDown());
        timer.start();
    }

    public static void addCoolDown(String uuid, PublicTownBlock publicTownBlock) {
        Timer timer = new Timer(uuid, publicTownBlock.getTradeCoolDown());
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
}
