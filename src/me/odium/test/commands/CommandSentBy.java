package me.odium.test.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import me.odium.test.DBConnection;
import me.odium.test.Statements;
import me.odium.test.SimpleMailPlugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSentBy implements CommandExecutor {

	public SimpleMailPlugin plugin;

	public CommandSentBy(SimpleMailPlugin plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		boolean isSpy = sender.hasPermission("SimpleMail.spy");

		if (!isSpy) {
			sender.sendMessage("No permission to use /sentby");
			return true;
		}
		
		if (args.length < 1) {
			sender.sendMessage("/sentby <PlayerName>");
			sender.sendMessage("(use % sign for wildcard)");
			return true;
		}

		String senderName = args[0];

		ResultSet rs = null;
		try {
			rs = service.executeQuery(Statements.OutboxConsole, senderName);

			sender.sendMessage(ChatColor.GOLD + "Mail sent by " + senderName);
			sender.sendMessage(ChatColor.GOLD + "- ID ----- TO ----------- DATE ------");
			while (rs.next()) {
				int isread = rs.getInt("isread");
				if (isread == 0) {
					sender.sendMessage(SimpleMailPlugin.format("&7  [&a%d&7]         %s          %s", rs.getInt("id"), rs.getString("target"), rs.getString("fdate")));
				} else {
					sender.sendMessage(SimpleMailPlugin.format("&7  [%d]         %s          %s", rs.getInt("id"), rs.getString("target"), rs.getString("fdate")));
				}
			}
		} catch (ExecutionException e) {
			sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "An internal error occured while finding mail by sender name");
		} catch (SQLException e) {
			plugin.log.log(Level.SEVERE, "Error executing sql query", e);
			sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "An internal error occured while finding mail by sender name");
		} finally {
			service.closeResultSet(rs);
		}

		return true;
	}
}