package org.nott.model;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nott.SimpleTownyProduct;
import org.nott.model.abstracts.BaseBlock;
import org.nott.model.interfaces.Product;
import org.nott.utils.Messages;
import org.nott.utils.ProductUtils;

import java.util.List;

@Data
public class PublicTownBlock extends BaseBlock implements Product {

    @Override
    public void doGain(Player player) {
        SimpleTownyProduct.logger.info("%s starting to gain [%s] public product".formatted(player.getName(), this.getName()));
        TownyAPI towny = TownyAPI.getInstance();
        Location location = player.getLocation();
        TownBlock currentBlock = towny.getTownBlock(location);
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Message message = instance.getMessage();
        if (currentBlock == null) {
            SimpleTownyProduct.logger.info("Not a Block. Skip.");
            Messages.sendError((CommandSender) player, this.getName() + ":" + message.getMustStandInBlock());
            return;
        }
        SpecialTownBlock blockTypes = instance.getConfiguration().getBlockTypes();
        List<PublicTownBlock> publics = blockTypes.getPublics();
        publics.stream().filter(publicTownBlock -> publicTownBlock.getName().equals(currentBlock.getType().getName())).findFirst().ifPresent(publicTownBlock -> {
            String uuid = player.getUniqueId().toString();
            boolean inCoolDown = ProductUtils.isInCoolDown(uuid, this);
            if (inCoolDown) {
                SimpleTownyProduct.logger.info("In cool down. Skip.");
                return;
            }
            // ProductUtils.executeCommand(player, this, this.getGainCommand());
            Messages.send((CommandSender) player, message.getSuccessGainProduct().formatted(this.getName()));
            ProductUtils.addCoolDown(uuid, this);
        });

    }
}
