package me.odium.test.listeners;

import java.util.concurrent.ExecutionException;

import me.odium.test.DBConnection;
import me.odium.test.SimpleMailPlugin;
import me.odium.test.Statements;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PListener implements Listener {

	public SimpleMailPlugin plugin;
	public PListener(SimpleMailPlugin plugin) {    
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

  	private DBConnection service = DBConnection.getInstance();

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (plugin.getConfig().getBoolean("OnPlayerJoin.ShowNewMessages")) {
			final Player player = event.getPlayer();
			int Delay = plugin.getConfig().getInt("OnPlayerJoin.DelayInSeconds");
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					if (!player.isOnline()) return;
					
					try {
					    int count = service.executeQueryInt(Statements.InboxCountUnread, player.getUniqueId());
					    if(count > 1) {
                            player.sendMessage(SimpleMailPlugin.format("&7[SimpleMail] &aYou have &6%d&a new messages; see &b/inbox&a and &b/mail", count));
                        } else if(count == 1) {
							player.sendMessage(SimpleMailPlugin.format("&7[SimpleMail] &aYou have a new message; see &b/inbox&a and &b/mail"));
						}
					} catch (ExecutionException e) {
					    // Say nothing
					}
	    	    }
			}, Delay*20L);
		}
	}
}