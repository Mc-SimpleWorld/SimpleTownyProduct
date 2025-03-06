package org.nott.model.block;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.util.BukkitTools;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.nott.SimpleTownyProduct;
import org.nott.event.PlotGainProductEvent;
import org.nott.exception.ConfigWrongException;
import org.nott.exception.MethodNotSupportException;
import org.nott.exception.ProductException;
import org.nott.model.Message;
import org.nott.model.abstracts.BaseBlock;
import org.nott.model.data.BlockCoolDownData;
import org.nott.model.interfaces.Product;
import org.nott.time.Timer;
import org.nott.utils.CommonUtils;
import org.nott.utils.Messages;
import org.nott.utils.PermissionUtils;
import org.nott.utils.ProductUtils;

import java.util.Date;
import java.util.List;

@Data
public class PublicTownBlock extends BaseBlock implements Product {

    @Override
    public void doGain(Player player) {
        PermissionUtils.checkPermission(player, "towny.product.publicGain");
        SimpleTownyProduct.logger.info("%s starting to gain [%s] public product".formatted(player.getName(), this.getName()));
        TownyAPI towny = TownyAPI.getInstance();
        Location location = player.getLocation();
        TownBlock currentBlock = towny.getTownBlock(location);
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Message message = instance.getMessage();
        try {
            if (currentBlock == null) {
                SimpleTownyProduct.logger.info("Not a Block. Skip.");
                throw new ProductException(this.getName() + ":" + message.getMustStandInBlock());
            }
            if (!ProductUtils.isSpecialBlock(currentBlock)) {
                throw new ProductException(this.getName() + ":" + message.getNoSpecialBlock());
            }
            Town town = towny.getTown(player);
            boolean inCoolDown = ProductUtils.isPublicBlockInCoolDown(player, this);
            if (inCoolDown) {
                SimpleTownyProduct.logger.info("In cool down. Skip.");
                return;
            }

            List<String> actuallyCommand = ProductUtils.formatBlockCommands(this, town);
            ProductUtils.executeCommand(player, actuallyCommand);
            Messages.send(player, message.getSuccessGainProduct().formatted(this.getName()));
            BukkitTools.fireEvent(new PlotGainProductEvent(town, this, player));
            BlockCoolDownData cool = new BlockCoolDownData();
            cool.setGainPlayerName(player.getName());
            cool.setGainPlayerUid(player.getUniqueId().toString());
            cool.setGainTime(CommonUtils.HHMMDDHMS.format(new Date()));
            cool.setBlockUuid(this.getUid());
            cool.setGainCount(1L);
            List<BlockCoolDownData> blockCoolDowns = SimpleTownyProduct.PUBLIC_SPECIAL_DATA.getBlockCoolDowns();
            blockCoolDowns.add(cool);
            Timer timer = new Timer(this.getUid(), this.getGainCoolDown());
            timer.setTimerHandler(() -> {
                // 当冷却时间到时删除冷却数据
                blockCoolDowns.remove(cool);
            });
            timer.start();
        } catch (Exception e) {
            Messages.sendError(player, e.getMessage());
        }
    }

    @Override
    public void beSteal(Player player) {
        throw new MethodNotSupportException();
    }
}
