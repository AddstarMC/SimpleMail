package me.odium.test.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 23/06/2018.
 */
abstract class AbstractCommand implements CommandExecutor {

    Integer checkMessage(CommandSender sender, String[] args) {
        Integer messageId;
        try {
            messageId = Integer.parseInt(args[0]);
            if (messageId < 0) {
                sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "The message does not exist");
                return null;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "The message does not exist");
            return null;
        }
        return messageId;
    }
}
