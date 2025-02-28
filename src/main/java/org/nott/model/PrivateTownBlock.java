package org.nott.model;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.util.BukkitTools;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.nott.SimpleTownyProduct;
import org.nott.event.PlotGainProductEvent;
import org.nott.event.PlotStealEndEvent;
import org.nott.exception.ConfigWrongException;
import org.nott.model.abstracts.BaseBlock;
import org.nott.model.interfaces.Product;
import org.nott.time.TimePeriod;
import org.nott.time.Timer;
import org.nott.utils.Messages;
import org.nott.utils.ProductUtils;
import org.nott.utils.TownyUtils;

import java.util.List;
import java.util.logging.Level;

@Data
public class PrivateTownBlock extends BaseBlock implements Product {

    @Override
    public void doGain(Player player) {
        SimpleTownyProduct.logger.info("Start gain in [%s] PrivateTownBlock For player: [%s]".formatted(this.getName(), player.getName()));
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Message message = instance.getMessage();
        TownyAPI towny = TownyAPI.getInstance();
        Resident resident = towny.getResident(player);
        Town town = resident.getTownOrNull();
        String key = this.getName() + ":" + player.getUniqueId();
        if(town == null){
            Messages.sendError(player, message.getNotInTown());
            return;
        }
        Configuration configuration = instance.getConfiguration();
        Location location = player.getLocation();
        Town atTown = towny.getTown(location);
        TownBlock townBlock = towny.getTownBlock(location);
        boolean isOwnTown = town.equals(atTown);
        boolean gainPrivateNeedStandInBlock = configuration.isGainPrivateNeedStandInBlock();
        boolean gainPrivateNeedStandInTown = configuration.isGainPrivateNeedStandInTown();
        // TODO Maybe useless
        boolean gainPrivateNeedStandInNation = configuration.isGainPrivateNeedStandInNation();

        if(!isOwnTown){
            Messages.sendError(player,this.getName() + "-" + message.getMustInOwnTown());
            return;
        }

        if(gainPrivateNeedStandInTown){
            if(!atTown.getName().equals(town.getName())){
                SimpleTownyProduct.logger.log(Level.INFO, "Not in town. Skip.");
                Messages.sendError(player,this.getName() + "-" + message.getMustStandInTown());
                return;
            }
        }else if(gainPrivateNeedStandInBlock){
            if(townBlock == null || !townBlock.getType().getName().equals(this.getName())){
                SimpleTownyProduct.logger.log(Level.INFO, "Not a Block. Skip.");
                Messages.sendError(player,this.getName() + "-" + message.getMustStandInBlock());
                return;
            }
        } else {
            throw new RuntimeException("Current not support other gain mode, except one: gainPrivateNeedStandInTown,gainPrivateNeedStandInTown");
        }

        //  使用权限管理
        Messages.checkPermission(player, "towny.product.publicGain");
        // 判断是否在冷却中
        if(ProductUtils.isInCoolDown(key)){
            SimpleTownyProduct.logger.log(Level.INFO, "In cool down. Skip.");
            return;
        }
        try {
            List<String> actuallyCommand = ProductUtils.formatBlockCommands(this, town);
            ProductUtils.executeCommand(player, actuallyCommand);
            Messages.send(player, message.getSuccessGainProduct().formatted(this.getName()));
            BukkitTools.fireEvent(new PlotGainProductEvent(town, this, player));
            ProductUtils.addCoolDown(key, this);
        } catch (ConfigWrongException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beSteal(Player player) {
        Message message = SimpleTownyProduct.INSTANCE.getMessage();
        Configuration configuration = SimpleTownyProduct.INSTANCE.getConfiguration();
        TownyAPI townyAPI = TownyAPI.getInstance();
        Location location = player.getLocation();
        Town town = townyAPI.getTown(location);
        if (!TownyUtils.whetherInBlock(player)) {
            Messages.sendError(player, message.getNotOnAnyBlock());
            return;
        }
        PlayerPlotBlock playerPlotBlock = ProductUtils.getSpecialBlockPlayerLoc(player);
        if(playerPlotBlock == null){
            Messages.sendError(player, message.getNoSpecialBlock());
            return;
        }
        try {
            BaseBlock block = playerPlotBlock.getBlock();
            Long capacity = ProductUtils.calculatedBlockCapacity(block, town);
            Double stealRate = configuration.getStealRate();
            long stolen = Math.round(capacity * stealRate);
            List<String> commands = ProductUtils.formatBlockCommands(block, stolen);
            ProductUtils.executeCommand(player, commands);
            Timer.lostProductTownMap.put(town.getUUID().toString(), stolen);
            BukkitTools.fireEvent(new PlotStealEndEvent(town, stolen, player.getName(), this));
        } catch (ConfigWrongException e) {
            throw new RuntimeException(e);
        }
    }
}
