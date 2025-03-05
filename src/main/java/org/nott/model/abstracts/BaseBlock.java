package org.nott.model.abstracts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.palmergames.bukkit.towny.object.TownBlock;
import lombok.Data;
import org.bukkit.entity.Player;
import org.nott.SimpleTownyProduct;
import org.nott.exception.ConfigWrongException;

import java.util.List;
import java.util.UUID;

@Data
public abstract class BaseBlock {

    @JsonIgnore
    private String uid;

    private String key;

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
        SimpleTownyProduct.logger.info("DoGain in Base");
    };

    public void beSteal(Player player){
        SimpleTownyProduct.logger.info("DoSteal in Base");
    }

    public String generateUUId(TownBlock townBlock){
        this.uid = townBlock.toString();
        return this.getUid();
    }
}
