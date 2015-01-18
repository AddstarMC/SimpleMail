package me.odium.test.commands;

import java.util.concurrent.ExecutionException;

import me.odium.test.DBConnection;
import me.odium.test.Lookup;
import me.odium.test.Lookup.LookupCallback;
import me.odium.test.Statements;
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
                    sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.GOLD + "Unknown player");
                    return;
                }
                
                try {
                    service.executeUpdate(Statements.ClearMail, player.getUniqueId());
                    sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.GOLD + player.getName() + "'s mailbox has been cleared");
                } catch (ExecutionException e) {
                    sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "An internal error occured while executing this command");
                }
            }
        });
		
		return true;
	}
}