package me.odium.test.listeners;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.odium.test.DBConnection;
import me.odium.test.simplemail;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PListener implements Listener {

	public simplemail plugin;
	public PListener(simplemail plugin) {    
		this.plugin = plugin;    
		plugin.getServer().getPluginManager().registerEvents(this, plugin);  
	}
  
  	DBConnection service = DBConnection.getInstance();

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (plugin.getConfig().getBoolean("OnPlayerJoin.ShowNewMessages")) {
			final Player player = event.getPlayer();
			int Delay = plugin.getConfig().getInt("OnPlayerJoin.DelayInSeconds");
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getServer().getPluginManager().getPlugin("SimpleMail"), new Runnable() {
				public void run() {
					if (!player.isOnline()) return;
					Connection con = null;
					java.sql.Statement stmt = null;
					ResultSet rs = null;
					try {        
						con = service.getConnection();
						stmt = con.createStatement();
						rs = stmt.executeQuery("SELECT COUNT(target) AS inboxtotal FROM SM_Mail WHERE target_id='"+player.getUniqueId().toString()+"' AND isread=0");
						if (rs.next()) {
							final int total = rs.getInt("inboxtotal");
							if(total > 0) {
								player.sendMessage(plugin.GRAY+"[SimpleMail] "+plugin.GREEN+ "You have " + plugin.GOLD +total+plugin.GREEN+" new messages");
							}
						}
						rs.close();
					} catch(Exception e) {
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
	    	    }
			}, Delay*20L);
		}
	}
}