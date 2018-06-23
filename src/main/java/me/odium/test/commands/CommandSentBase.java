package me.odium.test.commands;

import com.google.common.base.Strings;
import me.odium.test.DBConnection;
import me.odium.test.SimpleMailPlugin;
import me.odium.test.Statements;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 23/06/2018.
 */
abstract class CommandSentBase implements CommandExecutor {

    private final DBConnection service = DBConnection.getInstance();
    private final SimpleMailPlugin plugin;

    CommandSentBase(SimpleMailPlugin plugin) {
        this.plugin = plugin;
    }

    void handleCommand(CommandSender sender, String targetName, boolean by) {
        ResultSet rs = null;
        try {
            rs = service.executeQuery(Statements.OutboxConsole, targetName);

            if (by)
                sender.sendMessage(ChatColor.GOLD + "Mail sent by " + ChatColor.AQUA + targetName);
            else
                sender.sendMessage(ChatColor.GOLD + "Mail sent to " + ChatColor.AQUA + targetName);

            sender.sendMessage(ChatColor.GOLD + "- ID ---- FROM ------------ TO -------------- DATE ----------");
            while (rs.next()) {
                int isread = rs.getInt("isread");
                int messageID = rs.getInt("id");

                String formattedMessageID;
                if (isread == 0) {
                    formattedMessageID = Strings.padEnd(SimpleMailPlugin.format("&7 [&a%d&7]", messageID), 15, ' ');
                } else {
                    formattedMessageID = Strings.padEnd(SimpleMailPlugin.format("&7 [%d]", messageID), 11, ' ');
                }
                String msgSender = Strings.padEnd(rs.getString("sender"), 17, ' ');
                String msgTarget = Strings.padEnd(rs.getString("target"), 17, ' ');

                sender.sendMessage(formattedMessageID + " " + msgSender + " " + msgTarget + " " + rs.getString("fdate"));
            }
        } catch (ExecutionException e) {
            sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "An internal error occured while finding mail by sender name");
        } catch (SQLException e) {
            plugin.log.log(Level.SEVERE, "Error executing sql query", e);
            sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "An internal error occured while finding mail by sender name");
        } finally {
            service.closeResultSet(rs);
        }
    }


}
