package me.odium.test.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import me.odium.test.DBConnection;
import me.odium.test.simplemail;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class delmail implements CommandExecutor {

	public simplemail plugin;

	public delmail(simplemail plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		if (args.length != 1) {
			sender.sendMessage("/delmail <ID>");
			return true;
		}
		ResultSet rs = null;
		java.sql.Statement stmt = null;
		Connection con = null;
		try {
			con = service.getConnection();
			stmt = con.createStatement();

			rs = stmt.executeQuery("SELECT * FROM SM_Mail WHERE id='" + args[0] + "'");
			if (rs.next()) {
				if (!rs.getString("target_id").equalsIgnoreCase(player.getUniqueId().toString())) {
					sender.sendMessage(plugin.GRAY + "[SimpleMail] " + plugin.RED + "This is not your message to delete or it does not exist. ");
				} else {
					stmt.executeUpdate("DELETE FROM SM_Mail WHERE id='" + args[0] + "' AND target_id='" + player.getUniqueId().toString() + "'");
					sender.sendMessage(plugin.GRAY + "[SimpleMail] " + plugin.GREEN + "Message Deleted.");
				}
			}
		} catch (Exception e) {
			if (e.toString().contains("ResultSet closed")) {
				sender.sendMessage(plugin.GRAY + "[SimpleMail] " + plugin.RED + "This is not your message to delete or it does not exist.");
			} else if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException")) {
				sender.sendMessage("/delmail <id>");
			} else {
			    plugin.log.log(Level.SEVERE, "An error occured while deleting the mail", e);
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