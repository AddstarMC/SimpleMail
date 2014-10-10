package me.odium.test.commands;

import java.sql.Connection;
import java.sql.ResultSet;

import me.odium.test.DBConnection;
import me.odium.test.simplemail;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class readmail implements CommandExecutor {   

  public simplemail plugin;
  public readmail(simplemail plugin)  {
    this.plugin = plugin;
  }
  
  DBConnection service = DBConnection.getInstance();

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

      if (args.length != 1) {
        sender.sendMessage("/readmail <ID>");
        return true;
      }
      ResultSet rs;
      java.sql.Statement stmt;
      Connection con;
      try {
        con = service.getConnection();
        stmt = con.createStatement();

        rs = stmt.executeQuery("SELECT "
        		+ "id, sender, target, date, message, isread, expiration,"
        		+ "DATE_FORMAT(date, '%e/%b/%Y %H:%i') as fdate, "
        		+ "DATE_FORMAT(date, '%e/%b/%Y %H:%i') as fexpiration "
        		+ "FROM SM_Mail WHERE id='"+args[0]+"'");
        if (!rs.next()) {
            sender.sendMessage(plugin.GRAY+"[SimpleMail] "+plugin.RED+"This mail does not exist.");
            return true;
        }

        String target = rs.getString("target");
        String sentby = rs.getString("sender");
        
        boolean isSender = sentby.equals(sender.getName());
        boolean isTarget = target.equals(sender.getName());
        boolean isSpy    = sender.hasPermission("SimpleMail.spy");
        if (!isSender && !isTarget && !isSpy) {
            sender.sendMessage(plugin.GRAY+"[SimpleMail] "+plugin.RED+"This is not your message to read.");
            return true;
        }

        String date = rs.getString("date");
        String id = rs.getString("id");
        String expiration = rs.getString("expiration");        
        if (expiration == null || expiration.isEmpty()) {
          expiration = plugin.getExpiration(date);
        } else {
          expiration = rs.getString("fexpiration");
        }
        
        sender.sendMessage(plugin.GOLD+"Message Open: "+plugin.WHITE+id);        
        if (!sender.getName().equals(sentby))
          sender.sendMessage(plugin.GRAY+" From: " +plugin.GREEN+ rs.getString("sender"));
        if (!sender.getName().equals(target))
          sender.sendMessage(plugin.GRAY+" To: " +plugin.GREEN+ rs.getString("target"));
        sender.sendMessage(plugin.GRAY+" Date: " +plugin.WHITE+ rs.getString("fdate") );      
        sender.sendMessage(plugin.GRAY+" Expires: " +plugin.WHITE+ expiration);
        sender.sendMessage(plugin.GRAY+" Message: " +plugin.WHITE+ rs.getString("message"));

        if ((isTarget) && (rs.getInt("isread") == 0)) {
          String days = plugin.getConfig().getString("MailExpiration");
          stmt.executeUpdate("UPDATE SM_Mail SET isread=1, expiration=DATE_ADD(NOW(), INTERVAL "+ days +" DAY) WHERE id='"+args[0]+"'");
        }
        rs.close();
      } catch(Exception e) {
    	  plugin.getLogger().warning("Error reading mail! ID = " + args[0]);
    	  e.printStackTrace();
      }
      return true;
  }
}