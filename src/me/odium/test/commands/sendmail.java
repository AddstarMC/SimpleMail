package me.odium.test.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

import me.odium.test.DBConnection;
import me.odium.test.simplemail;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class sendmail implements CommandExecutor {   

  public simplemail plugin;
  public sendmail(simplemail plugin)  {
    this.plugin = plugin;
  }
  
  DBConnection service = DBConnection.getInstance();

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }
  
      if (args.length < 2) {
        sender.sendMessage("/sendmail <ExactPlayerName> <Message>");
        return true;
      }
      Connection con;
      java.sql.Statement stmt;
      try {        
        con = service.getConnection();
        stmt = con.createStatement();

        StringBuilder sb = new StringBuilder();
        for (String arg : args)
          sb.append(arg + " ");            
            String[] temp = sb.toString().split(" ");
            String[] temp2 = Arrays.copyOfRange(temp, 1, temp.length);
            sb.delete(0, sb.length());
            for (String details : temp2)
            {
              sb.append(details);
              sb.append(" ");
            }
            String details = sb.toString();  
            String target = plugin.myGetPlayerName(args[0]).toLowerCase();

            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(target) AS inboxtotal FROM SM_Mail WHERE target='"+target+"'");
            rs2.next();
            int MaxMailboxSize = plugin.getConfig().getInt("MaxMailboxSize");
            if (rs2.getInt("inboxtotal") >= MaxMailboxSize) {
              sender.sendMessage(plugin.GRAY+"[SimpleMail] "+plugin.RED+"Player's Inbox is full");
              rs2.close();
              return true;
            }
            PreparedStatement statement = con.prepareStatement("INSERT INTO SM_Mail "
            		+ "(sender, target, date, message, isread, expiration) VALUES "
            		+ "(?,?,NOW(),?,0,NULL);");
            if (player == null) {              
              statement.setString(1, "Server");
            } else {
              statement.setString(1, player.getName()); 
            }
			statement.setString(2, target);
			statement.setString(3, details);
			  
			statement.executeUpdate();
			statement.close();
			  
			sender.sendMessage(plugin.GRAY+"[SimpleMail] "+ChatColor.GREEN + "Message Sent to: " +ChatColor.WHITE+ target);
			String msg = plugin.GRAY+"[SimpleMail] "+plugin.GREEN+"You've Got Mail!"+plugin.GOLD+" [/Mail]";
			if (Bukkit.getPlayer(args[0]) != null) {
			  Bukkit.getPlayer(args[0]).sendMessage(msg);
			} else {
			  plugin.SendPluginMessage("Message", args[0], msg);
			}
			return true;
      } catch(Exception e) {
    	e.printStackTrace();
      }
     
    return true;    
  }

}