package org.nott.model;

import lombok.Data;
import org.nott.model.abstracts.BaseBlock;

@Data
public class PlayerPlotBlock {

    private boolean isPublic;

    private BaseBlock block;

    public PlayerPlotBlock(boolean isPublic, BaseBlock block) {
        this.isPublic = isPublic;
        this.block = block;
    }
}
