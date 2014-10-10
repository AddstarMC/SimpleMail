package me.odium.test.listeners;

import java.sql.Connection;
import java.sql.ResultSet;

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
					String targetnick = player.getName().toLowerCase();
					Connection con;
					java.sql.Statement stmt;
					ResultSet rs;
					try {        
						con = service.getConnection();
						stmt = con.createStatement();
						rs = stmt.executeQuery("SELECT COUNT(target) AS inboxtotal FROM SM_Mail WHERE target='"+targetnick+"' AND isread=0");
						if (rs.next()) {
							final int total = rs.getInt("inboxtotal");
							if(total > 0) {
								player.sendMessage(plugin.GRAY+"[SimpleMail] "+plugin.GREEN+ "You have " + plugin.GOLD +total+plugin.GREEN+" new messages");
							}
						}
						rs.close();
					} catch(Exception e) {
						e.printStackTrace();
					}
	    	    }
			}, Delay*20L);
		}
	}
}