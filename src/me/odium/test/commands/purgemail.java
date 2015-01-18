package me.odium.test.commands;

import me.odium.test.simplemail;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class purgemail implements CommandExecutor {   

  public simplemail plugin;
  public purgemail(simplemail plugin)  {
      this.plugin = plugin;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
      sender.sendMessage(ChatColor.GRAY + "[SimpleMail] "+ChatColor.GRAY + "Purged " + ChatColor.GREEN + plugin.expireMail() + ChatColor.GRAY+" expired messages");
      return true;
  }
}
