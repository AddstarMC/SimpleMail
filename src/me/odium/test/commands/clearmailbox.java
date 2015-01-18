package me.odium.test.commands;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import me.odium.test.DBConnection;
import me.odium.test.Lookup;
import me.odium.test.Lookup.LookupCallback;
import me.odium.test.simplemail;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class clearmailbox implements CommandExecutor {

	public simplemail plugin;

	public clearmailbox(simplemail plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 1) {
			sender.sendMessage("/clearmailbox <player>");
			return true;
		}
		
		Lookup.resolve(plugin, args[0], new LookupCallback() {
            @Override
            public void run(OfflinePlayer player) {
                if (player == null) {
                    sender.sendMessage(ChatColor.GRAY + "[SimpleMain] " + ChatColor.GOLD + "Unknown player");
                    return;
                }
                
                Statement stmt = null;
                Connection con = null;
                try {
                    con = service.getConnection();
                    stmt = con.createStatement();
                    stmt.executeUpdate("DELETE FROM SM_Mail WHERE target_id='" + player.getUniqueId().toString() + "'");
                    sender.sendMessage(plugin.GRAY + "[SimpleMail] " + plugin.GREEN + "Mailbox Cleared.");
                } catch (Exception e) {
                    plugin.log.log(Level.SEVERE, "An error occured while clearing an inbox", e);
                    if (e.toString().contains("locked")) {
                        sender.sendMessage(plugin.GRAY + "[SimpleMail] " + plugin.GOLD + "The database is busy. Please wait a moment before trying again...");
                    } else {
                        sender.sendMessage(plugin.GRAY + "[SimpleMail] " + plugin.RED + "Error: " + plugin.WHITE + e);
                    }
                } finally {
                    try {
                        if (stmt != null) { stmt.close(); stmt = null; }
                    } catch (SQLException e) {
                        System.out.println("ERROR: Failed to close Statement!");
                        e.printStackTrace();
                    }
                }
            }
        });
		
		return true;
	}
}