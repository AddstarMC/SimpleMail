package me.odium.test.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import me.odium.test.DBConnection;
import me.odium.test.simplemail;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class sendmail implements CommandExecutor {

	public simplemail plugin;

	public sendmail(simplemail plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		if (args.length < 2) {
			sender.sendMessage("/sendmail <ExactPlayerName> <Message>");
			return true;
		}
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			con = service.getConnection();
			stmt = con.createStatement();

			StringBuilder sb = new StringBuilder();
			for (String arg : args)
				sb.append(arg + " ");
			String[] temp = sb.toString().split(" ");
			String[] temp2 = Arrays.copyOfRange(temp, 1, temp.length);
			sb.delete(0, sb.length());
			for (String details : temp2) {
				sb.append(details);
				sb.append(" ");
			}
			String details = sb.toString();
			String target = plugin.myGetPlayerName(args[0]).toLowerCase();

			rs = stmt.executeQuery("SELECT COUNT(target) AS inboxtotal FROM SM_Mail WHERE target='" + target + "'");
			rs.next();
			int MaxMailboxSize = plugin.getConfig().getInt("MaxMailboxSize");
			if (rs.getInt("inboxtotal") >= MaxMailboxSize) {
				sender.sendMessage(plugin.GRAY + "[SimpleMail] " + plugin.RED + "Player's Inbox is full");
				return true;
			}
			ps = con.prepareStatement("INSERT INTO SM_Mail " + "(sender, target, date, message, isread, expiration) VALUES "
					+ "(?,?,NOW(),?,0,NULL);");
			if (player == null) {
				ps.setString(1, "Server");
			} else {
				ps.setString(1, player.getName());
			}
			ps.setString(2, target);
			ps.setString(3, details);

			ps.executeUpdate();

			sender.sendMessage(plugin.GRAY + "[SimpleMail] " + ChatColor.GREEN + "Message Sent to: " + ChatColor.WHITE + target);
			String msg = plugin.GRAY + "[SimpleMail] " + plugin.GREEN + "You've Got Mail!" + plugin.GOLD + " [/Mail]";
			if (Bukkit.getPlayer(args[0]) != null) {
				Bukkit.getPlayer(args[0]).sendMessage(msg);
			} else {
				plugin.SendPluginMessage("Message", args[0], msg);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) { rs.close(); rs = null; }
				if (stmt != null) { stmt.close(); stmt = null; }
				if (ps != null) { ps.close(); ps = null; }
			} catch (SQLException e) {
				System.out.println("ERROR: Failed to close Statement, PreparedStatement or ResultSet!");
				e.printStackTrace();
			}
		}

		return true;
	}

}