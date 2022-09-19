package psikuvit.parkour.Storage;

import java.sql.Connection;
import org.bukkit.plugin.Plugin;

public abstract class Database
{
    protected Plugin plugin;
    
    protected Database(final Plugin plugin) {
        this.plugin = plugin;
    }
    
    public abstract Connection openConnection();
    
    public abstract boolean hasConnection();
    
    public abstract Connection getConnection();
    
    public abstract void closeConnection();
}
