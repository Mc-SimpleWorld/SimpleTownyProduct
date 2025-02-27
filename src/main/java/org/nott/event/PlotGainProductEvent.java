package org.nott.event;

import com.palmergames.bukkit.towny.object.Town;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.nott.model.abstracts.BaseBlock;

/**
 * @author Nott
 * @date 2025-2-27
 */

@Data
public class PlotGainProductEvent extends Event {

    private Town currentTown;

    private BaseBlock specialBlock;

    private Player gainer;

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public PlotGainProductEvent(Town town, BaseBlock specialBlock, Player gainer) {
        this.currentTown = town;
        this.specialBlock = specialBlock;
        this.gainer = gainer;
    }

    public PlotGainProductEvent(boolean isAsync, Town town, BaseBlock specialBlock, Player gainer) {
        super(isAsync);
        this.currentTown = town;
        this.specialBlock = specialBlock;
        this.gainer = gainer;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
