package org.nott.listener;

import com.palmergames.adventure.text.Component;
import com.palmergames.adventure.text.format.NamedTextColor;
import com.palmergames.adventure.text.format.TextColor;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.PlotChangeTypeEvent;
import com.palmergames.bukkit.towny.event.PlotPreChangeTypeEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nott.SimpleTownyProduct;
import org.nott.exception.ProductException;
import org.nott.model.Message;
import org.nott.model.block.PlayerPlotBlock;
import org.nott.model.data.SpecialBlockData;
import org.nott.model.data.TownSpecialBlockData;
import org.nott.utils.Messages;
import org.nott.utils.ProductUtils;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Nott
 * @date 2025-2-21
 */
public class TownyEventListener implements Listener {

    @EventHandler
    public void onNewTownEvent(NewTownEvent newTownEvent){
        String uid = newTownEvent.getTown().getUUID().toString();
        TownSpecialBlockData data = TownSpecialBlockData.empty(newTownEvent.getTown());
        SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.put(uid, data);
    }

    @EventHandler
    public void onPrePlotChangeEvent(PlotPreChangeTypeEvent event) throws NotRegisteredException {
        Logger logger = SimpleTownyProduct.logger;
        logger.log(Level.INFO, "PlotPreChangeTypeEvent fired.");
        logger.log(Level.INFO, "New type: " + event.getNewType().getName());
        TownBlockType oldType = event.getOldType();
        logger.log(Level.INFO, "Old type: " + oldType.getName());
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Message message = instance.getMessage();
        TownBlockType newType = event.getNewType();
        String name = newType.getName();
        String oldTypeName = oldType.getName();
        Resident resident = event.getResident();
        Town town = resident.getTown();
        String uuid = town.getUUID().toString();
        PlayerPlotBlock targetBlockType = ProductUtils.findSpecialTownBlock(name);
        PlayerPlotBlock fromBlockType = ProductUtils.findSpecialTownBlock(oldTypeName);
        TownSpecialBlockData specialBlockData = SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.get(uuid);
        List<String> alreadyHasTypes = specialBlockData.getSpecialBlocks().stream().map(SpecialBlockData::getType).toList();
        if (targetBlockType == null && fromBlockType == null) {
            return;
        }
        try {
            // 判断新特殊方块是否中立
            if (targetBlockType != null && targetBlockType.isPublic() && !resident.isAdmin()) {
                throw new ProductException(Messages.format(message.getCannotClaimNeutral(), name));
            }
            if (fromBlockType == null || targetBlockType != null) {
                // 判断新特殊方块是否有互斥类型
                List<String> repelBlockType = targetBlockType.getBlock().getRepelBlockType();
                if (!repelBlockType.isEmpty()) {
                    for (String repelType : repelBlockType) {
                        if (alreadyHasTypes.contains(repelType)) {
                            // 有互斥类型，发送消息并取消事件
                            throw new ProductException(Messages.format(message.getAlreadyHaveRepelBlock(), name, repelType));
                        }
                    }
                }

                // 判断新特殊方块是否达到最大限制
                Integer limitPerTown = targetBlockType.getBlock().getLimitPerTown();
                if (limitPerTown != -1) {
                    int count = specialBlockData.getSpecialBlocks().size();
                    if (count >= limitPerTown) {
                        throw new ProductException(Messages.format(message.getBlockReachLimit(), limitPerTown));
                    }
                }
                // 判断新特殊方块是否达到同盟最大限制
                Integer sameNationMax = targetBlockType.getBlock().getSameNationMax();
                if (sameNationMax != -1) {
                    Nation nation = town.getNationOrNull();
                    if (nation == null) {
                        return;
                    }
                    List<Town> towns = nation.getTowns();
                    int count = 0;
                    for (Town child : towns) {
                        String childId = child.getUUID().toString();
                        if (SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.containsKey(childId)) {
                            TownSpecialBlockData data = SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.get(childId);
                            count += data.getSpecialBlocks().size();
                        }
                    }
                    if (count >= sameNationMax) {
                        throw new ProductException(Messages.format(message.getNationBlockReachLimit(), sameNationMax));
                    }
                }
            }
        } catch (ProductException e) {
            event.setCancelMessage(e.getMessage());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void PlotChangeEvent(PlotChangeTypeEvent event) throws Exception {
        SimpleTownyProduct.logger.log(Level.INFO, "PlotChangeEvent fired.");
        TownBlockType newType = event.getNewType();
        TownBlockType oldType = event.getOldType();
        TownBlock townBlock = event.getTownBlock();
        String name = newType.getName();
        PlayerPlotBlock newBlock = ProductUtils.findSpecialTownBlock(name);
        PlayerPlotBlock oldBlock = ProductUtils.findSpecialTownBlock(oldType.getName());
        if (newBlock == null && oldBlock == null) {
            return;
        }
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Message message = instance.getMessage();
        Town town = townBlock.getTown();
        Resident mayor = town.getMayor();
        mayor.sendActionBar(Component.text(message.getSendCommandToCheck() + TextColor.color(NamedTextColor.GREEN.green())));
        if(newBlock != null){
            ProductUtils.addSbData(newBlock.getBlock(), town, townBlock);
        }
        if(oldBlock != null){
            ProductUtils.removeSbData(oldBlock.getBlock(), town, townBlock);
        }
    }
}
