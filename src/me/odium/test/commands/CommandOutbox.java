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

public class CommandOutbox implements CommandExecutor {

	public SimpleMailPlugin plugin;

	public CommandOutbox(SimpleMailPlugin plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "The outbox command cannot be used at console");
			return true;
		}

	ResultSet rs = null;
	    try {
	        rs = service.executeQuery(Statements.Outbox, player.getUniqueId());
	        sender.sendMessage(ChatColor.GOLD + "- ID ----- TO ----------- DATE ------");
	        while(rs.next()) {
	            int isread = rs.getInt("isread");
	            if (isread == 0) {
	                sender.sendMessage(SimpleMailPlugin.format("&7  [&a%d&7]         %s          %s", rs.getInt("id"), rs.getString("target"), rs.getString("fdate")));         
	            } else {
	                sender.sendMessage(SimpleMailPlugin.format("&7  [%d]         %s          %s", rs.getInt("id"), rs.getString("target"), rs.getString("fdate")));
	            }
	        }
	    } catch(ExecutionException e) {
	        sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "An internal error occured while reading your outbox");
	    } catch(SQLException e) {
	        plugin.log.log(Level.SEVERE, "Error executing sql query", e);
	        sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "An internal error occured while reading your outbox");
	    } finally {
	        service.closeResultSet(rs);
	    }

		return true;
	}
}