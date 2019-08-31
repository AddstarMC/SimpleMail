package me.odium.test.commands;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import me.odium.test.DBConnection;
import me.odium.test.Statements;
import me.odium.test.SimpleMailPlugin;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.monolith.lookup.Lookup;

public class CommandSendMail implements CommandExecutor {

  private final SimpleMailPlugin plugin;
  private final DBConnection service = DBConnection.getInstance();

  public CommandSendMail(SimpleMailPlugin plugin) {
    this.plugin = plugin;
  }

  public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length < 2) {
      sender.sendMessage("/sendmail <ExactPlayerName> <Message>");
      return true;
    }

    final String message = StringUtils.join(args, ' ', 1, args.length);

    try {
      Lookup.lookupPlayerName(args[0], (success, player, error) -> {
        if (!success) {
          sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "That player does not exist.");
          return;
        }

        try {

          int count = service.executeQueryInt(Statements.InboxCount, player.getUniqueId());
          int maxSize = plugin.getConfig().getInt("MaxMailboxSize");

          if (count >= maxSize) {
            sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "Player's Inbox is full");
            return;
          }

          Player senderPlayer;
          UUID senderUUID = null;
          String senderUsername;

          if (sender instanceof Player) {
            senderPlayer = (Player) sender;
            senderUUID = senderPlayer.getUniqueId();
            senderUsername = senderPlayer.getName();
          } else {
            // When sending from console, we want the sender to always be "Server"
            senderUsername = "Server";
          }

          service.executeUpdate(Statements.SendMail,
              (senderUUID),
              senderUsername,
              player.getUniqueId(),
              player.getName(),
              message);

          // Notify
          sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.GREEN
              + "Message Sent to: " + ChatColor.WHITE + player.getName());

          String msg = ChatColor.GRAY + "[SimpleMail] " + ChatColor.GREEN
              + "You've Got Mail!" + ChatColor.GOLD + " [/mail]";
          if (player.isLocal()) {
            player.getPlayer().sendMessage(msg);
          } else {
            plugin.sendPluginMessage("Message", player.getName(), msg);
          }

        } catch (ExecutionException e) {
          if (sender instanceof Player)
            sender.sendMessage(ChatColor.RED + "An internal error occured while "
                + "executing this command.");
          else
            sender.sendMessage(ChatColor.RED + "[SimpleMail] " + ChatColor.RED
                + "Error sending mail: " + e.getMessage());
        }
      });

    } catch (IllegalStateException e) {
      if (sender instanceof Player)
        sender.sendMessage(ChatColor.RED + "An internal error occured "
            + "while executing this command.");
      else
        sender.sendMessage(ChatColor.RED + "[SimpleMail] " + ChatColor.RED
            + "Error validating mail recipient: " + e.getMessage());

    }

    return true;
  }

}