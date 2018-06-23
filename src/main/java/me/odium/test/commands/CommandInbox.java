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
import org.bukkit.entity.Player;

public class CommandInbox implements CommandExecutor {

	private final SimpleMailPlugin plugin;

	public CommandInbox(SimpleMailPlugin plugin) {
		this.plugin = plugin;
	}

	private final DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player;
		String targetName;
		ResultSet rs = null;
		try {
			if (sender instanceof Player) {
				player = (Player) sender;
				rs = service.executeQuery(Statements.Inbox, player.getUniqueId());
			} else {
				// Command called from console
				targetName = sender.getName();
				rs = service.executeQuery(Statements.InboxConsole, targetName);
			}
			sender.sendMessage(ChatColor.GOLD + "- ID ---- FROM ------------ DATE ----------");
			while (rs.next()) {
				int isread = rs.getInt("isread");
				int messageID = rs.getInt("id");

				String formattedMessageID;
				if (isread == 0) {
					formattedMessageID = Strings.padEnd(SimpleMailPlugin.format("&7 [&a%d&7]", messageID), 15, ' ');
				} else {
					formattedMessageID = Strings.padEnd(SimpleMailPlugin.format("&7 [%d]", messageID), 11, ' ');
				}
				String msgSender = Strings.padEnd(rs.getString("sender"), 17, ' ');

				sender.sendMessage(formattedMessageID + " " + msgSender + " " + rs.getString("fdate"));
			}

		} catch (ExecutionException e) {
			sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "An internal error occured while reading your inbox");
		} catch (SQLException e) {
			plugin.log.log(Level.SEVERE, "Error executing sql query", e);
			sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "An internal error occured while reading your inbox");
		} finally {
			service.closeResultSet(rs);
		}

		return true;
	}

}