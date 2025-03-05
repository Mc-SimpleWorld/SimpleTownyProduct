package org.nott.model.block;

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
import org.nott.exception.ProductException;
import org.nott.model.Configuration;
import org.nott.model.Message;
import org.nott.model.abstracts.BaseBlock;
import org.nott.model.data.LostResourceData;
import org.nott.model.data.TownSpecialBlockData;
import org.nott.model.interfaces.Product;
import org.nott.time.Timer;
import org.nott.utils.Messages;
import org.nott.utils.PermissionUtils;
import org.nott.utils.ProductUtils;
import org.nott.utils.TownyUtils;

import java.util.ArrayList;
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
        String townId = town.getUUID().toString();
        try {
            TownSpecialBlockData data = SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.get(townId);
            LostResourceData lost = data.getLost(this.getName());
            List<String> actuallyCommand = ProductUtils.formatBlockCommands(this, lost);
            ProductUtils.executeCommand(player, actuallyCommand);
            Messages.send(player, message.getSuccessGainProduct().formatted(this.getName()));
            BukkitTools.fireEvent(new PlotGainProductEvent(town, this, player));
        } catch (Exception e) {
            Messages.sendError(player, e.getMessage());
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
        if (playerPlotBlock == null) {
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
            Timer.lostProductTownMap.put(ProductUtils.stolenKey(this, town), stolen);
            BukkitTools.fireEvent(new PlotStealEndEvent(town, stolen, player.getName(), this));
        } catch (ConfigWrongException e) {
            throw new RuntimeException(e);
        }
    }
}
