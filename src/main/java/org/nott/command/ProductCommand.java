package org.nott.command;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nott.SimpleTownyProduct;
import org.nott.model.PlayerPlotBlock;
import org.nott.model.abstracts.BaseBlock;
import org.nott.utils.CommonUtils;
import org.nott.utils.Messages;
import org.nott.utils.ProductUtils;

import java.util.Collection;

/**
 * @author Nott
 * @date 2025-2-21
 */
public class ProductCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        instance.getLogger().info("Product command executed.");
        if (args.length == 0) {
            return true;
        }
        String execute = args[0];
        String[] subArgs = CommonUtils.removeFirstElement(args);
        switch (execute) {
            case "info":
                parseInfoCommand(commandSender);
                break;
            case "gain":
                parseGainCommand(commandSender);
                break;
        }
        return true;
    }

    private void parseGainCommand(CommandSender commandSender) {
        Player player = (Player) commandSender;
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null) {
            Messages.sendError(commandSender, "You are not a resident.");
            return;
        }
        Town townOrNull = resident.getTownOrNull();
        if (townOrNull == null) {
            Messages.sendError(commandSender, "You are not in a town.");
            return;
        }
        Town town = townOrNull;
        Collection<TownBlock> townBlocks = town.getTownBlocks();
        SimpleTownyProduct instance = SimpleTownyProduct.INSTANCE;
        long count = townBlocks.stream().filter(townBlock -> instance.getConfiguration().getBlockTypes().existBlock(townBlock.getType().getName())
        ).findAny().stream().count();
        if (count == 0) {
            Messages.sendError(commandSender, "You don't have any special blocks.");
        }
        // 异步处理地块收获产品逻辑
        townBlocks.forEach(townBlock -> {
            SimpleTownyProduct.SCHEDULER.runTaskAsynchronously(instance, () -> {
                // do something
                String name = townBlock.getType().getName();
                if (!instance.getConfiguration().getBlockTypes().existBlock(name)) {
                    return;
                }
                PlayerPlotBlock specialTownBlock = ProductUtils.findSpecialTownBlock(name);
                BaseBlock block = specialTownBlock.getBlock();
                block.doGain(player);
            });
        });
    }

    private void parseInfoCommand(CommandSender commandSender) {
        commandSender.sendMessage("SimpleTownyProduct version: " + SimpleTownyProduct.VERSION);
    }
}
