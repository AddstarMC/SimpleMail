package me.odium.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.google.common.base.Charsets;

import me.odium.test.simplemail;


public class DBConnection {
    private static DBConnection instance = new DBConnection();
    public Connection con = null;
    public  int Timeout = 30;
    public Statement stmt;

    public simplemail plugin;

    private DBConnection() {
    }

    public static synchronized DBConnection getInstance() {
        return instance;
    }
    
    /**
     * We set the plugin that is to be used for these connections.
     * @param plugin
     */
    public void setPlugin(simplemail plugin) {
        this.plugin = plugin;
    }

    public boolean setConnection() throws Exception {
        String host = plugin.getConfig().getString("Mysql.Host");
        String user = plugin.getConfig().getString("Mysql.User");
        String pass = plugin.getConfig().getString("Mysql.Pass");
        String db = plugin.getConfig().getString("Mysql.Database");
        try {
            Class.forName("com.mysql.jdbc.Driver");
        	con = DriverManager.getConnection("jdbc:mysql://" + host + "/" + db, user, pass);
        } catch (SQLException e) {
            plugin.getLogger().severe("Unable to open database!");
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
        	plugin.getLogger().severe("Unable to find a suitable MySQL driver!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Connection getConnection() {
        return con;
    }

    public void closeConnection() {
        try { con.close(); } catch (Exception ignore) {}
    }

    public void createTable() {
        Statement stmt;
        try {
            stmt = con.createStatement();
            String queryC = "CREATE TABLE IF NOT EXISTS SM_Mail (id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT, sender_id char(36), sender varchar(16), target_id char(36), target varchar(16), date datetime, message text, isread tinyint(1), expiration datetime)";
            stmt.executeUpdate(queryC);
        } catch(Exception e) {
            plugin.log.log(Level.SEVERE, "An error occured while creating the table", e);
        }
    }
    
    private String getOfflineUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer: " + name).getBytes(Charsets.UTF_8)).toString();
    }
    
    public void convertTable() {
        Statement statement = null;
        try {
            statement = con.createStatement();
            
            // Check table status, do schema check
            try {
                statement.executeQuery("SELECT sender_id,target_id FROM SM_Mail LIMIT 0").close();
                // Passed so we are updated
                return;
            } catch (SQLException e) {
                // Ignore
            }
            
            con.setAutoCommit(false);
            
            // Update the schema
            statement.executeUpdate("ALTER TABLE SM_Mail ADD COLUMN (sender_id CHAR(36), target_id CHAR(36))");
            statement.executeUpdate("ALTER TABLE SM_Mail ADD INDEX idx_sender (sender_id)");
            statement.executeUpdate("ALTER TABLE SM_Mail ADD INDEX idx_target (target_id)");
            
            plugin.log.info("Converting SimpleMail database. This may take a while:");
            
            // Make a cache of the offline player files for fast resolution of names
            plugin.log.info("Building cache using offline player files...");
            HashMap<String, UUID> nameMap = new HashMap<String, UUID>();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                nameMap.put(player.getName().toLowerCase(), player.getUniqueId());
            }
            
            plugin.log.info("Begining convert using cache");
            PreparedStatement bulkUpdate = con.prepareStatement("UPDATE SM_Mail SET sender_id=?,target_id=? WHERE id=?");
            ResultSet results = statement.executeQuery("SELECT id,sender,target FROM SM_Mail");
            
            int converted = 0;
            int failed = 0;
            while(results.next()) {
                UUID senderId = nameMap.get(results.getString(2).toLowerCase());
                UUID targetId = nameMap.get(results.getString(3).toLowerCase());
                boolean fail = false;
                
                if (senderId != null) {
                    bulkUpdate.setString(1, senderId.toString());
                } else {
                    bulkUpdate.setString(1, getOfflineUUID(results.getString(2)));
                    fail = true;
                }
                
                if (targetId != null) {
                    bulkUpdate.setString(2, targetId.toString());
                } else {
                    bulkUpdate.setString(2, getOfflineUUID(results.getString(3)));
                    fail = true;
                }
                
                bulkUpdate.setInt(3, results.getInt(1));
                bulkUpdate.addBatch();
                ++converted;
                
                if (fail) {
                    ++failed;
                }
            }
            
            plugin.log.info("Resolved names of " + converted + " entries.");
            if (failed != 0) {
                plugin.log.info(failed + " were not resolved or only partially resolved using the player files.");
            }
            
            plugin.log.info("Pushing changes to DB");
            bulkUpdate.executeBatch();
            
            con.commit();
            
            plugin.log.info("Conversion complete. You are now using UUIDs");
            
            bulkUpdate.close();
            results.close();
            
        } catch(SQLException e) {
            plugin.log.log(Level.SEVERE, "An error occurede while converting the table", e);
            try {
                con.rollback();
            } catch (SQLException ex) {
                plugin.log.log(Level.SEVERE, "Unable to rollback the update", ex);
            }
        } finally {
            try {
                if (statement != null) { 
                    statement.close();
                }
                
                con.setAutoCommit(true);
            } catch (SQLException e) {
            }
            
            
        }
    }

    public void setStatement() throws Exception {
        if (con == null) {
            setConnection();
        }
        Statement stmt = con.createStatement();
        stmt.setQueryTimeout(Timeout);  // set timeout to 30 sec.
    }

    public  Statement getStatement() {
        return stmt;
    }

    public void executeStmt(String instruction) throws SQLException {
        stmt.executeUpdate(instruction);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone is not allowed.");
    }
}

//public class DBConnection {
//
//  public simplemail plugin;
//  public DBConnection(simplemail plugin)  {
//    this.plugin = plugin;
//  }
//
//  public Connection DBConnect() {
//    Connection con;
//    try{
//      Class.forName("org.sqlite.JDBC");
//      con = DriverManager.getConnection("jdbc:sqlite:test.db");
//      return con;
//    } catch(Exception e) {
//      System.err.println(e);
//    }
//    return null;
//  }
//  
//  public Statement DBStatement() {
//    java.sql.Statement stmt;
//    try {
//      stmt = DBConnect().createStatement();
//      return stmt;
//    } catch(Exception e) {
//      System.err.println(e);
//    }
//    return null;
//  }
//
//  public void DBCreatetable() {
//    Statement stmt;
//    try {
//      stmt = DBStatement();
//      String queryC = "CREATE TABLE IF NOT EXISTS SM_Mail (id INTEGER PRIMARY KEY, sender varchar(16), target varchar(16), date timestamp, message varchar(30), read varchar(10))";
//      stmt.executeUpdate(queryC);
//    } catch(Exception e) {
//      System.err.println(e);
//    }
//  }  
// 
//  public void DBcloseconnection() {
//    try{    
//      DBConnect().close();    
//    } catch(Exception e) {
//      System.err.println(e);
//    }
//  }
//
//}
