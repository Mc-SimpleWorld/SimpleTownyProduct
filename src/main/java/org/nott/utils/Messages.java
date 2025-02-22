package org.nott.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nott.SimpleTownyProduct;
import org.nott.model.Message;

public class Messages {

    public static final String SUCCESS = "00EE00";
    public static final String ERROR = "EE0000";

    public static String format(String messageKey, Object... args) {
        return messageKey.contains("%s") ? String.format(messageKey, args) : messageKey;
    }

    public static void sendError(CommandSender sender, String messageKey, Object... args) {
        String message = format(messageKey, args);
        message = SimpleTownyProduct.INSTANCE.getConfiguration().getPrefix() + message;
        send(sender, TextColor.fromCSSHexString(ERROR), message);
    }

    public static void send(CommandSender sender, String messageKey, Object... args) {
        send(sender, true, messageKey, args);
    }

    public static void send(CommandSender sender, boolean prefix, String messageKey, Object... args) {
        String message = format(messageKey, args);
        if (prefix) {
            message = SimpleTownyProduct.INSTANCE.getConfiguration().getPrefix() + message;
            send(sender, TextColor.fromCSSHexString(SUCCESS), message);
        } else {
            send(sender,TextColor.fromCSSHexString(SUCCESS), message);
        }
    }

    public static void send(CommandSender sender, TextColor textColor, String message) {
        TextComponent component = Component.text(message).color(textColor);
        SimpleTownyProduct.MESSAGE_API.sender(sender).sendMessage(component);
    }

}
