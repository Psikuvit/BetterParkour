
package psikuvit.parkour.Utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import psikuvit.parkour.Main;
import psikuvit.parkour.ParkourLocation;

import java.util.ArrayList;
import java.util.List;

public class Hologram
{
    private List<String> lines;
    private final List<ArmorStand> armorStands;
    private ParkourLocation parkourLocation;
    private Location loc;
    private boolean enabled;
    
    public Hologram() {
        this.lines = new ArrayList<>();
        this.armorStands = new ArrayList<>();
        this.parkourLocation = null;
        this.loc = null;
        this.enabled = true;
    }
    
    public Hologram(final boolean b) {
        this.lines = new ArrayList<>();
        this.armorStands = new ArrayList<>();
        this.parkourLocation = null;
        this.loc = null;
        this.enabled = true;
        if (b) {
            this.enabled = Main.getInstance().getConfig().getBoolean("ParkourHologramsEnabled");
        }
    }
    
    public void setRawCenter(final Location loc) {
        this.loc = loc.clone().add(0, 3,0);
    }
    
    public Hologram setCenter(final Location location) {
        (this.loc = location.clone().add(0.0, 1.0, 0.0)).setX(this.loc.getBlockX() + 0.5);
        this.loc.setZ(this.loc.getBlockZ() + 0.5);
        return this;
    }

    
    public Hologram setLines(final List<String> lines) {
        this.lines = lines;
        return this;
    }
    
    public Hologram show() {
        if (!this.enabled) {
            return this;
        }
        for (final String s : this.lines) {
            if (this.loc != null && this.loc.getWorld() != null) {
                final ArmorStand armorStand = this.loc.getWorld().spawn(this.loc.clone().add(0.0, -0.4, 0.0), ArmorStand.class);
                this.loc = this.loc.clone().add(0.0, -0.4, 0.0);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setSmall(true);
                armorStand.setMarker(true);
                armorStand.setBasePlate(false);
                if (this.getParkourLocation() == null) {
                    armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', s));
                }
                else {
                    armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', s.replace("%checkpoint%", String.valueOf(this.getParkourLocation().getPosition()))));
                }
                armorStand.setCustomNameVisible(true);
                this.armorStands.add(armorStand);
            }
        }
        return this;
    }
    
    public List<Location> getLocations() {
        final ArrayList<Location> list = new ArrayList<>();
        for (ArmorStand armorStand : this.armorStands) {
            list.add(armorStand.getLocation());
        }
        return list;
    }
    
    public void destroy() {
        if (!this.enabled) {
            return;
        }
        for (ArmorStand armorStand : this.armorStands) {
            armorStand.remove();
        }
    }
    
    public Hologram setParkourLocation(final ParkourLocation parkourLocation) {
        this.parkourLocation = parkourLocation;
        return this;
    }
    
    public ParkourLocation getParkourLocation() {
        return this.parkourLocation;
    }
}
