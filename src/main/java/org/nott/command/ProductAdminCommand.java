package org.nott.command;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nott.SimpleTownyProduct;
import org.nott.exception.ProductException;
import org.nott.model.Configuration;
import org.nott.model.Message;
import org.nott.model.block.PlayerPlotBlock;
import org.nott.model.block.PublicTownBlock;
import org.nott.model.abstracts.BaseBlock;
import org.nott.utils.CommonUtils;
import org.nott.utils.Messages;
import org.nott.utils.PermissionUtils;
import org.nott.utils.ProductUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Nott
 * @date 2025-3-3
 */
public class ProductAdminCommand implements TabExecutor {

    List<String> tabCommands = Arrays.asList("reload", "set", "s");
    List<String> tabSecCommands = Arrays.asList("block", "steal");

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        instance.getLogger().info("Product command executed.");
        if (!PermissionUtils.hasPermission(commandSender, "towny.product.admin")) {
            Messages.sendError(commandSender, SimpleTownyProduct.INSTANCE.getMessage().getCommonNoPermission());
            return true;
        }
        String execute = args.length > 0 ? args[0] : "";
        try {
            String[] subArgs = CommonUtils.removeFirstElement(args);
            switch (execute) {
                default:
                    parseHelpCommand(commandSender);
                    break;
                case "reload":
                    parseReloadCommand();
                    break;
                case "set":
                    parseAdminSetCoolDownCommand(commandSender, subArgs);
                    break;

            }
        } catch (Exception e) {
            Messages.sendError(commandSender, e.getMessage());
        }
        return true;
    }

    private void parseHelpCommand(CommandSender commandSender) {
        Message message = SimpleTownyProduct.INSTANCE.getMessage();
        List<Component> texts = new ArrayList<Component>();
        for (String string : message.getCommandAdminHelp()) {
            texts.add(Component.text(string, NamedTextColor.GOLD));
        }
        Messages.sendMessages(commandSender, Messages.buildProductScreen(texts));
    }

    private void parseReloadCommand() {
        SimpleTownyProduct.INSTANCE.loadConfigurationAndMessage();
    }

    private void parseAdminSetCoolDownCommand(CommandSender commandSender, String[] args) {
        if (args.length == 0) {
            return;
        }
        TownyAPI townyAPI = TownyAPI.getInstance();
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Message message = instance.getMessage();

        String arg = args[0];
        if ("block".equalsIgnoreCase(arg)) {
            parseSetBlockCoolDownCommand(commandSender, args, townyAPI, message);
        }
        if ("steal".equalsIgnoreCase(arg)) {
            parseSetStealCoolDownCommand(args, townyAPI, message);
        }

    }

    private void parseSetStealCoolDownCommand(String[] args, TownyAPI townyAPI, Message message) {
        String playerName = args[1];
        Resident resident = townyAPI.getResident(playerName);
        if (resident == null) {
            throw new ProductException(Messages.format(message.getResidentNotFound(), playerName));
        }
        Player player = resident.getPlayer();
        String period = args[2];
        ProductUtils.setCoolDown(ProductUtils.stealActivityKey(player), Long.parseLong(period));
    }

    private static void parseSetBlockCoolDownCommand(CommandSender commandSender, String[] args, TownyAPI townyAPI, Message message) {
        String type = args[1];
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        Configuration configuration = instance.getConfiguration();
        if ("private".equalsIgnoreCase(type)) {
            String townName = args[2];
            String blockName = args[3];
            String period = args[4];
            long parsePeriod = Long.parseLong(period);
            Town town = townyAPI.getTown(townName);
            if (town == null) {
                throw new ProductException(Messages.format(message.getTownNotFound(), townName));
            }
            List<PlayerPlotBlock> block = ProductUtils.getSpecialBlockFromTownBlock(town.getTownBlocks(), false);
            if ("*".equalsIgnoreCase(blockName)) {
                for (PlayerPlotBlock plotBlock : block) {
                    BaseBlock baseBlock = plotBlock.getBlock();
                    String key = ProductUtils.blockKey(baseBlock, town);
                    if (ProductUtils.isInCoolDown(key)) {
                        ProductUtils.setCoolDown(key, parsePeriod);
                        Messages.send(commandSender, message.getSuccessSetBlockCool(), town.getName(), baseBlock.getName(), period);
                    }
                }
            } else {
                PlayerPlotBlock plotBlock = ProductUtils.findSpecialTownBlock(blockName, town);
                if (plotBlock == null) {
                    throw new ProductException(Messages.format(message.getBlockNotFound(), blockName));
                }
                BaseBlock baseBlock = plotBlock.getBlock();
                ProductUtils.setCoolDown(ProductUtils.blockKey(plotBlock.getBlock(), town), parsePeriod);
                Messages.send(commandSender, message.getSuccessSetBlockCool(), town.getName(), baseBlock.getName(), period);
            }
        }
        if ("public".equalsIgnoreCase(type)) {
            String playerName = args[2];
            String blockName = args[3];
            String period = args[4];
            long parsePeriod = Long.parseLong(period);
            Resident resident = townyAPI.getResident(playerName);
            if (resident == null) {
                throw new ProductException(Messages.format(message.getResidentNotFound(), playerName));
            }
            List<PublicTownBlock> publics = configuration.getBlockTypes().getPublics();
            if ("*".equals(blockName)) {
                for (PublicTownBlock block : publics) {
                    ProductUtils.setCoolDown(ProductUtils.publicBlockKey(block, resident.getPlayer()), parsePeriod);
                }
            } else {
                PublicTownBlock specialBlock = publics.stream().filter(publicTownBlock -> blockName.equals(publicTownBlock.getName()))
                        .findFirst().orElse(null);
                if (specialBlock == null) {
                    throw new ProductException(Messages.format(message.getBlockNotFound(), blockName));
                }
                ProductUtils.setCoolDown(ProductUtils.publicBlockKey(specialBlock, resident.getPlayer()), parsePeriod);
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(PermissionUtils.hasPermission(commandSender, "towny.product.admin")){
            if(args.length == 1){
                return tabCommands;
            }
            if(args.length == 2){
                return tabSecCommands;
            }
        }
        return null;
    }
}
