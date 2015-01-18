package me.odium.test.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import me.odium.test.DBConnection;
import me.odium.test.Statements;
import me.odium.test.simplemail;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class readmail implements CommandExecutor {

	public simplemail plugin;

	public readmail(simplemail plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 1) {
			return false;
		}
		
		UUID senderId = null;
		if (sender instanceof Player) {
		    senderId = ((Player)sender).getUniqueId();
		}
		
		// Parse the message id
        int messageId;
        try {
            messageId = Integer.parseInt(args[0]);
            if (messageId < 0) {
                sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "The message does not exist");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "The message does not exist");
            return true;
        }
		
		ResultSet rs = null;
		try {
			rs = service.executeQuery(Statements.ReadMail, messageId);
			if (!rs.next()) {
			    sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "The message does not exist");
				return true;
			}

			UUID target = UUID.fromString(rs.getString("target_id"));
			UUID sentby = UUID.fromString(rs.getString("sender_id"));

			boolean isSender = sentby.equals(senderId);
			boolean isTarget = target.equals(senderId);
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

			sender.sendMessage(ChatColor.GOLD + "Message Open: " + ChatColor.WHITE + rs.getString("id"));
			if (!isSender)
				sender.sendMessage(ChatColor.GRAY + " From: " + ChatColor.GREEN + rs.getString("sender"));
			else
			    sender.sendMessage(ChatColor.GRAY + " From: " + ChatColor.GREEN + "Me");
			if (!isTarget)
				sender.sendMessage(ChatColor.GRAY + " To: " + ChatColor.GREEN + rs.getString("target"));
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
}