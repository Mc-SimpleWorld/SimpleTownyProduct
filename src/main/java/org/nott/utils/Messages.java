package org.nott.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.nott.SimpleTownyProduct;
import org.nott.model.Message;

import java.util.ArrayList;
import java.util.List;

public class Messages {

    public static final String SPACE = " ";

    public static void sendMessages(CommandSender sender, List<Component> components) {
        for (Component component : components) {
            SimpleTownyProduct.MESSAGE_API.sender(sender).sendMessage(component);
        }
    }

    public static String format(String messageKey, Object... args) {
        return messageKey.contains("%s") ? String.format(messageKey, args) : messageKey;
    }

    public static void sendError(CommandSender sender, String messageKey, Object... args) {
        String message = format(messageKey, args);
        message = SPACE + SimpleTownyProduct.INSTANCE.getConfiguration().getPrefix() + message;
        send(sender, NamedTextColor.DARK_RED, message);
    }

    public static void send(CommandSender sender, String messageKey, Object... args) {
        send(sender, true, messageKey, args);
    }

    public static void send(CommandSender sender, boolean prefix, String messageKey, Object... args) {
        String message = format(messageKey, args);
        if (prefix) {
            message = SPACE + SimpleTownyProduct.INSTANCE.getConfiguration().getPrefix() + " " + message;
            send(sender, NamedTextColor.GREEN, message);
        } else {
            send(sender,NamedTextColor.GREEN, message);
        }
    }

    public static void send(CommandSender sender, TextColor textColor, String message) {
        TextComponent component = Component.text(message).color(textColor);
        SimpleTownyProduct.MESSAGE_API.sender(sender).sendMessage(component);
    }

    public static List<Component> buildProductScreen(List<Component> body){
        List<Component> components = new ArrayList<>();
        components.add(blankLine());
        components.add(Component.text("---------[%s]--------".formatted(SimpleTownyProduct.INSTANCE.configuration.getPrefix())).color(NamedTextColor.BLUE));
        components.add(blankLine());
        components.addAll(body);
        components.add(blankLine());
        components.add(Component.text("---------[%s]--------".formatted(SimpleTownyProduct.INSTANCE.configuration.getPrefix())).color(NamedTextColor.BLUE));
        components.add(blankLine());
        return components;
    }

    public static Component blankLine(){
        return Component.empty();
    }

    public static void checkPermission(Player player, String permission) {
        if(player.isOp() || player instanceof ConsoleCommandSender){
            return;
        }
        if (!player.hasPermission(permission)) {
            Message message = SimpleTownyProduct.INSTANCE.getMessage();
            sendError(player, message.getCommonNoPermission());
        }
    }
}
