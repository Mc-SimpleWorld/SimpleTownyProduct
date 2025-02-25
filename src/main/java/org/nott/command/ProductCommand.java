package org.nott.command;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.confirmations.ConfirmationBuilder;
import com.palmergames.bukkit.towny.confirmations.ConfirmationHandler;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.util.BukkitTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nott.SimpleTownyProduct;
import org.nott.event.PlotBeStealEvent;
import org.nott.event.PrePlotStealEvent;
import org.nott.model.Configuration;
import org.nott.model.Message;
import org.nott.model.PlayerPlotBlock;
import org.nott.model.abstracts.BaseBlock;
import org.nott.utils.CommonUtils;
import org.nott.utils.Messages;
import org.nott.utils.ProductUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nott
 * @date 2025-2-21
 */
public class ProductCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        instance.getLogger().info("Product command executed.");
        String execute = args.length > 0 ? args[0] : "";
        String[] subArgs = CommonUtils.removeFirstElement(args);
        switch (execute) {
            default:
                parsePluginInfoCommand(commandSender);
                break;
            case "help":
                parseHelpCommand(commandSender);
                break;
            case "reload":
                parseReloadCommand(commandSender);
                break;
            case "info":
                parseInfoCommand(commandSender, subArgs);
                break;
            case "gain":
                parseGainCommand(commandSender);
                break;
            case "steal":
                parseStealCommand(commandSender, subArgs);
                break;
        }
        return true;
    }

    private void parseHelpCommand(CommandSender commandSender) {
        Message message = SimpleTownyProduct.INSTANCE.getMessage();
        boolean op = commandSender.isOp();
        List<Component> texts = new ArrayList<Component>();
        for (String string : message.getCommandHelp()) {
            texts.add(Component.text(string, NamedTextColor.GOLD));
        }
        if(op){
            for (String string : message.getCommandAdminHelp()) {
                texts.add(Component.text(string, NamedTextColor.GOLD));
            }
        }
        Messages.sendMessages(commandSender, Messages.buildProductScreen(texts));
    }

    private void parsePluginInfoCommand(CommandSender commandSender) {
        Message message = SimpleTownyProduct.INSTANCE.getMessage();
        Configuration configuration = SimpleTownyProduct.INSTANCE.getConfiguration();
        List<Component> texts = new ArrayList<Component>();
        texts.add(Component.text(configuration.getPrefix(), NamedTextColor.DARK_GREEN));
        texts.add(Component.text(message.getPluginsDescription(), NamedTextColor.DARK_GREEN));
        texts.add(Component.text("Version: [%s]".formatted(configuration.getVersion()), NamedTextColor.DARK_GREEN));
        Messages.sendMessages(commandSender, Messages.buildProductScreen(texts));
    }

    private void parseReloadCommand(CommandSender commandSender) {
        if(!commandSender.isOp()){
            Messages.sendError(commandSender, SimpleTownyProduct.INSTANCE.getMessage().getCommonNoPermission());
            return;
        }
        SimpleTownyProduct.INSTANCE.loadConfiguration();
    }

    private void parseStealCommand(CommandSender commandSender, String[] args) {
        Player player = (Player) commandSender;
        Location location = player.getLocation();
        TownyAPI townyAPI = TownyAPI.getInstance();
        Resident resident = townyAPI.getResident(player);
        Town fromTown = townyAPI.getResidentTownOrNull(resident);
        Nation fromNation = townyAPI.getResidentNationOrNull(resident);
        Town town = townyAPI.getTown(location);
        Message message = SimpleTownyProduct.INSTANCE.getMessage();
        Configuration configuration = SimpleTownyProduct.INSTANCE.getConfiguration();
        boolean stealFromTown = !configuration.isStealNeedInBlock();
        List<BaseBlock> targetBlocks = new ArrayList<>();
        if(!configuration.isBlockCanBeSteal()){
            Messages.sendError(commandSender, message.getNotOpenSteal());
            return;
        }
        if(fromTown == null){
            Messages.sendError(commandSender, message.getNotInTown());
            return;
        }
        if (town == null) {
            Messages.sendError(commandSender, message.getMustStandInTown());
            return;
        }
        if(fromTown.equals(town)){
            Messages.sendError(commandSender, message.getNotAllowStealOwnTown());
            return;
        }
        if(fromNation != null && fromNation.getTowns().contains(town)){
            Messages.sendError(commandSender, message.getNotAllowStealNationTown());
            return;
        }
        if(!stealFromTown){
            TownBlock townBlock = townyAPI.getTownBlock(location);
            if (townBlock == null) {
                Messages.sendError(commandSender, message.getMustStandInBlock());
                return;
            }

            PlayerPlotBlock plotBlock = ProductUtils.findSpecialTownBlock(townBlock.getTypeName());
            if (plotBlock == null) {
                Messages.sendError(commandSender, message.getNotOnAnyBlock());
                return;
            }
            boolean blockPublic = plotBlock.isPublic();
            if (blockPublic) {
                Messages.sendError(commandSender, message.getPublicBlockCantSteal());
                return;
            }
            targetBlocks.add(plotBlock.getBlock());
        }else {
            Collection<TownBlock> townBlocks = town.getTownBlocks();
            List<PlayerPlotBlock> plotBlocks = ProductUtils.getSpecialBlockFromTownBlock(townBlocks, true);
            if(plotBlocks.isEmpty()){
                Messages.sendError(commandSender, message.getNoSpecialBlock());
                return;
            }
            targetBlocks = plotBlocks.stream().map(PlayerPlotBlock::getBlock).collect(Collectors.toList());
        }

        if(ProductUtils.isInCoolDown(player.getUniqueId().toString())){
            Messages.sendError(commandSender, message.getWaitForNextSteal());
            return;
        }

        if (ProductUtils.isInCoolDown(town.getUUID().toString())) {
            Messages.sendError(commandSender, message.getTargetCoolingDown());
            return;
        }

        PrePlotStealEvent event = new PrePlotStealEvent(targetBlocks, player ,town, stealFromTown);
        BukkitTools.fireEvent(event);
    }

    private void parseGainCommand(CommandSender commandSender) {
        Player player = (Player) commandSender;
        Resident resident = TownyAPI.getInstance().getResident(player);
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Message message = instance.getMessage();
        if (resident == null) {
            Messages.sendError(commandSender, message.getNotInTown());
            return;
        }
        Town town = resident.getTownOrNull();
        if (town == null) {
            Messages.sendError(commandSender, message.getNotInTown());
            return;
        }
        Collection<TownBlock> townBlocks = town.getTownBlocks();
        List<PlayerPlotBlock> haveBlocks = ProductUtils.getSpecialBlockFromTownBlock(townBlocks, true);
        // 异步处理地块收获产品逻辑
        haveBlocks.forEach(townBlock -> {
            SimpleTownyProduct.SCHEDULER.runTask(instance, () -> {
                BaseBlock block = townBlock.getBlock();
                block.doGain(player);
            });
        });
    }

    private void parseInfoCommand(CommandSender commandSender, String[] subArgs) {
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Message message = instance.getMessage();
        // 返回拥有的特殊地块信息(公共地块 + 个人地块)
        Player player = (Player) commandSender;
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null) {
            Messages.sendError(commandSender, message.getNotInTown());
            return;
        }
        Town town = resident.getTownOrNull();
        if (town == null){
            Messages.sendError(commandSender, message.getNotInTown());
        }
        Collection<TownBlock> townBlocks = town.getTownBlocks();
        if(townBlocks == null || townBlocks.isEmpty()){
            Messages.sendError(commandSender, message.getNoSpecialBlock());
            return;
        }
        List<PlayerPlotBlock> haveBlocks = ProductUtils.getSpecialBlockFromTownBlock(townBlocks, true);
        // 返回拥有的特殊地块信息(地块类型 产出物品 是否冷却)
        List<Component> body = new ArrayList<>();
        body.add(Component.text(message.getCurrentTown().formatted(town.getName()))
                .color(TextColor.fromHexString("#ffd391")));
        body.add(Component.text(message.getClickToGain())
                .hoverEvent(HoverEvent.showText(Component.text(message.getGainCommandHover())))
                .color(NamedTextColor.GOLD)
                .clickEvent(ClickEvent.runCommand("/t product gain"))
        );
        body.add(Messages.blankLine());
        body.add(Component.text("%s--%s--%s".formatted(message.getSpecialBlock(),message.getSpecialType(),message.getWhetherCoolDown()))
                .color(TextColor.fromHexString("#38d415")));
        body.add(Messages.blankLine());
        for (PlayerPlotBlock haveBlock : haveBlocks) {
            BaseBlock block = haveBlock.getBlock();
            boolean aPublic = haveBlock.isPublic();
            String name = block.getName();
            String isPublic = aPublic ? message.getPublicType() : message.getPrivateType();
            boolean isCoolDown = ProductUtils.isInCoolDown(aPublic ? player.getUniqueId().toString() : town.getUUID().toString()) ;
            String coolDown = isCoolDown ? message.getCoolDown() : message.getUnCoolDown();
            String info = "%s--%s--%s".formatted(name,isPublic,coolDown);
            TextComponent component = Component.text(info).color(aPublic ? NamedTextColor.DARK_GREEN : NamedTextColor.GOLD);
            body.add(component);
        }
        Messages.sendMessages(commandSender,Messages.buildProductScreen(body));
    }
}
