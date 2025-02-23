package org.nott.model.abstracts;

import lombok.Data;
import org.bukkit.entity.Player;
import org.nott.SimpleTownyProduct;

import java.util.List;

@Data
public abstract class BaseBlock {

    private Double townLevelExponent;

    private String gainCoolDown;

    private Integer baseGainNumber;

    private List<String> gainCommand;

    private String mapKey;

    private String name;

    private Integer limitPerTown;

    private Integer sameNationMax;

    private Double basePrice;

    private Double baseSellPrice;

    private boolean forSaleAble;

    private List<String> repelBlockType;

    private boolean tradeAble;

    private String tradeCoolDown;

    public void doGain(Player player) {
        // do something
        SimpleTownyProduct.logger.info("doGain in Base");
    };
}
