package org.nott.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.nott.model.StealActivity;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlotStealPauseEvent extends Event {

    private StealActivity stealActivity;

    private Player thief;

    private int remainingDeathCount;

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public PlotStealPauseEvent(StealActivity stealActivity, int remainingDeathCount) {
        this.stealActivity = stealActivity;
        this.thief = stealActivity.getThief();
        this.remainingDeathCount = remainingDeathCount;
    }

    public PlotStealPauseEvent(boolean isAsync, StealActivity stealActivity, int remainingDeathCount) {
        super(isAsync);
        this.stealActivity = stealActivity;
        this.thief = stealActivity.getThief();
        this.remainingDeathCount = remainingDeathCount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}