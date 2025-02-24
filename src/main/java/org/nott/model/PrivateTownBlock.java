package org.nott.model;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nott.SimpleTownyProduct;
import org.nott.exception.ConfigWrongException;
import org.nott.model.Configuration;
import org.nott.model.abstracts.BaseBlock;
import org.nott.model.interfaces.Product;
import org.nott.utils.Messages;
import org.nott.utils.ProductUtils;

import java.util.List;
import java.util.logging.Level;

@Data
public class PrivateTownBlock extends BaseBlock implements Product {

    @Override
    public void doGain(Player player) {
        // do something
        SimpleTownyProduct.logger.info("Start gain in [%s] PrivateTownBlock For player: [%s]".formatted(this.getName(), player.getName()));
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Message message = instance.getMessage();
        TownyAPI towny = TownyAPI.getInstance();
        Resident resident = towny.getResident(player);
        Town town = resident.getTownOrNull();
        if(town == null){
            Messages.sendError(player, message.getNotInTown());
            return;
        }
        boolean isMayor = town.isMayor(resident);
        Configuration configuration = instance.getConfiguration();
        Location location = player.getLocation();
        boolean residentCanGain = configuration.isResidentCanGain();
        Town atTown = towny.getTown(location);
        TownBlock townBlock = towny.getTownBlock(location);
        boolean isOwnTown = town.equals(atTown);
        if(!residentCanGain && !isMayor && isOwnTown){
            SimpleTownyProduct.logger.log(Level.INFO, "Resident can't gain. Skip.");
            Messages.sendError(player,this.getName() + "-" + message.getResidentCantGain());
            return;
        }
        boolean gainPrivateNeedStandInBlock = configuration.isGainPrivateNeedStandInBlock();
        boolean gainPrivateNeedStandInTown = configuration.isGainPrivateNeedStandInTown();
        // TODO Maybe useless
        boolean gainPrivateNeedStandInNation = configuration.isGainPrivateNeedStandInNation();
        if(gainPrivateNeedStandInTown){
            if(atTown == null || !atTown.getName().equals(town.getName()) || !isOwnTown){
                SimpleTownyProduct.logger.log(Level.INFO, "Not in town. Skip.");
                Messages.sendError(player,this.getName() + "-" + message.getMustStandInTown());
                return;
            }
        }else if(gainPrivateNeedStandInBlock){
            if(townBlock == null || !isOwnTown || !townBlock.getType().getName().equals(this.getName())){
                SimpleTownyProduct.logger.log(Level.INFO, "Not a Block. Skip.");
                Messages.sendError(player,this.getName() + "-" + message.getMustStandInBlock());
                return;
            }
        }
        if(!isOwnTown){
            Messages.sendError(player,this.getName() + "-" + message.getMustInOwnTown());
            return;
        }
        // 判断是否在冷却中
        if(ProductUtils.isInCoolDown(town.getUUID().toString(), this)){
            SimpleTownyProduct.logger.log(Level.INFO, "In cool down. Skip.");
            return;
        }
        try {
            List<String> actuallyCommand = ProductUtils.formatBlockCommands(this, town);
            ProductUtils.executeCommand(player, this, actuallyCommand);
            Messages.send(player, message.getSuccessGainProduct().formatted(this.getName()));
            ProductUtils.addCoolDown4Town(town, this);
        } catch (ConfigWrongException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doSteal(Player player) {
        super.doSteal(player);
    }
}
