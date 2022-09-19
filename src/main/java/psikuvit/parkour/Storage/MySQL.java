package psikuvit.parkour.Storage;

import org.bukkit.plugin.Plugin;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL extends Database
{
    private final String user;
    private final String database;
    private final String password;
    private final String port;
    private final String hostname;
    private static Connection connection;
    static String url;

    
    public MySQL(final Plugin plugin, final String hostname, final String port, final String database, final String user, final String password) {
        super(plugin);
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        MySQL.connection = null;
    }
    
    @Override
    public Connection openConnection() {
        try {
            url = "jdbc:mysql://" + hostname + "/" + database;
            MySQL.connection = DriverManager.getConnection(url, user, password);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return MySQL.connection;
    }
    
    public boolean isConnected() {
        return MySQL.connection != null;
    }
    
    @Override
    public boolean hasConnection() {
        boolean b = true;
        try {
            if (!MySQL.connection.isValid(2000)) {
                b = false;
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return b;
    }
    
    @Override
    public Connection getConnection() {
        return MySQL.connection;
    }
    
    @Override
    public void closeConnection() {
        if (MySQL.connection != null) {
            try {
                MySQL.connection.close();
            }
            catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static void Update(final String s) {
        Stats.es.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!MySQL.connection.isValid(2000)) {
                        MySQL.connection.close();
                        new Stats().register();
                    }
                }
                catch (SQLException ex) {
                    ex.printStackTrace();
                }
                try {
                    if (MySQL.connection != null) {
                        final Statement statement = MySQL.connection.createStatement();
                        statement.executeUpdate(s);
                        statement.close();
                    }
                }
                catch (SQLException ex2) {
                    ex2.printStackTrace();
                }
                MySQL.closeThread();
            }
        });
    }
    
    public static void closeThread() {
        Thread.currentThread().interrupt();
    }
}
