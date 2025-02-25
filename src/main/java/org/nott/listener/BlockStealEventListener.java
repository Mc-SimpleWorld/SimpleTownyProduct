package org.nott.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.jail.Jail;
import com.palmergames.bukkit.towny.object.jail.JailReason;
import com.palmergames.bukkit.towny.utils.JailUtil;
import com.palmergames.bukkit.util.BukkitTools;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.nott.SimpleTownyProduct;
import org.nott.event.PlotBeStealEvent;
import org.nott.event.PlotStealEndEvent;
import org.nott.event.PlotStealInterruptEvent;
import org.nott.event.PrePlotStealEvent;
import org.nott.exception.ConfigWrongException;
import org.nott.model.Configuration;
import org.nott.model.Message;
import org.nott.model.StealActivity;
import org.nott.model.abstracts.BaseBlock;
import org.nott.time.TimePeriod;
import org.nott.time.Timer;
import org.nott.utils.Messages;

import java.time.Duration;
import java.util.List;

/**
 * @author Nott
 * @date 2025-2-24
 */
public class BlockStealEventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreBlockStealEventListener(PrePlotStealEvent prePlotStealEvent) {
        boolean stealWholeTownBlock = prePlotStealEvent.isStealWholeTownBlock();
        Player player = prePlotStealEvent.getThief();
        Town town = prePlotStealEvent.getTargetTown();
        Message message = SimpleTownyProduct.INSTANCE.getMessage();
        Confirmation.runOnAcceptAsync(() -> BukkitTools.fireEvent(new PlotBeStealEvent(prePlotStealEvent.getBlocks(), town, player, stealWholeTownBlock)))
                .setTitle(stealWholeTownBlock ? message.getConfirmToStealTown() : message.getConfirmToSteal())
                .setAsync(true)
                .setDuration(20)
                .runOnCancel(() -> {
                    Messages.sendError(player, message.getGiveUpSteal());
                    prePlotStealEvent.setCancelled(true);
                })
                .sendTo(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBeStealEventListener(PlotBeStealEvent plotBeStealEvent) throws ConfigWrongException {
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Configuration configuration = instance.getConfiguration();
        Message message = instance.getMessage();
        Player player = plotBeStealEvent.getThief();
        Town town = plotBeStealEvent.getTargetTown();
        String stealNeedStandInTime = configuration.getStealNeedStandInTime();
        Long val = TimePeriod.fromStringGetVal(stealNeedStandInTime);
        List<BaseBlock> targetBlock = plotBeStealEvent.getTargetBlock();
        StealActivity activity = new StealActivity(plotBeStealEvent);
        // TODO 添加偷窃冷却

        // 若小偷在偷取中PVP死亡，将会被送入监狱并取消偷窃事件
        town.setPVP(true);
        SimpleTownyProduct.SCHEDULER.runTaskAsynchronously(instance, () -> {
            long second = val / 1000;
            long start = System.currentTimeMillis();
            // 创建进度条（bossbar）
            final BossBar bar = BossBar.bossBar(Component.text(message.getStealProgressTitle().formatted(second + "s"), NamedTextColor.DARK_GREEN), 1, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
            player.showBossBar(bar);
            while (true){
                long currented = System.currentTimeMillis();
                if(currented >= start + val){
                    final Component mainTitle = Component.text(message.getStealSuccessTitle(), NamedTextColor.GREEN);
                    final Component subtitle = Component.text(message.getStealSuccessSubTitle(), NamedTextColor.GREEN);
                    Title title = Title.title(mainTitle, subtitle, Title.Times.times(Duration.ofSeconds(3), Duration.ofSeconds(5), Duration.ofMillis(2)));
                    player.hideBossBar(bar);
                    player.showTitle(title);
                    activity.finish();
                    break;
                }
                // 如果偷取事件被取消，则bossbar也取消
                if(activity.isInterrupt()){
                    SimpleTownyProduct.logger.info("Stealing event is over :" + activity.getInterruptReason());
                    final Component mainTitle = Component.text(message.getStealFailTitle(), NamedTextColor.DARK_RED);
                    final Component subtitle = Component.text(activity.getInterruptReason(), NamedTextColor.DARK_RED);
                    Title title = Title.title(mainTitle, subtitle, Title.Times.times(Duration.ofSeconds(3), Duration.ofSeconds(5), Duration.ofMillis(2)));
                    player.hideBossBar(bar);
                    player.showTitle(title);
                    break;
                }
                long left = (start + val - currented) / 1000;
                double progress = (double) (currented - start) / val;
                bar.name(Component.text(message.getStealProgressTitle().formatted(left + "s")));
                bar.progress((float) progress);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                activity.checkThiefIfOut();

            }

        });
        StringBuilder targetBlocksName = new StringBuilder();
        for (BaseBlock block : targetBlock) {
            targetBlocksName.append(block.getName());
        }
        // 发送警告信息给受害城镇
        Timer.runningStealActivity.put(player, activity);
        SimpleTownyProduct.SCHEDULER.runTaskAsynchronously(instance, () -> {
            List<Resident> residents = town.getResidents();
            for (Resident resident : residents) {
                if (resident.isOnline()) {
                    Player residentPlayer = resident.getPlayer();
                    Messages.sendError(residentPlayer,
                            message.getYourTownBeStealing()
                            , town.getName(), targetBlocksName.toString(), player.getName());
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockStealActInterruptEvent(PlotStealEndEvent event){
        SimpleTownyProduct.logger.info("PlotStealEndEvent fired");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockStealActEndEvent(PlotStealInterruptEvent event){
        SimpleTownyProduct.logger.info("PlotStealEndEvent fired");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onStealingPlayPvPDeathEventListener(PlayerDeathEvent event) {
        EntityDamageEvent entityDamageEvent = event.getEntity().getLastDamageCause();
        Entity entity = entityDamageEvent.getDamageSource().getCausingEntity();
        Player player = event.getPlayer();
        if (entity instanceof Player && Timer.runningStealActivity.containsKey(player)) {
            StealActivity activity = Timer.runningStealActivity.get(player);
            activity.setInterruptReason("Thief death.");
            activity.setInterrupt(true);
            Town town = activity.getTargetTown();
            TownyAPI townyAPI = TownyAPI.getInstance();
            Resident resident = townyAPI.getResident(player);
            if (!town.hasJails()) {
                return;
            }
            Jail jail = town.getJails().stream().findFirst().get();
            JailUtil.jailResident(resident, jail, 1, 8, JailReason.OUTLAW_DEATH, null);
        }

    }
}
