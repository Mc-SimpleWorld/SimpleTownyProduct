package org.nott.event;

import com.palmergames.bukkit.towny.event.CancellableTownyEvent;
import com.palmergames.bukkit.towny.object.Town;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.nott.model.abstracts.BaseBlock;

import java.util.List;

/**
 * @author Nott
 * @date 2025-2-24
 */

@Data
public class PrePlotStealEvent extends CancellableTownyEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private List<BaseBlock> blocks;

    private Player thief;

    private Town targetTown;

    private boolean stealWholeTownBlock;

    public PrePlotStealEvent(List<BaseBlock> blocks, Player thief, Town targetTown, boolean stealWholeTownBlock) {
        this.blocks = blocks;
        this.thief = thief;
        this.targetTown = targetTown;
        this.stealWholeTownBlock = stealWholeTownBlock;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
