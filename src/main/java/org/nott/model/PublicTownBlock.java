package org.nott.model;

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
import org.nott.model.abstracts.BaseBlock;
import org.nott.model.interfaces.Product;
import org.nott.utils.Messages;
import org.nott.utils.PermissionUtils;
import org.nott.utils.ProductUtils;

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
        if (currentBlock == null) {
            SimpleTownyProduct.logger.info("Not a Block. Skip.");
            Messages.sendError(player, this.getName() + ":" + message.getMustStandInBlock());
            return;
        }
        Town town = towny.getTown(player);
        SpecialTownBlock blockTypes = instance.getConfiguration().getBlockTypes();
        List<PublicTownBlock> publics = blockTypes.getPublics();
        publics.stream().filter(publicTownBlock -> publicTownBlock.getName().equals(currentBlock.getType().getName())).findFirst().ifPresent(publicTownBlock -> {
            String uuid = player.getUniqueId().toString();
            boolean inCoolDown = ProductUtils.isInCoolDown(uuid);
            if (inCoolDown) {
                SimpleTownyProduct.logger.info("In cool down. Skip.");
                return;
            }
            try {
                List<String> actuallyCommand = ProductUtils.formatBlockCommands(this, town);
                ProductUtils.executeCommand(player, actuallyCommand);
                Messages.send(player, message.getSuccessGainProduct().formatted(this.getName()));
                BukkitTools.fireEvent(new PlotGainProductEvent(town, this, player));
                ProductUtils.addCoolDown(uuid, this);
            } catch (ConfigWrongException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void beSteal(Player player) {
        throw new MethodNotSupportException();
    }
}
