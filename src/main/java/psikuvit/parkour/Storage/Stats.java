package psikuvit.parkour.Storage;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import psikuvit.parkour.Main;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Stats {
    public static ExecutorService es = Executors.newSingleThreadExecutor();

    public static MySQL sql;

    private String host;

    private String port;

    private String user;

    private String password;

    private String database;

    private boolean mysqlenabled = false;

    public Stats() {
        File file = new File(Main.getInstance().getDataFolder(), "Stats.yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        this.mysqlenabled = yamlConfiguration.getBoolean("MySQLEnabled");
    }

    public void register() {
        File file = new File(Main.getInstance().getDataFolder(), "Stats.yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        yamlConfiguration.options().copyDefaults(true);
        yamlConfiguration.addDefault("MySQLEnabled", false);
        yamlConfiguration.addDefault("hostname", "localhost");
        yamlConfiguration.addDefault("port", 3306);
        yamlConfiguration.addDefault("database", "database");
        yamlConfiguration.addDefault("username", "root");
        yamlConfiguration.addDefault("password", "password");
        try {
            yamlConfiguration.save(file);
            yamlConfiguration.load(file);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        this.host = yamlConfiguration.getString("hostname");
        this.port = yamlConfiguration.getString("port");
        this.database = yamlConfiguration.getString("database");
        this.user = yamlConfiguration.getString("username");
        this.password = yamlConfiguration.getString("password");
        this.mysqlenabled = yamlConfiguration.getBoolean("MySQLEnabled");
        if (!this.mysqlenabled)
            return;
        sql = new MySQL(Main.getInstance(), this.host, this.port, this.database, this.user, this.password);
        boolean bool = false;
        try {
            Connection connection = sql.openConnection();
            bool = connection.isValid(1000);
            MySQL.Update("CREATE TABLE IF NOT EXISTS parkour_records (id int NOT NULL AUTO_INCREMENT PRIMARY KEY, uuid varchar(50), parkourstats longtext)");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!bool) {
            Bukkit.getConsoleSender().sendMessage("not Connected!");
        } else {
            Bukkit.getConsoleSender().sendMessage("Connected!");
        }
    }

    public boolean isEnabled() {
        return this.mysqlenabled;
    }

    private void insertField(String paramString) {
        if (!this.mysqlenabled)
            return;
        if (!sql.isConnected())
            return;
        MySQL.Update("INSERT INTO parkour_records (uuid, parkourstats) VALUES ('" + paramString + "', '')");
    }

    public void updateField(String paramString1, String paramString2, Object paramObject) {
        if (!this.mysqlenabled)
            return;
        if (!sql.isConnected())
            return;
        if (getField(paramString1, paramString2) == null)
            insertField(paramString1);
        MySQL.Update("UPDATE parkour_records SET " + paramString2 + " = '" + paramObject + "' WHERE uuid = '" + paramString1 + "'");
    }

    public Object getField(String paramString1, String paramString2) {
        String str = executeStatement("SELECT * FROM parkour_records WHERE uuid='" + paramString1 + "'", paramString2);
        if (str == null)
            return null;
        return executeStatement("SELECT * FROM parkour_records WHERE uuid='" + paramString1 + "'", paramString2);
    }

    public List<String> getLeaderboard(String paramString) {
        List<String> list = new ArrayList();
        if (isEnabled())
            list = executeStatement("SELECT * FROM parkour_records");
        return list;
    }

    private List<String> executeStatement(final String query) {
        if (!this.mysqlenabled)
            return null;
        if (!sql.isConnected())
            return null;
        Future<?> future = es.submit(new Callable<List<String>>() {
            public List<String> call() {
                ArrayList<String> arrayList = new ArrayList();
                try {
                    if (!Stats.sql.hasConnection()) {
                        Stats.sql.closeConnection();
                        Stats.sql.openConnection();
                    }
                    Connection connection = Stats.sql.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        String str = resultSet.getString("uuid");
                        arrayList.add(str);
                    }
                    resultSet.close();
                    preparedStatement.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return arrayList;
            }
        });
        try {
            return (List<String>)future.get();
        } catch (Exception exception) {
            return new ArrayList<String>();
        }
    }

    private String executeStatement(final String query, final String raw) {
        if (!this.mysqlenabled)
            return null;
        if (!sql.isConnected())
            return null;
        Future<?> future = es.submit(new Callable<String>() {
            public String call() {
                String str = null;
                try {
                    if (!Stats.sql.hasConnection()) {
                        Stats.sql.closeConnection();
                        Stats.sql.openConnection();
                    }
                    Connection connection = Stats.sql.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.last();
                    if (resultSet.getRow() != 0) {
                        resultSet.first();
                        str = resultSet.getString(raw);
                    }
                    resultSet.close();
                    preparedStatement.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return str;
            }
        });
        try {
            return (String)future.get();
        } catch (Exception exception) {
            return null;
        }
    }
}
