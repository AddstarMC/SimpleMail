package me.odium.test.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.odium.test.DBConnection;
import me.odium.test.simplemail;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class outbox implements CommandExecutor {

	public simplemail plugin;

	public outbox(simplemail plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		ResultSet rs = null;
		java.sql.Statement stmt = null;
		Connection con = null;
		try {
			con = service.getConnection();
			stmt = con.createStatement();

			rs = stmt.executeQuery("SELECT *, DATE_FORMAT(date, '%e/%b/%Y %H:%i') as fdate FROM SM_Mail WHERE sender_id='" + player.getUniqueId().toString() + "'");

			sender.sendMessage(plugin.GOLD + "- ID ----- TO ----------- DATE ------");
			while (rs.next()) {
				int isread = rs.getInt("isread");
				if (isread == 0) {
					sender.sendMessage(plugin.GRAY + "  [" + plugin.GREEN + rs.getInt("id") + plugin.GRAY + "]" + "         " + rs.getString("target") + "          "
							+ rs.getString("fdate"));
				} else {
					sender.sendMessage(plugin.GRAY + "  [" + rs.getInt("id") + plugin.GRAY + "]" + "         " + rs.getString("target") + "          "
							+ rs.getString("fdate"));
				}
			}
			sender.sendMessage(plugin.GRAY + "(deleted/expired messages will not be displayed)");
		} catch (Exception e) {
			plugin.log.info("[SimpleMail] " + "Error: " + e);
			if (e.toString().contains("locked")) {
				sender.sendMessage(plugin.GRAY + "[SimpleMail] " + plugin.GOLD + "The database is busy. Please wait a moment before trying again...");
			} else {
				player.sendMessage(plugin.GRAY + "[SimpleMail] " + plugin.RED + "Error: " + plugin.WHITE + e);
			}
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