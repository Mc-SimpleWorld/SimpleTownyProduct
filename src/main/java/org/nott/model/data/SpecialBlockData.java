package org.nott.model.data;

import com.palmergames.bukkit.towny.object.TownBlock;
import lombok.Data;

/**
 * @author Nott
 * @date 2025-3-5
 */
@Data
public class SpecialBlockData {

    private String blockUuid;

    private String type;

    private boolean claimFromOther;

    private boolean neutral;

    // todo 添加该特殊区块对应的TownBlock属性，在收获时需要对比
//    private TownBlock townBlock;


}
