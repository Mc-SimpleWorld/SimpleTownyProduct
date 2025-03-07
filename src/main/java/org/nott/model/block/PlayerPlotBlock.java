package org.nott.model.block;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import lombok.Data;
import org.nott.model.abstracts.BaseBlock;
import org.nott.model.data.SpecialBlockData;

@Data
public class PlayerPlotBlock {

    private Town town;

    private TownBlock townBlock;

    private boolean isPublic;

    private BaseBlock block;

    private SpecialBlockData specialBlockData;

    private boolean isCoolDown;

    public PlayerPlotBlock() {
    }

    public PlayerPlotBlock(boolean isPublic, BaseBlock block) {
        this.isPublic = isPublic;
        this.block = block;
    }

    public PlayerPlotBlock(boolean isPublic, BaseBlock block, TownBlock townBlock) {
        this.isPublic = isPublic;
        this.block = block;
        this.townBlock = townBlock;
    }

    public PlayerPlotBlock(TownBlock townBlock, boolean isPublic, BaseBlock block, SpecialBlockData specialBlockData) {
        this.townBlock = townBlock;
        this.isPublic = isPublic;
        this.block = block;
        this.specialBlockData = specialBlockData;
    }
}
