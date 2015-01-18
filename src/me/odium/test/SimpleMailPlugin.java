package me.odium.test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import me.odium.test.commands.CommandClearMailbox;
import me.odium.test.commands.CommandDelMail;
import me.odium.test.commands.CommandInbox;
import me.odium.test.commands.CommandMail;
import me.odium.test.commands.CommandMailboxes;
import me.odium.test.commands.CommandOutbox;
import me.odium.test.commands.CommandPurgeMail;
import me.odium.test.commands.CommandReadMail;
import me.odium.test.commands.CommandSendMail;
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

public class SimpleMailPlugin extends JavaPlugin {
    public Logger log = getLogger();

    private DBConnection service = DBConnection.getInstance();

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
        // Create connection & table
        try {
            service.setPlugin(this);
            if (!service.setConnection()) {
                getLogger().severe("Database connectivion not successful! Aborting startup.");
                setEnabled(false);
                return;
            }
            service.createTable();
            service.convertTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        // Check for and delete any expired tickets, display progress.
        log.info(expireMail() + " Expired Messages Cleared");
    }

    public void onDisable() {
        // Check for and delete any expired tickets, display progress.
        log.info(expireMail() + " Expired Messages Cleared");
        // Close DB connection
        service.closeConnection();
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
        sender.sendMessage(ChatColor.GREEN + " /sendmail <player> <msg> " + ChatColor.WHITE + "- Send a message");
        sender.sendMessage(ChatColor.GREEN + " /readmail <id> " + ChatColor.WHITE + "- Read a message");
        sender.sendMessage(ChatColor.GREEN + " /delmail <id> " + ChatColor.WHITE + "- Delete a message");
        if (sender == null || sender.hasPermission("SimpleMail.admin")) {
            sender.sendMessage(ChatColor.GOLD + "[Admin Commands]");
            sender.sendMessage(ChatColor.AQUA + " /mailboxes " + ChatColor.WHITE + "- List active mailboxes");
            sender.sendMessage(ChatColor.AQUA + " /clearmailbox <playername> " + ChatColor.WHITE + "- Clear an active mailbox");
            sender.sendMessage(ChatColor.AQUA + " /purgemail " + ChatColor.WHITE + "- Purge expired messages from DB");
        }
    }

    public static String format(String format, Object... args) {
        return String.format(ChatColor.translateAlternateColorCodes('&', format), args);
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