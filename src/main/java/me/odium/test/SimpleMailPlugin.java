package me.odium.test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import au.com.addstar.monolith.lookup.Lookup;
import me.odium.test.commands.CommandClearMailbox;
import me.odium.test.commands.CommandDelMail;
import me.odium.test.commands.CommandInbox;
import me.odium.test.commands.CommandMail;
import me.odium.test.commands.CommandMailboxes;
import me.odium.test.commands.CommandOutbox;
import me.odium.test.commands.CommandPurgeMail;
import me.odium.test.commands.CommandReadMail;
import me.odium.test.commands.CommandSendMail;
import me.odium.test.commands.CommandSentBy;
import me.odium.test.commands.CommandSentTo;
import me.odium.test.listeners.PListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class SimpleMailPlugin extends JavaPlugin {
  public final Logger log = getLogger();
  private final DBConnection service = DBConnection.getInstance();

  public static String format(String format, Object... args) {
    return String.format(ChatColor.translateAlternateColorCodes('&', format), args);
  }

  public void onEnable() {
    // Load Config.yml
    FileConfiguration cfg = getConfig();
    FileConfigurationOptions cfgOptions = cfg.options();
    cfgOptions.copyDefaults(true);
    cfgOptions.copyHeader(true);
    saveConfig();

    // declare new listener
    new PListener(this);
    this.getCommand("mail").setExecutor(new CommandMail(this));
    this.getCommand("readmail").setExecutor(new CommandReadMail(this));
    this.getCommand("delmail").setExecutor(new CommandDelMail(this));
    this.getCommand("sendmail").setExecutor(new CommandSendMail(this));
    this.getCommand("clearmailbox").setExecutor(new CommandClearMailbox(this));
    this.getCommand("inbox").setExecutor(new CommandInbox(this));
    this.getCommand("outbox").setExecutor(new CommandOutbox(this));
    this.getCommand("mailboxes").setExecutor(new CommandMailboxes(this));
    this.getCommand("purgemail").setExecutor(new CommandPurgeMail(this));
    this.getCommand("sentby").setExecutor(new CommandSentBy(this));
    this.getCommand("sentto").setExecutor(new CommandSentTo(this));

    // Create connection & table
    try {
      service.setPlugin(this);
      if (!service.setConnection()) {
        getLogger().severe("Database connection not successful! Aborting startup.");
        setEnabled(false);
        return;
      }
      service.createTable();
      service.convertTable();
      // Check for and delete any expired tickets, display progress.
      log.info(expireMail() + " Expired Messages Cleared");
    } catch (Exception e) {
      log.severe("An error was encounted during database initialization");
      log.severe(e.getMessage());
      log.severe("Disabling plugin");
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }
    this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
  }

  public void onDisable() {
    // Check for and delete any expired tickets, display progress.
    if (service != null) {
      log.info(expireMail() + " Expired Messages Cleared");
      service.closeConnection();
    }
    // Close DB connection
  }

  /**
   * Get the mail expiration
   *
   * @param date a date
   * @return The date mail expires as a string
   */
  @SuppressWarnings("unused")
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
    SimpleDateFormat dtgFormat = new SimpleDateFormat("dd/MMM/yy HH:mm");
    return dtgFormat.format(expirationDate);
  }

  public int expireMail() {
    try {
      return service.executeUpdate(Statements.Purge);
    } catch (ExecutionException e) {
      return 0;
    }
  }

  public void displayHelp(CommandSender sender) {
    sender.sendMessage(ChatColor.GOLD + "[ SimpleMail " + getDescription().getVersion() + " ]");
    sender.sendMessage(ChatColor.GREEN + " /inbox " + ChatColor.WHITE + "- Check your inbox");
    sender.sendMessage(ChatColor.GREEN + " /outbox " + ChatColor.WHITE + "- Check your outbox");
    sender.sendMessage(ChatColor.GREEN + " /sendmail <player> <msg> "
        + ChatColor.WHITE + "- Send a message");
    sender.sendMessage(ChatColor.GREEN + " /readmail <id> " + ChatColor.WHITE
        + "- Read a message");
    sender.sendMessage(ChatColor.GREEN + " /delmail <id> " + ChatColor.WHITE
        + "- Delete a message");

    if (sender.hasPermission("SimpleMail.admin") || sender.hasPermission("SimpleMail.spy")) {
      sender.sendMessage(ChatColor.GOLD + "[Admin Commands]");
      sender.sendMessage(ChatColor.AQUA + " /sentby " + ChatColor.WHITE
          + "- Find messages sent by user; supports % for wildcard");
      sender.sendMessage(ChatColor.AQUA + " /sentto " + ChatColor.WHITE
          + "- Find messages sent to user; supports % for wildcard");

      if (sender.hasPermission("SimpleMail.admin")) {
        sender.sendMessage(ChatColor.AQUA + " /mailboxes <MaxRows> <NameFilter> " + ChatColor.WHITE
            + "- List active mailboxes; supports % sign for wildcard");
        sender.sendMessage(ChatColor.AQUA + " /clearmailbox <playername> "
            + ChatColor.WHITE + "- Clear an active mailbox (delete messages received by player)");
        sender.sendMessage(ChatColor.AQUA + " /purgemail " + ChatColor.WHITE
            + "- Purge expired messages from DB");
      }
    }
  }

  public void sendPluginMessage(String subchannel, String data1, String data2) {
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF(subchannel);
    out.writeUTF(data1);
    out.writeUTF(data2);
    Bukkit.getServer().sendPluginMessage(this, "BungeeCord", out.toByteArray());
  }

  /**
   * Public Method to allow integration of external plugins to send mail.
   *
   * @param senderUsername sender name
   * @param senderUUID     sender uuid
   * @param targetName     target name
   * @param mailmessage    message
   */
  @SuppressWarnings("unused")
  public void SendMailMessage(CommandSender sender, String senderUsername, UUID senderUUID,
                              String targetName, String mailmessage) {
    Lookup.lookupPlayerName(targetName, (success, player, error) -> {
      if (!success) {
        sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED
            + "That player does not exist.");
        return;
      }
      try {
        service.executeUpdate(Statements.SendMail, senderUUID, senderUsername,
            player.getUniqueId(), player.getName(), mailmessage);
      } catch (ExecutionException e) {
        if (sender instanceof Player)
          sender.sendMessage(ChatColor.RED
              + "An internal error occured while executing this command.");
        else
          sender.sendMessage(ChatColor.RED + "[SimpleMail] " + ChatColor.RED
              + "Error sending mail: " + e.getMessage());
      }

    });
  }
}