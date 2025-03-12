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
import org.nott.model.data.BlockCoolDownData;
import org.nott.model.data.LostResourceData;
import org.nott.model.data.SpecialBlockData;
import org.nott.model.data.TownSpecialBlockData;
import org.nott.model.interfaces.Product;
import org.nott.time.TimePeriod;
import org.nott.time.Timer;
import org.nott.utils.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

@Data
public class PrivateTownBlock extends BaseBlock implements Product {

    @Override
    public void doGain(Player player) {
        SimpleTownyProduct.logger.info("Start gain in [%s] PrivateTownBlock For player: [%s]".formatted(this.getName(), player.getName()));
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Configuration configuration = instance.getConfiguration();
        Message message = instance.getMessage();
        TownyAPI towny = TownyAPI.getInstance();
        Resident resident = towny.getResident(player);
        Town town = resident.getTownOrNull();
        String townId = town.getUUID().toString();
        Location location = player.getLocation();
        TownBlock townBlock = towny.getTownBlock(location);
        Town atTown = towny.getTown(location);

        if(atTown == null){
            throw new ProductException(message.getNotInTown());
        }
        boolean isOwnTown = town.equals(atTown);
        boolean gainPrivateNeedStandInBlock = configuration.isGainPrivateNeedStandInBlock();
        boolean gainPrivateNeedStandInTown = configuration.isGainPrivateNeedStandInTown();
        if (!isOwnTown) {
            throw new ProductException(message.getMustInOwnTown());
        }
        List<TownBlock> townBlocks = new ArrayList<>();
        if (gainPrivateNeedStandInTown) {
            if (!atTown.getName().equals(town.getName())) {
                SimpleTownyProduct.logger.log(Level.INFO, "Not in town. Skip.");
                throw new ProductException(message.getMustStandInTown());
            }
            townBlocks.addAll(town.getTownBlocks());
        } else if (gainPrivateNeedStandInBlock) {
            if (townBlock == null || ProductUtils.isSpecialBlock(townBlock)) {
                SimpleTownyProduct.logger.log(Level.INFO, "Not a Block. Skip.");
                throw new ProductException(message.getMustStandInBlock());
            }
            townBlocks.add(townBlock);
        } else {
            throw new ProductException("Current not support other gain mode, except one: gainPrivateNeedStandInTown,gainPrivateNeedStandInTown");
        }

        try {
            TownSpecialBlockData data = SimpleTownyProduct.TOWN_SPECIAL_BLOCK_DATA_MAP.get(townId);

            LostResourceData lost = data.getLost(this.getName());
            SpecialBlockData specialBlockData = data.getSpecialBlocks().stream().filter(r -> this.getUid().equals(r.getBlockUuid())).findFirst().orElse(null);
            if(specialBlockData == null){
                return;
            }
            if(data.isCoolDown(specialBlockData.getType())) return;
            List<BlockCoolDownData> blockCoolDowns = data.getBlockCoolDowns();
            BlockCoolDownData blockCoolDownData = new BlockCoolDownData();
            blockCoolDownData.setBlockUuid(specialBlockData.getBlockUuid());
            blockCoolDownData.setCool(TimePeriod.fromStringGetVal(this.getGainCoolDown()));
            blockCoolDownData.setGainTime(CommonUtils.HHMMDDHMS.format(new Date()));
            blockCoolDownData.setGainPlayerUid(player.getUniqueId().toString());
            blockCoolDownData.setGainPlayerName(player.getName());
            blockCoolDowns.add(blockCoolDownData);
            Timer timer = new Timer(specialBlockData.getBlockUuid(), this.getGainCoolDown());
            timer.setTimerHandler(() -> {
                // 当冷却时间到时删除冷却数据
                blockCoolDowns.remove(blockCoolDownData);
            });
            timer.start();
            List<String> actuallyCommand = ProductUtils.formatBlockCommands(this, lost.getLostAmount());
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
        try {
            PlayerPlotBlock playerPlotBlock = ProductUtils.findSpecialFromLocation(player);
            if (playerPlotBlock == null) {
                Messages.sendError(player, message.getNoSpecialBlock());
                return;
            }
            BaseBlock block = playerPlotBlock.getBlock();
            Long capacity = ProductUtils.calculatedBlockCapacity(block, town);
            Double stealRate = configuration.getStealRate();
            long stolen = Math.round(capacity * stealRate);
            List<String> commands = ProductUtils.formatBlockCommands(block, stolen);
            ProductUtils.executeCommand(player, commands);
            // todo 设置城镇被偷窃冷却

            BukkitTools.fireEvent(new PlotStealEndEvent(town, stolen, player.getName(), this));
        } catch (Exception e) {
            Messages.sendError(player, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
