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
public class PrePlotStealEvent extends CancellableTownyEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private BaseBlock block;

    private Player thief;

    private Town targetTown;

    public PrePlotStealEvent(BaseBlock block, Player thief, Town targetTown) {
        this.block = block;
        this.thief = thief;
        this.targetTown = targetTown;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
