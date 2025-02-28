package org.nott.utils;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author Nott
 * @date 2025-2-28
 */
public class TownyUtils {

    public static boolean whetherInTown(Player player){
        return whetherInTown(player, false);
    }

    public static boolean whetherInTown(Player player, boolean self){
        TownyAPI townyAPI = TownyAPI.getInstance();
        Location location = player.getLocation();
        Town town = townyAPI.getTown(location);
        if(self){
            Resident resident = townyAPI.getResident(player);
            Town townOrNull = resident.getTownOrNull();
            if(townOrNull == null){
                return false;
            }
            return townOrNull.equals(town);
        }
        return town != null;
    }

    public static boolean whetherInBlock(Player player){
        return whetherInBlock(player, false);
    }

    public static boolean whetherInBlock(Player player, boolean self){
        TownyAPI townyAPI = TownyAPI.getInstance();
        Location location = player.getLocation();
        Town town = townyAPI.getTown(location);
        TownBlock townBlock = townyAPI.getTownBlock(location);
        if(town == null){
            return false;
        }
        if(self){
            if (!whetherInTown(player, true)) {
                return false;
            }
            if(townBlock == null){
                return false;
            }
            return town.hasTownBlock(townBlock);
        }
        return townBlock != null;
    }
}
