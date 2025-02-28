package org.nott.event;

import com.palmergames.bukkit.towny.object.Town;
import lombok.Data;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.nott.model.abstracts.BaseBlock;

/**
 * @author Nott
 * @date 2025-2-25
 */
@Data
public class PlotStealEndEvent extends Event {

    private Town town;

    private Long lost;

    private String thiefName;

    private BaseBlock block;

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public PlotStealEndEvent(Town town, Long lost, String thiefName, BaseBlock block) {
        this.town = town;
        this.lost = lost;
        this.thiefName = thiefName;
        this.block = block;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
