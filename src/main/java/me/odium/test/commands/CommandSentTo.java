package me.odium.test.commands;

import me.odium.test.SimpleMailPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandSentTo extends CommandSentBase {

  public CommandSentTo(SimpleMailPlugin plugin) {
    super(plugin);
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    boolean isSpy = sender.hasPermission("SimpleMail.spy");

    if (!isSpy) {
      sender.sendMessage("No permission to use /sentto");
      return true;
    }

    if (args.length < 1) {
      sender.sendMessage("/sentto <PlayerName>");
      sender.sendMessage("(use % sign for wildcard)");
      return true;
    }

    String targetName = args[0];
    handleCommand(sender, targetName, false);
    return true;
  }

}