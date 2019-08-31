package me.odium.test.commands;

import java.util.concurrent.ExecutionException;

import me.odium.test.DBConnection;
import me.odium.test.Statements;
import me.odium.test.SimpleMailPlugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandDelMail extends AbstractCommand {

  private final DBConnection service = DBConnection.getInstance();

  public CommandDelMail(SimpleMailPlugin plugin) {
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length != 1) {
      return false;
    }

    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }
    if (player == null) {
      sender.sendMessage("Console not supported for this command yet");
      return true;
    }
    // Parse the message id
    Integer messageId = checkMessage(sender, args);
    if (messageId == null) return true;
    try {
      // Check that the user owns the message and that it exists
      if (service.executeQueryInt(
          Statements.CheckMessageOwn, messageId, player.getUniqueId()) == 0) {
        sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED
            + "The message does not exist");
        return true;
      }

      // Delete it
      service.executeUpdate(Statements.Delete, messageId, player.getUniqueId());
      sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.GREEN + "Message Deleted.");
    } catch (ExecutionException e) {
      sender.sendMessage(ChatColor.RED + "An internal error occured while executing this command.");
    }

    return true;
  }
}