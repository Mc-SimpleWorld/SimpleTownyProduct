package org.nott.utils;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.nott.SimpleTownyProduct;
import org.nott.model.Message;

/**
 * @author Nott
 * @date 2025-2-28
 */
public class PermissionUtils {

    public static void grantPermission(Player player, Plugin plugin, String permission) {
        PermissionAttachment attachment = player.addAttachment(plugin);
        attachment.setPermission(permission, true);
    }

    public static void revokePermission(Player player, Plugin plugin, String permission) {
        PermissionAttachment attachment = player.addAttachment(plugin);
        attachment.setPermission(permission, false);
    }

    public static void checkPermission(Player player, String permission) {
        if(player.isOp() || player instanceof ConsoleCommandSender){
            return;
        }
        if (!player.hasPermission(permission)) {
            Message message = SimpleTownyProduct.INSTANCE.getMessage();
            Messages.sendError(player, message.getCommonNoPermission());
        }
    }
}
