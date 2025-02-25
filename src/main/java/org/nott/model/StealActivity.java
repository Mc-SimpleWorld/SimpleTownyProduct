package org.nott.model;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.nott.event.PlotBeStealEvent;
import org.nott.model.abstracts.BaseBlock;

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
        if(inTown){
            Town town = townyAPI.getTown(location);
            if(!targetTown.equals(town)){
                this.setInterrupt(true);
                this.setInterruptReason("Out of town");
            }
        }else {
            TownBlock block = townyAPI.getTownBlock(location);
            BaseBlock baseBlock = getBlocks().get(0);
            if(block == null || !baseBlock.getName().equals(block.getTypeName())){
                this.setInterrupt(true);
                this.setInterruptReason("Out of block");
            }
        }
    }

    public boolean isEnd() {
        return System.currentTimeMillis() + period >= start + period;
    }

    public void waitForEnd() {
        // 加入异步检测小偷是否走出目标区间[城镇 or 区块]，若是则取消该事件

    }
}
