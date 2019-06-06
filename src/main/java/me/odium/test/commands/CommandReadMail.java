package me.odium.test.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import me.odium.test.DBConnection;
import me.odium.test.Statements;
import me.odium.test.SimpleMailPlugin;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandReadMail extends AbstractCommand {

	private final SimpleMailPlugin plugin;

	public CommandReadMail(SimpleMailPlugin plugin) {
		this.plugin = plugin;
	}

	private final DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 1) {
			return false;
		}

		UUID senderId = null;
		if (sender instanceof Player) {
		    senderId = ((Player)sender).getUniqueId();
		}

		// Parse the message id
		Integer messageId = checkMessage(sender, args);
		if (messageId == null) return true;
		ResultSet rs = null;
		try {
			rs = service.executeQuery(Statements.ReadMail, messageId);
			if (!rs.next()) {
			    sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "The message does not exist");
				return true;
			}

			boolean isSender = false;
			boolean isTarget = false;

			// Note that sender_id will be null if the mail was sent by console
			// The sender field should still be valid
			String messageSender = rs.getString("sender_id");
			if (!IsNullOrEmpty(messageSender)) {
				UUID sentby = UUID.fromString(messageSender);
				isSender = sentby.equals(senderId);
			}

			String messageTarget = rs.getString("target_id");
			if (!IsNullOrEmpty(messageTarget)) {
				UUID target = UUID.fromString(messageTarget);
				isTarget = target.equals(senderId);
			}

			boolean isSpy = sender.hasPermission("SimpleMail.spy");
			if (!isSender && !isTarget && !isSpy) {
				sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "This is not your message to read.");
				return true;
			}

			String expiration = rs.getString("expiration");
			if (expiration == null || expiration.isEmpty()) {
				expiration = "None";
			} else {
				expiration = rs.getString("fexpiration");
			}

			String senderName = rs.getString("sender");
			if (IsNullOrEmpty(senderName))
				senderName = "[Unknown]";

			String targetName = rs.getString("target");
			if (IsNullOrEmpty(targetName))
				targetName = "[Unknown]";

			sender.sendMessage(ChatColor.GOLD + "Message Open: " + ChatColor.WHITE + rs.getString("id"));
			if (!isSender)
				sender.sendMessage(ChatColor.GRAY + " From: " + ChatColor.GREEN + senderName);
			else
			    sender.sendMessage(ChatColor.GRAY + " From: " + ChatColor.GREEN + "Me");
			if (!isTarget)
				sender.sendMessage(ChatColor.GRAY + " To: " + ChatColor.GREEN + targetName);
			else
			    sender.sendMessage(ChatColor.GRAY + " To: " + ChatColor.GREEN + "Me");
			sender.sendMessage(ChatColor.GRAY + " Date: " + ChatColor.WHITE + rs.getString("fdate"));
			sender.sendMessage(ChatColor.GRAY + " Expires: " + ChatColor.WHITE + expiration);
			sender.sendMessage(ChatColor.GRAY + " Message: " + ChatColor.WHITE + rs.getString("message"));

			if (isTarget && rs.getInt("isread") == 0) {
			    service.executeUpdate(Statements.MarkRead, plugin.getConfig().getString("MailExpiration"), messageId);
			}
		} catch (ExecutionException e) {
		    sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "An internal error occured while opening the mail");
		} catch (SQLException e) {
		    plugin.log.log(Level.SEVERE, "Error executing sql query", e);
		    sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "An internal error occured while opening the mail");
		} finally {
		    service.closeResultSet(rs);
		}
		return true;
	}

	// Returns true if value is null, empty, or is the literal string "null"
	private boolean IsNullOrEmpty(String value) {
		return StringUtils.isEmpty(value) || value.equalsIgnoreCase("null");
	}
}