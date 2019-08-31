package me.odium.test.commands;

import java.util.concurrent.ExecutionException;

import me.odium.test.DBConnection;
import me.odium.test.Statements;
import me.odium.test.SimpleMailPlugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import au.com.addstar.monolith.lookup.Lookup;

public class CommandClearMailbox implements CommandExecutor {

  private final DBConnection service = DBConnection.getInstance();

  public CommandClearMailbox(SimpleMailPlugin plugin) {
  }

  public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length != 1) {
      sender.sendMessage("/clearmailbox <player>");
      return true;
    }

    Lookup.lookupPlayerName(args[0], (success, player, error) -> {
      if (!success) {
        sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.GOLD + "Unknown player");
        return;
      }

      try {
        service.executeUpdate(Statements.ClearMail, player.getUniqueId());
        sender.sendMessage(ChatColor.GRAY + "[SimpleMail] "
            + ChatColor.GOLD + player.getName() + "'s mailbox has been cleared");
      } catch (ExecutionException e) {
        sender.sendMessage(ChatColor.GRAY + "[SimpleMail] "
            + ChatColor.RED + "An internal error occured while executing this command");
      }
    });

    return true;
  }
}