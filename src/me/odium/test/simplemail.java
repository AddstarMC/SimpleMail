package me.odium.test;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import me.odium.test.commands.clearmailbox;
import me.odium.test.commands.delmail;
import me.odium.test.commands.inbox;
import me.odium.test.commands.mail;
import me.odium.test.commands.mailboxes;
import me.odium.test.commands.outbox;
import me.odium.test.commands.purgemail;
import me.odium.test.commands.readmail;
import me.odium.test.commands.sendmail;
import me.odium.test.listeners.PListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class simplemail extends JavaPlugin {
  public Logger log = Logger.getLogger("Minecraft");

  public ChatColor GREEN = ChatColor.GREEN;
  public ChatColor RED = ChatColor.RED;
  public ChatColor GOLD = ChatColor.GOLD;
  public ChatColor GRAY = ChatColor.GRAY;
  public ChatColor WHITE = ChatColor.WHITE; 
  public ChatColor AQUA = ChatColor.AQUA;

  DBConnection service = DBConnection.getInstance();

  public void onEnable(){    
    log.info("[" + getDescription().getName() + "] " + getDescription().getVersion() + " enabled.");
    // Load Config.yml
    FileConfiguration cfg = getConfig();
    FileConfigurationOptions cfgOptions = cfg.options();
    cfgOptions.copyDefaults(true);
    cfgOptions.copyHeader(true);
    saveConfig();
    // declare new listener
    new PListener(this);
    this.getCommand("mail").setExecutor(new mail(this));
    this.getCommand("readmail").setExecutor(new readmail(this));
    this.getCommand("delmail").setExecutor(new delmail(this));
    this.getCommand("sendmail").setExecutor(new sendmail(this));
    this.getCommand("clearmailbox").setExecutor(new clearmailbox(this));
    this.getCommand("inbox").setExecutor(new inbox(this));
    this.getCommand("outbox").setExecutor(new outbox(this));
    this.getCommand("mailboxes").setExecutor(new mailboxes(this));
    this.getCommand("purgemail").setExecutor(new purgemail(this));
    // Create connection & table
    try {
      service.setPlugin(this);
      if (!service.setConnection()) {
    	  getLogger().severe("Database connectivion not successful! Aborting startup.");
    	  setEnabled(false);
    	  return;
      }
      service.createTable();
    } catch(Exception e) {
      log.info("[SimpleMail] "+"Error: "+e); 
    }
    this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    // Check for and delete any expired tickets, display progress.
    log.info("[SimpleMail] "+expireMail()+" Expired Messages Cleared");
  }

  public void onDisable(){    
    // Check for and delete any expired tickets, display progress.
    log.info("[SimpleMail] "+expireMail()+" Expired Messages Cleared");
    // Close DB connection
    service.closeConnection();
    log.info("[" + getDescription().getName() + "] " + getDescription().getVersion() + " disabled.");
  }  

  public String myGetPlayerName(String name) { 
    Player caddPlayer = getServer().getPlayerExact(name);
    String pName;
    if(caddPlayer == null) {
      caddPlayer = getServer().getPlayer(name);
      if(caddPlayer == null) {
        pName = name;
      } else {
        pName = caddPlayer.getName();
      }
    } else {
      pName = caddPlayer.getName();
    }
    return pName;
  }

  public String getCurrentDTG (String string) {
    Calendar currentDate = Calendar.getInstance();
    SimpleDateFormat dtgFormat = new SimpleDateFormat ("dd/MMM/yy HH:mm");    
    return dtgFormat.format (currentDate.getTime());
  }

  public String getExpiration(String date) {  
    String mailExpiration = getConfig().getString("MailExpiration");
    for (char c : mailExpiration.toCharArray()) {
      if (!Character.isDigit(c)) {
        mailExpiration = "14";
      }
    }
    int expire = Integer.parseInt(mailExpiration);
    Calendar cal = Calendar.getInstance();
    cal.getTime();
    cal.add(Calendar.DAY_OF_WEEK, expire);
    java.util.Date expirationDate = cal.getTime();
    SimpleDateFormat dtgFormat = new SimpleDateFormat ("dd/MMM/yy HH:mm");    
    return dtgFormat.format (expirationDate);  
  }

  public int expireMail() {
    java.sql.Statement stmt;
    Connection con;
    int expirations = 0;
    try {
      con = service.getConnection();
      stmt = con.createStatement();
      expirations = stmt.executeUpdate("DELETE FROM SM_Mail WHERE expiration IS NOT NULL AND expiration < NOW()");
    } catch(Exception e) {
      log.info("[SimpleMail] "+"Error: "+e);
      e.printStackTrace();
    }  
    return expirations;
  }

  public void displayHelp(CommandSender sender) {
    sender.sendMessage(GOLD+"[ SimpleMail "+getDescription().getVersion()+" ]");
    sender.sendMessage(GREEN+" /inbox " +WHITE+"- Check your inbox");
    sender.sendMessage(GREEN+" /outbox " +WHITE+"- Check your outbox");
    sender.sendMessage(GREEN+" /sendmail <player> <msg> " +WHITE+"- Send a message");
    sender.sendMessage(GREEN+" /readmail <id> " +WHITE+"- Read a message");
    sender.sendMessage(GREEN+" /delmail <id> " +WHITE+"- Delete a message");
    if (sender == null || sender.hasPermission("SimpleMail.admin")) {     
      sender.sendMessage(GOLD+"[Admin Commands]");
      sender.sendMessage(AQUA+" /mailboxes " +WHITE+"- List active mailboxes");
      sender.sendMessage(AQUA+" /clearmailbox <playername> " +WHITE+"- Clear an active mailbox");      
      sender.sendMessage(AQUA+" /purgemail " +WHITE+"- Purge expired messages from DB");
    }
  }
  
  public void SendPluginMessage(String subchannel, String data1, String data2) {
	  ByteArrayDataOutput out = ByteStreams.newDataOutput();
	  out.writeUTF(subchannel);
	  out.writeUTF(data1);
	  out.writeUTF(data2);
	  Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
	  player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
  }
}