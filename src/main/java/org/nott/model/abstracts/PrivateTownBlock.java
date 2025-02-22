package org.nott.model.abstracts;

import lombok.Data;
import org.bukkit.entity.Player;
import org.nott.SimpleTownyProduct;
import org.nott.model.interfaces.Product;

@Data
public class PrivateTownBlock extends BaseBlock implements Product {

    @Override
    public void doGain(Player player) {
        // do something
        SimpleTownyProduct.logger.info("doGain in PrivateTownBlock");
    }

}
