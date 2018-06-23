package me.odium.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.google.common.base.Charsets;

import me.odium.test.SimpleMailPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class DBConnection {
    private static final DBConnection instance = new DBConnection();
    private Connection con = null;

    private PreparedStatement[] mStatements;

    private SimpleMailPlugin plugin;

    private DBConnection() {
    }

    public static synchronized DBConnection getInstance() {
        return instance;
    }
    
    /**
     * We set the plugin that is to be used for these connections.
     * @param plugin this plugin
     */
    public void setPlugin(SimpleMailPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean setConnection() {
        Properties props = new Properties();
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("Mysql.Host", "localhost");
        String db = config.getString("Mysql.Database", "simplemail");
        props.put("user", config.getString("Mysql.User", "username"));
        props.put("password", config.getString("Mysql.Pass", "password"));
        ConfigurationSection dbprops = config.getConfigurationSection("Mysql.Properties");
        if (dbprops != null) {
            for (Map.Entry<String, Object> entry : dbprops.getValues(false).entrySet()) {
                props.put(entry.getKey(), entry.getValue());
            }
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://" + host + "/" + db, props);
        	initialize();
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
    
    private void initialize() throws SQLException {
        mStatements = new PreparedStatement[Statements.values().length];
        int next = 0;
        for (Statements statement : Statements.values()) {
            mStatements[next++] = con.prepareStatement(statement.getSQL());
        }
    }

    public Connection getConnection() {
        return con;
    }

    public void closeConnection() {
        try {
            for (PreparedStatement statement : mStatements) {
                statement.close();
            }
            con.close();
        } catch (Exception ignore) {
        }
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
        try (Statement statement = con.createStatement()) {

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
            HashMap<String, UUID> nameMap = new HashMap<>();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                nameMap.put(player.getName().toLowerCase(), player.getUniqueId());
            }

            plugin.log.info("Begining convert using cache");
            PreparedStatement bulkUpdate = con.prepareStatement("UPDATE SM_Mail SET sender_id=?,target_id=? WHERE id=?");
            ResultSet results = statement.executeQuery("SELECT id,sender,target FROM SM_Mail");

            int converted = 0;
            int failed = 0;
            while (results.next()) {
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

        } catch (SQLException e) {
            plugin.log.log(Level.SEVERE, "An error occurede while converting the table", e);
            try {
                con.rollback();
            } catch (SQLException ex) {
                plugin.log.log(Level.SEVERE, "Unable to rollback the update", ex);
            }
        }
    }

    public void setStatement() throws Exception {
        if (con == null) {
            setConnection();
        }
        Statement stmt = con.createStatement();
        int timeout = 30;
        stmt.setQueryTimeout(timeout);  // set timeout to 30 sec.
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone is not allowed.");
    }
    
    private void setParameters(PreparedStatement statement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; ++i) {
            if (args[i] instanceof Number) {
                statement.setObject(i+1, args[i]);
            } else if (args[i] instanceof String) {
                statement.setString(i+1, (String)args[i]);
            } else {
                statement.setString(i+1, String.valueOf(args[i]));
            }
        }
    }
    
    public int executeUpdate(Statements type, Object... args) throws ExecutionException {
        try {
            PreparedStatement statement = mStatements[type.ordinal()];
            setParameters(statement, args);
            
            return statement.executeUpdate();
        } catch (SQLException e) {
            plugin.log.log(Level.SEVERE, "Error executing sql update", e);
            throw new ExecutionException(e);
        }
    }
    
    public ResultSet executeQuery(Statements type, Object... args) throws ExecutionException {
        try {
            PreparedStatement statement = mStatements[type.ordinal()];
            setParameters(statement, args);

            return statement.executeQuery();
        } catch (SQLException e) {
            plugin.log.log(Level.SEVERE, "Error executing sql query", e);
            throw new ExecutionException(e);
        }
    }
    
    // Shortcut method for queries that return a single int
    public int executeQueryInt(Statements type, Object... args) throws ExecutionException, IllegalStateException {
        ResultSet rs = null;
        try {
            rs = executeQuery(type, args);
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new IllegalStateException("This query does not return an int");
            }
        } catch (SQLException e) {
            plugin.log.log(Level.SEVERE, "Error executing sql query", e);
            throw new ExecutionException(e);
        } finally {
            closeResultSet(rs);
        }
        
    }
    
    public void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                plugin.log.log(Level.WARNING, "Unable to close statment", e);
            }
        }
    }
    
    public void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                if (!(rs.getStatement() instanceof PreparedStatement)) {
                    rs.getStatement().close();
                }
                rs.close();
            } catch (SQLException e) {
                plugin.log.log(Level.WARNING, "Unable to close ResultSet", e);
            }
        }
    }
}
