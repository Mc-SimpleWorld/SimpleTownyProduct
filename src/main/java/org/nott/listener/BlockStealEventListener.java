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
import org.nott.utils.PermissionUtils;
import org.nott.utils.ProductUtils;

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
        boolean pvp = town.isPVP();
        Message message = SimpleTownyProduct.INSTANCE.getMessage();
        Confirmation.runOnAcceptAsync(() -> BukkitTools.fireEvent(new PlotBeStealEvent(prePlotStealEvent.getBlocks(), town, player, stealWholeTownBlock)))
                .setTitle(stealWholeTownBlock ? message.getConfirmToStealTown() : message.getConfirmToSteal())
                .setAsync(true)
                .setDuration(20)
                .runOnCancel(() -> {
                    Messages.sendError(player, message.getGiveUpSteal());
                    prePlotStealEvent.setCancelled(true);
                    if(!pvp){
                        town.setPVP(false);
                    }
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
        // 添加偷窃冷却
         ProductUtils.addCoolDown(Timer.STEAL_KEY + player.getUniqueId(), TimePeriod.fromStringGetVal(configuration.getStealCoolDown()));
        // 若小偷在偷取中PVP死亡，将会被送入监狱并取消偷窃事件
        boolean pvp = town.isPVP();
        if(!pvp){
            town.setPVP(true);
        }
        SimpleTownyProduct.SCHEDULER.runTaskAsynchronously(instance, () -> {
            long second = val / 1000;
            long start = System.currentTimeMillis();
            // 创建进度条（bossbar）
            final BossBar bar = BossBar.bossBar(Component.text(message.getStealProgressTitle().formatted(second + "s"), NamedTextColor.DARK_GREEN), 1, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
            player.showBossBar(bar);
            final Component startTitle = Component.text(message.getStartStealBlockTitle(), NamedTextColor.GOLD);
            final Component startSubtitle = Component.text(message.getStartStealBlockSubTitle(), NamedTextColor.DARK_GRAY);
            Title begin = Title.title(startTitle, startSubtitle, Title.Times.times(Duration.ofSeconds(2), Duration.ofSeconds(2), Duration.ofMillis(1)));
            player.showTitle(begin);
            while (true){
                long currented = System.currentTimeMillis();
                // 时间结束
                if(currented >= start + val){
                    player.hideBossBar(bar);
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

                // Boss条
                long left = (start + val - currented) / 1000;
                double progress = (double) (currented - start) / val;
                bar.name(Component.text(message.getStealProgressTitle().formatted(left + "s")));
                bar.progress((float) progress);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // 检查小偷位置
                activity.checkThiefIfOut();
            }

        });
        StringBuilder targetBlocksName = new StringBuilder();
        for (BaseBlock block : targetBlock) {
            targetBlocksName.append(block.getName());
        }
        // 发送警告信息给受害城镇
        Timer.runningStealActivity.put(player.getUniqueId().toString(), activity);
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
        SimpleTownyProduct.SCHEDULER.runTaskLater(instance, () -> {
            if(!pvp){
                town.setPVP(false);
            }
            activity.finish();
        }, val / 1000 * 20 + 20);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockStealActInterruptEvent(PlotStealInterruptEvent event){
        // 发送信息
        Town currentTown = event.getCurrentTown();
        String message = event.getMessage();
        Player thief = event.getThief();
        Message instanceMessage = SimpleTownyProduct.INSTANCE.getMessage();
        Resident mayor = currentTown.getMayor();
        if (mayor.isOnline()) {
            Messages.send(mayor.getPlayer(), instanceMessage.getStealInterruptMessage(), thief.getName(), message);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockStealActEndEvent(PlotStealEndEvent event) {
        SimpleTownyProduct.logger.info("PlotStealEndEvent fired");
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        String thiefName = event.getThiefName();
        Long lost = event.getLost();
        Town town = event.getTown();
        BaseBlock block = event.getBlock();
        // 发送给拥有gain权限的人
        List<Resident> residents = town.getResidents();
        for (Resident resident : residents) {
            if (resident.isOnline() && PermissionUtils.hasPermission(resident.getPlayer(), "towny.product.gain")) {
                Messages.send(resident.getPlayer(), instance.getMessage().getBeStolenWarning()
                        .formatted(town.getName(), block.getName(), thiefName, lost));
            }
        }
        // 现在发给在线的市长
        Resident mayor = town.getMayor();
        if (mayor.isOnline()) {
            Messages.send(mayor.getPlayer(), instance.getMessage().getBeStolenWarning()
                    .formatted(town.getName(), block.getName(), thiefName, lost));
            return;
        }
        SimpleTownyProduct.logger.info("PlotStealEndEvent fired");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onStealingPlayPvPDeathEventListener(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        String playUUID = player.getUniqueId().toString();
        boolean isCurrentStealAct = Timer.runningStealActivity.containsKey(playUUID);
        if(!isCurrentStealAct){
            return;
        }
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Configuration configuration = instance.getConfiguration();
        boolean thiefKilledEnd = configuration.isThiefKilledEnd();
        if(!thiefKilledEnd){
            return;
        }
        Integer i = Timer.deathTimes.getOrDefault(playUUID, 0);
        i++;
        boolean isOver = i >=  configuration.getThiefKilledEndCount();
        if(isOver){
            Timer.deathTimes.remove(playUUID);
            StealActivity activity = Timer.runningStealActivity.get(playUUID);
            activity.setInterruptReason(SimpleTownyProduct.INSTANCE.getMessage().getStealFailDeath());
            activity.setInterrupt(true);
            Town town = activity.getTargetTown();
            TownyAPI townyAPI = TownyAPI.getInstance();
            Resident resident = townyAPI.getResident(player);
            if (!town.hasJails()) {
                Resident mayor = town.getMayor();
                if(mayor.isOnline()){
                    Messages.send(mayor.getPlayer(), SimpleTownyProduct.INSTANCE.getMessage().getTownNotHaveJailToLock());
                }
                Messages.sendError(player, SimpleTownyProduct.INSTANCE.getMessage().getTownNotHaveJailForThief());
                return;
            }
            Jail jail = town.getJails().stream().findFirst().get();
            JailUtil.jailResident(resident, jail, 1, 8, JailReason.OUTLAW_DEATH, null);
            Messages.sendError(player, SimpleTownyProduct.INSTANCE.getMessage().getThiefBeLockedByFail());
        }else {
            Timer.deathTimes.put(playUUID, i);
            Messages.sendError(player, SimpleTownyProduct.INSTANCE.getMessage().getThiefDeathWarning().formatted(configuration.getThiefKilledEndCount() - i));
        }
    }
}
