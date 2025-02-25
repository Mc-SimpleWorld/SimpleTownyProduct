package org.nott.listener;

import com.palmergames.adventure.text.Component;
import com.palmergames.adventure.text.format.NamedTextColor;
import com.palmergames.adventure.text.format.TextColor;
import com.palmergames.bukkit.towny.event.PlotChangeTypeEvent;
import com.palmergames.bukkit.towny.event.PlotPreChangeTypeEvent;
import com.palmergames.bukkit.towny.event.plot.PlotSetForSaleEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nott.SimpleTownyProduct;
import org.nott.model.Message;
import org.nott.model.PlayerPlotBlock;
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
    public void onPrePlotChangeEvent(PlotPreChangeTypeEvent event) throws NotRegisteredException {
        Logger logger = SimpleTownyProduct.logger;
        logger.log(Level.INFO, "PlotPreChangeTypeEvent fired.");
        logger.log(Level.INFO, "New type: " + event.getNewType().getName());
        logger.log(Level.INFO, "Old type: " + event.getOldType().getName());
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Message message = instance.getMessage();
        TownBlockType newType = event.getNewType();
        String name = newType.getName();
        Resident resident = event.getResident();
        Town town = resident.getTown();
        List<String> alreadyHasTypes = town.getTownBlocks().stream().map(townBlock -> townBlock.getType().getName()).distinct().collect(Collectors.toList());
        PlayerPlotBlock targetBlockType = ProductUtils.findSpecialTownBlock(name);
        if(targetBlockType == null){
            return;
        }
        // 判断新特殊方块是否中立
        if(targetBlockType.isPublic() && !resident.isAdmin()){
            event.setCancelMessage(Messages.format(message.getCannotClaimNeutral(), name));
            event.setCancelled(true);
            return;
        }

        // 判断新特殊方块是否有互斥类型
        List<String> repelBlockType = targetBlockType.getBlock().getRepelBlockType();
        if(!repelBlockType.isEmpty()){
            for (String repelType : repelBlockType) {
                if(alreadyHasTypes.contains(repelType)){
                    // 有互斥类型，发送消息并取消事件
                    event.setCancelMessage(Messages.format(message.getAlreadyHaveRepelBlock(), name, repelType));
                    event.setCancelled(true);
                    return;
                }
            }
        }
        // 判断新特殊方块是否达到最大限制
        Integer limitPerTown = targetBlockType.getBlock().getLimitPerTown();
        if(limitPerTown != -1){
            int count = (int) town.getTownBlocks().stream().filter(townBlock -> townBlock.getType().getName().equals(name)).count();
            if(count >= limitPerTown){
                event.setCancelMessage(Messages.format(message.getBlockReachLimit(), limitPerTown));
                event.setCancelled(true);
                return;
            }
        }
        // 判断新特殊方块是否达到同盟最大限制
        Integer sameNationMax = targetBlockType.getBlock().getSameNationMax();
        if(sameNationMax != -1){
            Nation nation = town.getNationOrNull();
            if(nation == null){
                return;
            }
            List<Town> towns = nation.getTowns();
            List<Collection<TownBlock>> collect = towns.stream().map(Town::getTownBlocks).toList();
            int count = (int) collect.stream().flatMap(Collection::stream).filter(townBlock -> townBlock.getType().getName().equals(name)).count();
            if(count >= sameNationMax) {
                event.setCancelMessage(Messages.format(message.getNationBlockReachLimit(), sameNationMax));
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void PlotChangeEvent(PlotChangeTypeEvent event) throws Exception{
        SimpleTownyProduct.logger.log(Level.INFO, "PlotChangeEvent fired.");
        TownBlockType newType = event.getNewType();
        String name = newType.getName();
        PlayerPlotBlock block = ProductUtils.findSpecialTownBlock(name);
        if(block == null){
            return;
        }
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Message message = instance.getMessage();
        Town town = event.getTownBlock().getTown();
        Resident mayor = town.getMayor();
        mayor.sendActionBar(Component.text(message.getSendCommandToCheck() + TextColor.color(NamedTextColor.GREEN.green())));
    }
}
