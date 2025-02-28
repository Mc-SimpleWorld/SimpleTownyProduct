package org.nott.utils;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

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
}
