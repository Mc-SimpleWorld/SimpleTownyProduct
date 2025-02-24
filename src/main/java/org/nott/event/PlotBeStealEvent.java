package org.nott.event;

import com.palmergames.bukkit.towny.event.CancellableTownyEvent;
import com.palmergames.bukkit.towny.object.Town;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.nott.model.abstracts.BaseBlock;

/**
 * @author Nott
 * @date 2025-2-24
 */
@Data
public class PlotBeStealEvent extends CancellableTownyEvent {

    private BaseBlock targetBlock;

    private Town targetTown;

    private Player thief;

    public PlotBeStealEvent(BaseBlock block, Town town, Player thief) {
        this.targetBlock = block;
        this.targetTown = town;
        this.thief = thief;
    }

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
