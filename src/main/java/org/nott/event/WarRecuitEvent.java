package org.nott.event;

import com.palmergames.bukkit.towny.object.Town;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.nott.model.block.PlayerPlotBlock;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nott
 * @date 2025-3-13
 */
@Data
public class WarRecuitEvent extends Event {

    private Player starter;

    private Town town;

    private PlayerPlotBlock plotBlock;

    private Set<Player> playerGroup = new HashSet<>();

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public WarRecuitEvent(Player starter, Town town, PlayerPlotBlock plotBlock) {
        this.starter = starter;
        this.town = town;
        this.plotBlock = plotBlock;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
