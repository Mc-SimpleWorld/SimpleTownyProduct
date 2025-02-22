package org.nott.utils;

import org.nott.SimpleTownyProduct;
import org.nott.model.PlayerPlotBlock;
import org.nott.model.SpecialTownBlock;
import org.nott.model.abstracts.PrivateTownBlock;
import org.nott.model.abstracts.PublicTownBlock;

import java.util.List;

public class ProductUtils {

    public static PlayerPlotBlock findSpecialTownBlock(String name){
        SpecialTownBlock blockTypes = SimpleTownyProduct.INSTANCE.configuration.getBlockTypes();
        List<PublicTownBlock> publics = blockTypes.getPublics();
        PublicTownBlock find = publics.stream().filter(publicTownBlock -> publicTownBlock.getName().equals(name)).findFirst().orElse(null);
        if(find != null){
            return new PlayerPlotBlock(true, find);
        }
        List<PrivateTownBlock> privates = blockTypes.getPrivates();
        PrivateTownBlock privateTownBlock = privates.stream().filter(privateTownBlock1 -> privateTownBlock1.getName().equals(name)).findFirst().orElse(null);
        if(privateTownBlock != null){
            return new PlayerPlotBlock(false, privateTownBlock);
        }
        return null;

    }
}
