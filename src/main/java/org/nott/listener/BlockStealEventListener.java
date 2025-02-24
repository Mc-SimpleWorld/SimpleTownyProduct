package org.nott.listener;

import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.util.BukkitTools;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.nott.SimpleTownyProduct;
import org.nott.event.PlotBeStealEvent;
import org.nott.event.PrePlotStealEvent;
import org.nott.model.Configuration;
import org.nott.model.Message;
import org.nott.model.abstracts.BaseBlock;

/**
 * @author Nott
 * @date 2025-2-24
 */
public class BlockStealEventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreBlockStealEventListener(PrePlotStealEvent prePlotStealEvent){
        BaseBlock block = prePlotStealEvent.getBlock();
        Player player = prePlotStealEvent.getThief();
        Town town = prePlotStealEvent.getTargetTown();
        Message message = SimpleTownyProduct.INSTANCE.getMessage();
        Confirmation.runOnAcceptAsync(() -> BukkitTools.fireEvent(new PlotBeStealEvent(block, town, player)))
                .setTitle(message.getConfirmToSteal())
                .setAsync(true)
                .setCancelText(message.getGiveUpSteal())
                .setDuration(20)
                .runOnCancel(() -> prePlotStealEvent.setCancelled(true))
                .sendTo(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBeStealEventListener(PlotBeStealEvent plotBeStealEvent){
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Configuration configuration = instance.getConfiguration();
        BaseBlock targetBlock = plotBeStealEvent.getTargetBlock();
        Player player = plotBeStealEvent.getThief();
        Town town = plotBeStealEvent.getTargetTown();
        // todo 检测是否处于偷取冷却（根据配置：是否只能按区块偷取或直接偷取城镇，判断不同的uuid冷却）

        // todo 加入异步检测小偷是否走出目标区间，若是则取消该事件(runLate方法)

        // todo 调用区块的steal方法
    }
}
