package me.odium.test;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Lookup {
    public static void resolve(final Plugin plugin, final String name, final LookupCallback callback) {
        Player player = Bukkit.getPlayer(name);
        if (player != null) {
            callback.run(player);
            return;
        }
        
        // Do async lookup
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                final OfflinePlayer player = Bukkit.getOfflinePlayer(name);
                
                // Callback on the main thread
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (player.hasPlayedBefore()) {
                            callback.run(player);
                        } else {
                            callback.run(null);
                        }
                    }
                });
            }
        });
    }
    
    public interface LookupCallback {
        public void run(OfflinePlayer player);
    }
}
