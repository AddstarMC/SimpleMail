package me.odium.test.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import com.google.common.base.Strings;
import me.odium.test.DBConnection;
import me.odium.test.Statements;
import me.odium.test.SimpleMailPlugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandMailboxes implements CommandExecutor {

  private final SimpleMailPlugin plugin;
  private final DBConnection service = DBConnection.getInstance();

  public CommandMailboxes(SimpleMailPlugin plugin) {
    this.plugin = plugin;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    ResultSet rs = null;

    String filterString = "%";
    int maxRecords = 100;

    if (args.length > 0) {
      try {
        maxRecords = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "First argument must be an integer");
        return true;
      }
    }

    if (args.length > 1) {
      filterString = args[1];
    }

    try {
      rs = service.executeQuery(Statements.Mailboxes, filterString, maxRecords);

      if (filterString.equalsIgnoreCase("%"))
        sender.sendMessage(ChatColor.GOLD + "Active Inboxes " + ChatColor.AQUA
            + "(first " + maxRecords + "): ");
      else
        sender.sendMessage(ChatColor.GOLD + "Active Inboxes " + ChatColor.AQUA
            + "(target Like '" + filterString + "', first " + maxRecords + "): ");

      sender.sendMessage(ChatColor.GOLD + "PLAYER ----------- MESSAGES -- UNREAD");
      while (rs.next()) {
        sender.sendMessage(ChatColor.GREEN
            + Strings.padEnd(rs.getString("target"), 19, ' ') +
            ChatColor.WHITE + Strings.padEnd(String.valueOf(rs.getInt("messages")),
            12, ' ') + rs.getInt("unread"));
      }

    } catch (ExecutionException e) {
      sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED
          + "An internal error occured while reading active mailboxes");
    } catch (SQLException e) {
      plugin.log.log(Level.SEVERE, "Error executing sql query", e);
      sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED
          + "An internal error occured while reading active mailboxes");
    } finally {
      service.closeResultSet(rs);
    }

    return true;
  }

}