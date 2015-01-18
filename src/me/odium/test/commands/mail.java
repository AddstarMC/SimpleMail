package me.odium.test.commands;

import me.odium.test.simplemail;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class mail implements CommandExecutor {   

  public simplemail plugin;
  public mail(simplemail plugin)  {
    this.plugin = plugin;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }
    if (args.length == 0) {
      if (player == null) {
        sender.sendMessage(ChatColor.GOLD+"[ SimpleMail "+plugin.getDescription().getVersion()+" ]");
        sender.sendMessage(ChatColor.GREEN+" /inbox " +ChatColor.WHITE+"- Check your inbox");
        sender.sendMessage(ChatColor.GREEN+" /outbox " +ChatColor.WHITE+"- Display your outbox");
        sender.sendMessage(ChatColor.GREEN+" /sendmail <player> <msg> " +ChatColor.WHITE+"- Send a message");
        sender.sendMessage(ChatColor.GREEN+" /readmail <id> " +ChatColor.WHITE+"- Read a message");
        sender.sendMessage(ChatColor.GREEN+" /delmail <id> " +ChatColor.WHITE+"- Delete a message");
        sender.sendMessage(ChatColor.GOLD+"[Admin Commands]");
        sender.sendMessage(ChatColor.AQUA+" /mailboxes " +ChatColor.WHITE+"- List active mailboxes");
        sender.sendMessage(ChatColor.AQUA+" /clearmailbox <playername> " +ChatColor.WHITE+"- Clear an active mailbox");
        return true;
      }      
      plugin.displayHelp(sender);
      return true;
    } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
      if(player == null || player.hasPermission("simplemail.admin")) {
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GRAY+"[SimpleMail] "+ChatColor.GREEN + "Config Reloaded!");
        return true;
      } else {
        sender.sendMessage(ChatColor.GRAY+"[SimpleMail] "+ChatColor.RED + "You do not have permission");
      }
    }

    return true;    
  }

}