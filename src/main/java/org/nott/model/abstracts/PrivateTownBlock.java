package org.nott.model.abstracts;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import lombok.Data;
import org.bukkit.entity.Player;
import org.nott.SimpleTownyProduct;
import org.nott.model.Configuration;
import org.nott.model.interfaces.Product;
import org.nott.utils.ProductUtils;

import java.util.logging.Level;

@Data
public class PrivateTownBlock extends BaseBlock implements Product {

    @Override
    public void doGain(Player player) {
        // do something
        SimpleTownyProduct.logger.info("Start gain in PrivateTownBlock");
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        TownyAPI towny = TownyAPI.getInstance();
        Resident resident = towny.getResident(player);
        Town town = resident.getTownOrNull();
        boolean isMayor = town.isMayor(resident);
        Configuration configuration = instance.getConfiguration();
        boolean residentCanGain = configuration.isResidentCanGain();
        if(!residentCanGain && !isMayor){
            SimpleTownyProduct.logger.log(Level.INFO, "Resident can't gain. Skip.");
            return;
        }
        // 判断是否在冷却中
        if(ProductUtils.isInCoolDown(town.getUUID().toString(), this)){
            SimpleTownyProduct.logger.log(Level.INFO, "In cool down. Skip.");
            return;
        }
        // 执行命令
//        ProductUtils.executeCommand(player, this, this.getGainCommand());
        SimpleTownyProduct.logger.info("doGain in PrivateTownBlock");
        // 添加冷却
        ProductUtils.addCoolDown4Town(town, this);
    }

}
