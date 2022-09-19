
package psikuvit.parkour.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import psikuvit.parkour.Main;

import java.io.File;
import java.util.List;

public class HologramLocation
{
    private List<Location> locList;
    
    public HologramLocation(final List<Location> locList) {
        this.locList = locList;
    }
    
    public HologramLocation() {
    }
    
    public void reset() {
        final File file = new File(Main.getInstance().getDataFolder(), "holograms.dat");
        for (final String s : YamlConfiguration.loadConfiguration(file).getStringList("Locations")) {
            final String s2 = s.split(":")[0];
            final String s3 = s.split(":")[1];
            final String s4 = s.split(":")[2];
            final String s5 = s.split(":")[3];
            if (Bukkit.getWorld(s2) != null) {
                final Location location = new Location(Bukkit.getWorld(s2), Double.parseDouble(s3), Double.parseDouble(s4), Double.parseDouble(s5));
                if (location.getWorld() == null) {
                    continue;
                }
                for (final Entity entity : location.getWorld().getNearbyEntities(location, 5.0, 5.0, 5.0)) {
                    if (entity instanceof ArmorStand && location.equals(entity.getLocation())) {
                        entity.remove();
                    }
                }
            }
        }
        file.delete();
    }
    
    public void save() {
        final File file = new File(Main.getInstance().getDataFolder(), "holograms.dat");
        final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(file);
        final List<String> stringList = (loadConfiguration).getStringList("Locations");
        for (final Location location : this.locList) {
            stringList.add(location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ());
        }
        (loadConfiguration).set("Locations", stringList);
        try {
            (loadConfiguration).save(file);
            (loadConfiguration).load(file);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
