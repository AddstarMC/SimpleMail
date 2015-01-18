package me.odium.test.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import me.odium.test.DBConnection;
import me.odium.test.Lookup;
import me.odium.test.Lookup.LookupCallback;
import me.odium.test.simplemail;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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

	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 2) {
			sender.sendMessage("/sendmail <ExactPlayerName> <Message>");
			return true;
		}
		
		final String message = StringUtils.join(args, ' ', 1, args.length);
		
		Lookup.resolve(plugin, args[0], new LookupCallback() {
            @Override
            public void run(OfflinePlayer player) {
                if (player == null) {
                    sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "That player does not exist.");
                    return;
                }
                Connection con = null;
                Statement stmt = null;
                ResultSet rs = null;
                PreparedStatement ps = null;
                try {
                    con = service.getConnection();
                    stmt = con.createStatement();

                    rs = stmt.executeQuery("SELECT COUNT(target) AS inboxtotal FROM SM_Mail WHERE target_id='" + player.getUniqueId() + "'");
                    rs.next();
                    int MaxMailboxSize = plugin.getConfig().getInt("MaxMailboxSize");
                    if (rs.getInt("inboxtotal") >= MaxMailboxSize) {
                        sender.sendMessage(plugin.GRAY + "[SimpleMail] " + plugin.RED + "Player's Inbox is full");
                        return;
                    }
                    ps = con.prepareStatement("INSERT INTO SM_Mail " + "(sender_id, sender, target_id, target, date, message, isread, expiration) VALUES "
                            + "(?,?,?,?,NOW(),?,0,NULL);");
                    if (sender instanceof Player) {
                        ps.setString(2, ((Player)sender).getName());
                        ps.setString(1, ((Player)sender).getUniqueId().toString());
                    } else {
                        ps.setString(2, "Server");
                    }
                    
                    ps.setString(3, player.getUniqueId().toString());
                    ps.setString(4, player.getName());
                    
                    ps.setString(5, message);

                    ps.executeUpdate();

                    sender.sendMessage(plugin.GRAY + "[SimpleMail] " + ChatColor.GREEN + "Message Sent to: " + ChatColor.WHITE + player.getName());
                    String msg = plugin.GRAY + "[SimpleMail] " + plugin.GREEN + "You've Got Mail!" + plugin.GOLD + " [/Mail]";
                    if (player.isOnline()) {
                        player.getPlayer().sendMessage(msg);
                    } else {
                        plugin.SendPluginMessage("Message", player.getName(), msg);
                    }
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
            }
        });

		return true;
	}

}