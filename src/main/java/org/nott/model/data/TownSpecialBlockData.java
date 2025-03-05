package org.nott.model.data;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import lombok.Data;
import org.nott.exception.ProductException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nott
 * @date 2025-3-5
 */
@Data
public class TownSpecialBlockData {

    private String townUuid;

    private String townName;

    private List<SpecialBlockData> specialBlocks;

    private List<BlockCoolDownData> blockCoolDowns;

    private List<LostResourceData> lostResources;

    public static TownSpecialBlockData empty(Town town) {
        TownSpecialBlockData data = new TownSpecialBlockData();
        data.setTownName(town.getName());
        data.setTownUuid(town.getUUID().toString());
        data.setSpecialBlocks(new ArrayList<>());
        data.setBlockCoolDowns(new ArrayList<>());
        data.setLostResources(new ArrayList<>());
        return data;
    }


    public LostResourceData getLost(String type){
        SpecialBlockData specialBlockData = this.specialBlocks.stream().filter(sb -> type.equals(sb.getType())).findFirst().orElse(null);
        if(specialBlockData == null){
            throw new ProductException("SpecialBlockData not found for " + type);
        }
        LostResourceData lost = lostResources.stream().filter(lostResourceData -> lostResourceData.getBlockUuid().equals(specialBlockData.getBlockUuid())).findFirst().orElse(null);
        return lost;
    }

    public BlockCoolDownData getCoolDown(String type){
        SpecialBlockData specialBlockData = this.specialBlocks.stream().filter(sb -> type.equals(sb.getType())).findFirst().orElse(null);
        if(specialBlockData == null){
            throw new ProductException("SpecialBlockData not found for " + type);
        }
        BlockCoolDownData blockCoolDown = blockCoolDowns.stream().filter(blockCoolDownData -> specialBlockData.getBlockUuid().equals(blockCoolDownData.getBlockUuid())).findFirst().orElse(null);
        return blockCoolDown;
    }

    public boolean isCoolDown(String type){
        return getCoolDown(type) != null;
    }

    public boolean isLost(String type){
        return getLost(type) != null;
    }
}
