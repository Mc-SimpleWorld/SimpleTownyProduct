package org.nott.model.block;

import com.palmergames.bukkit.towny.object.TownBlock;
import lombok.Data;
import org.nott.model.abstracts.BaseBlock;

@Data
public class PlayerPlotBlock {

    private TownBlock townBlock;

    private boolean isPublic;

    private BaseBlock block;

    public PlayerPlotBlock(boolean isPublic, BaseBlock block) {
        this.isPublic = isPublic;
        this.block = block;
    }

    public PlayerPlotBlock(boolean isPublic, BaseBlock block, TownBlock townBlock) {
        this.isPublic = isPublic;
        this.block = block;
        this.townBlock = townBlock;
    }
}
