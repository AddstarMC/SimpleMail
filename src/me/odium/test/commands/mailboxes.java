package me.odium.test.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import me.odium.test.DBConnection;
import me.odium.test.Statements;
import me.odium.test.simplemail;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class mailboxes implements CommandExecutor {

	public simplemail plugin;

	public mailboxes(simplemail plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	    ResultSet rs = null;
		try {
			rs = service.executeQuery(Statements.Mailboxes);
			sender.sendMessage(ChatColor.GOLD + "Active Inboxes: ");
			while (rs.next()) {
				sender.sendMessage(ChatColor.GRAY + " Mailbox: " + ChatColor.GREEN + rs.getString("target"));
			}
		} catch(ExecutionException e) {
	        sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "An internal error occured while reading active mailboxes");
	    } catch(SQLException e) {
	        plugin.log.log(Level.SEVERE, "Error executing sql query", e);
	        sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "An internal error occured while reading active mailboxes");
	    } finally {
	        service.closeResultSet(rs);
	    }

		return true;
	}

}