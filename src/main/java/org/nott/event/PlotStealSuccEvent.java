package org.nott.event;

import com.palmergames.bukkit.towny.object.Town;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.nott.model.StealActivity;
import org.nott.model.abstracts.BaseBlock;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlotStealSuccEvent extends Event {

    private Town town;

    private Player thief;

    private List<BaseBlock> blocks;

    private StealActivity stealActivity;

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public PlotStealSuccEvent(StealActivity stealActivity) {
        this.stealActivity = stealActivity;
        this.town = stealActivity.getTargetTown();
        this.thief = stealActivity.getThief();
        this.blocks = stealActivity.getBlocks();
    }

    public PlotStealSuccEvent(boolean isAsync, StealActivity stealActivity) {
        super(isAsync);
        this.stealActivity = stealActivity;
        this.town = stealActivity.getTargetTown();
        this.thief = stealActivity.getThief();
        this.blocks = stealActivity.getBlocks();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}