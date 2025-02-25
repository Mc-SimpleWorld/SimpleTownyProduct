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
public class PlotBeStealEvent extends CancellableTownyEvent {

    private List<BaseBlock> targetBlock;

    private Town targetTown;

    private Player thief;

    private boolean stealWholeTownBlock;

    public PlotBeStealEvent(List<BaseBlock> targetBlock, Town targetTown, Player thief, boolean stealWholeTownBlock) {
        this.targetBlock = targetBlock;
        this.targetTown = targetTown;
        this.thief = thief;
        this.stealWholeTownBlock = stealWholeTownBlock;
    }

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
