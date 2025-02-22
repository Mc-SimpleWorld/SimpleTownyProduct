package org.nott.model.abstracts;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.nott.SimpleTownyProduct;
import org.nott.model.SpecialTownBlock;
import org.nott.model.interfaces.Product;
import org.nott.utils.ProductUtils;

import java.util.Collection;
import java.util.List;

@Data
public class PublicTownBlock extends BaseBlock implements Product {

    @Override
    public void doGain(Player player) {
        TownyAPI towny = TownyAPI.getInstance();
        Location location = player.getLocation();
        Town town = towny.getTown(location);
        TownBlock currentBlock = towny.getTownBlock(location);
        if (currentBlock == null) {
            SimpleTownyProduct.logger.info("Not a Block. Skip.");
            return;
        }
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        SpecialTownBlock blockTypes = instance.getConfiguration().getBlockTypes();
        List<PublicTownBlock> publics = blockTypes.getPublics();
        if (town == null) {
            SimpleTownyProduct.logger.info("Not in town. Skip.");
            return;
        }
        publics.stream().filter(publicTownBlock -> publicTownBlock.getName().equals(currentBlock.getType().getName())).findFirst().ifPresent(publicTownBlock -> {
            SimpleTownyProduct.logger.info("Start gain in PublicTownBlock");
            String uuid = player.getUniqueId().toString();
            boolean inCoolDown = ProductUtils.isInCoolDown(uuid, this);
            if (inCoolDown) {
                SimpleTownyProduct.logger.info("In cool down. Skip.");
                return;
            }
            // ProductUtils.executeCommand(player, this, this.getGainCommand());
            SimpleTownyProduct.logger.info("doGain in PublicTownBlock");
            ProductUtils.addCoolDown(uuid, this);
        });

    }
}
