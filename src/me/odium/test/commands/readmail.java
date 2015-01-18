package me.odium.test.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import me.odium.test.DBConnection;
import me.odium.test.simplemail;

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
			sender.sendMessage("/readmail <ID>");
			return true;
		}
		
		UUID senderId = null;
		if (sender instanceof Player) {
		    senderId = ((Player)sender).getUniqueId();
		}
		
		ResultSet rs = null;
		java.sql.Statement stmt = null;
		Connection con = null;
		try {
			con = service.getConnection();
			stmt = con.createStatement();

			rs = stmt.executeQuery("SELECT " + "id, sender_id, sender, target_id, target, date, message, isread, expiration," + "DATE_FORMAT(date, '%e/%b/%Y %H:%i') as fdate, "
					+ "DATE_FORMAT(date, '%e/%b/%Y %H:%i') as fexpiration " + "FROM SM_Mail WHERE id='" + args[0] + "'");
			if (!rs.next()) {
				sender.sendMessage(plugin.GRAY + "[SimpleMail] " + plugin.RED + "This mail does not exist.");
				return true;
			}

			String targetName = rs.getString("target");
			UUID target = UUID.fromString(rs.getString("target_id"));
			String sentbyname = rs.getString("sender");
			UUID sentby = UUID.fromString(rs.getString("sender_id"));

			boolean isSender = sentby.equals(senderId);
			boolean isTarget = target.equals(senderId);
			boolean isSpy = sender.hasPermission("SimpleMail.spy");
			if (!isSender && !isTarget && !isSpy) {
				sender.sendMessage(plugin.GRAY + "[SimpleMail] " + plugin.RED + "This is not your message to read.");
				return true;
			}

			String date = rs.getString("date");
			String id = rs.getString("id");
			String expiration = rs.getString("expiration");
			if (expiration == null || expiration.isEmpty()) {
				expiration = plugin.getExpiration(date);
			} else {
				expiration = rs.getString("fexpiration");
			}

			sender.sendMessage(plugin.GOLD + "Message Open: " + plugin.WHITE + id);
			if (!isSender)
				sender.sendMessage(plugin.GRAY + " From: " + plugin.GREEN + sentbyname);
			else
			    sender.sendMessage(plugin.GRAY + " From: " + plugin.GREEN + "Me");
			if (!isTarget)
				sender.sendMessage(plugin.GRAY + " To: " + plugin.GREEN + targetName);
			else
			    sender.sendMessage(plugin.GRAY + " To: " + plugin.GREEN + "Me");
			sender.sendMessage(plugin.GRAY + " Date: " + plugin.WHITE + rs.getString("fdate"));
			sender.sendMessage(plugin.GRAY + " Expires: " + plugin.WHITE + expiration);
			sender.sendMessage(plugin.GRAY + " Message: " + plugin.WHITE + rs.getString("message"));

			if ((isTarget) && (rs.getInt("isread") == 0)) {
				String days = plugin.getConfig().getString("MailExpiration");
				stmt.executeUpdate("UPDATE SM_Mail SET isread=1, expiration=DATE_ADD(NOW(), INTERVAL " + days + " DAY) WHERE id='" + args[0] + "'");
			}
		} catch (Exception e) {
			plugin.getLogger().warning("Error reading mail! ID = " + args[0]);
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) { rs.close(); rs = null; }
				if (stmt != null) { stmt.close(); stmt = null; }
			} catch (SQLException e) {
				System.out.println("ERROR: Failed to close Statement or ResultSet!");
				e.printStackTrace();
			}
		}
		return true;
	}
}