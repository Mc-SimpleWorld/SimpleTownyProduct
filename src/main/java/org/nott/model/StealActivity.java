package org.nott.model;


import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.nott.SimpleTownyProduct;
import org.nott.event.PlotBeStealEvent;
import org.nott.model.abstracts.BaseBlock;
import org.nott.time.Timer;

import java.time.Duration;
import java.util.List;

/**
 * @author Nott
 * @date 2025-2-25
 */
@Data
public class StealActivity {

    private boolean isInterrupt;

    private Player thief;

    private Town targetTown;

    private List<BaseBlock> blocks;

    private Long start;

    private Long period;

    private boolean inTown;

    private String interruptReason;

    private Integer outOfTownCount = 0;

    public void interrupt() {
        this.isInterrupt = true;
    }

    public StealActivity() {
    }

    public StealActivity(PlotBeStealEvent event) {
        this.setBlocks(event.getTargetBlock());
        this.setInterrupt(false);
        this.setTargetTown(event.getTargetTown());
        this.setThief(event.getThief());
        this.setStart(System.currentTimeMillis());
        this.setInTown(event.isStealWholeTownBlock());
    }

    public void checkThiefIfOut() {
        Location location = thief.getLocation();
        TownyAPI townyAPI = TownyAPI.getInstance();
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Configuration configuration = instance.getConfiguration();
        Message message = instance.getMessage();
        if(inTown){
            Town town = townyAPI.getTown(location);
            if(!targetTown.equals(town)){
                countDownIfInterrupt(configuration, message);
            }else {
                this.setOutOfTownCount(0);
            }
        }else {
            TownBlock block = townyAPI.getTownBlock(location);
            BaseBlock baseBlock = getBlocks().get(0);
            if(block == null || !baseBlock.getName().equals(block.getTypeName())){
                countDownIfInterrupt(configuration, message);
            }else {
                this.setOutOfTownCount(0);
            }
        }
    }

    private void countDownIfInterrupt(Configuration configuration, Message message) {
        if(this.outOfTownCount <= configuration.getStealTempOutSecond()){
            this.setOutOfTownCount(this.getOutOfTownCount() + 1);
            int time = configuration.getStealTempOutSecond() - this.outOfTownCount;
            if(time > 0){
                final Component subtitle = Component.text(message.getThiefOutTownWarning().formatted(time), NamedTextColor.GRAY);
                Title title = Title.title(Component.empty(), subtitle, Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(1), Duration.ofMillis(0)));
                thief.showTitle(title);
            }
        }else {
            this.setInterrupt(true);
            this.setInterruptReason(message.getStealInterruptForOut());
        }
    }


    public void finish() {
        Timer.runningStealActivity.remove(thief.getUniqueId().toString());
    }
}
