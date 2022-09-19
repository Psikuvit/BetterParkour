package psikuvit.parkour;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import psikuvit.parkour.Utils.Hologram;
import psikuvit.parkour.Utils.HologramLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parkour
{
    private final List<ParkourLocation> locations;
    private final List<Hologram> holograms;
    private List<String> blockCommands;
    private final Map<Integer, Integer> checkpointFallBacks;
    private final List<Location> statsLocations;
    private Location start;
    private Location end;
    private Location reset;
    private String name;
    private boolean LeaveRedirect;
    private boolean FinishRedirect;
    private boolean fallBack;
    private boolean deathFallback;
    private boolean automaticRespawnEnabled;
    private boolean checkpoint_required;
    private int fallbacky;
    private long delay;

    public Parkour() {
        this.locations = new ArrayList<>();
        this.holograms = new ArrayList<>();
        this.blockCommands = new ArrayList<>();
        this.checkpointFallBacks = new HashMap<>();
        this.statsLocations = new ArrayList<>();
        this.LeaveRedirect = false;
        this.FinishRedirect = false;
        this.fallBack = false;
        this.checkpoint_required = false;
        this.fallbacky = Integer.MAX_VALUE;
        this.delay = 0L;
    }
    
    public void loadHologram() {
        final Hologram show = new Hologram(true).setCenter(this.getStartLocation()).setLines(Main.getInstance().getHologram(this.getName(), "Hologram.ParkourStart")).show();
        final Hologram show2 = new Hologram(true).setCenter(this.getEndLocation()).setLines(Main.getInstance().getHologram(this.getName(), "Hologram.ParkourEnd")).show();
        this.holograms.add(show);
        this.holograms.add(show2);
        new HologramLocation(show.getLocations()).save();
        new HologramLocation(show2.getLocations()).save();
        for (final ParkourLocation parkourLocation : this.locations) {
            final Hologram show3 = new Hologram(true).setCenter(parkourLocation.getLocation()).setLines(Main.getInstance().getHologram(this.getName(), "Hologram.Checkpoint")).setParkourLocation(parkourLocation).show();
            new HologramLocation(show3.getLocations()).save();
            this.holograms.add(show3);
        }
    }
    
    public void loadBlockCommands() {
        this.blockCommands = (YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder(), "Parkours/" + this.getName() + ".yml"))).getStringList("BlockCommands");
    }
    
    public void loadStatsLocations() {
        final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder(), "Parkours/" + this.name + ".yml"));
        (loadConfiguration).options().copyDefaults(true);
        final ConfigurationSection configurationSection = (loadConfiguration).getConfigurationSection("ParkourStatsHologram");
        if (configurationSection != null) {
            for (String s : configurationSection.getKeys(false)) {
                final String string = (loadConfiguration).getString("ParkourStatsHologram." + s + ".Location");
                this.statsLocations.add(new Location(Bukkit.getWorld(string.split(":")[0]), Double.parseDouble(string.split(":")[1]), Double.parseDouble(string.split(":")[2]), Double.parseDouble(string.split(":")[3])));
            }
        }
    }
    
    public List<Location> getStatsLocations() {
        return this.statsLocations;
    }
    
    public List<Hologram> getHolograms() {
        return this.holograms;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public void setDelay(final long delay) {
        this.delay = delay;
    }
    
    public long getDelay() {
        return this.delay;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setResetLocation(final Location reset) {
        this.reset = reset;
        (this.reset = this.reset.clone()).setX(this.reset.getBlockX() + 0.5);
        this.reset.setZ(this.reset.getBlockZ() + 0.5);
    }
    
    public boolean isCheckpointRequired() {
        return this.checkpoint_required;
    }
    
    public void setFallBack(final boolean fallBack) {
        this.fallBack = fallBack;
    }
    
    public void setDeathFallBack(final boolean deathFallback) {
        this.deathFallback = deathFallback;
    }
    
    public void setAutomaticRespawnEnabled(final boolean automaticRespawnEnabled) {
        this.automaticRespawnEnabled = automaticRespawnEnabled;
    }
    
    public void setFallBackY(final int fallbacky) {
        this.fallbacky = fallbacky;
    }
    
    public void setFinishRedirect(final boolean finishRedirect) {
        this.FinishRedirect = finishRedirect;
    }
    
    public void setLeaveRedirect(final boolean leaveRedirect) {
        this.LeaveRedirect = leaveRedirect;
    }
    
    public boolean isLeaveRedirect() {
        return this.LeaveRedirect;
    }
    
    public boolean isFinishRedirect() {
        return this.FinishRedirect;
    }
    
    public boolean isDeathFallBack() {
        return this.deathFallback;
    }
    
    public boolean isAutomaticRespawnEnabled() {
        return this.automaticRespawnEnabled;
    }
    
    public boolean isFallBack() {
        return this.fallBack;
    }
    
    public int getFallBackY() {
        return this.fallbacky;
    }
    
    public boolean hasCheckpointFallBack(final int i) {
        return this.checkpointFallBacks.containsKey(i);
    }
    
    public int getCheckpointFallBackY(final int i) {
        return this.checkpointFallBacks.get(i);
    }
    
    public void addCheckpointFallBack(final int i, final int j) {
        this.checkpointFallBacks.put(i, j);
    }
    
    public void setStartLocation(final Location start) {
        this.start = start;
    }
    
    public void setEndLocation(final Location end) {
        this.end = end;
    }
    
    public void addCheckPoint(final int n, final Location location) {
        this.locations.add(new ParkourLocation(n, location));
    }
    
    public void setCheckpointsRequired(final boolean checkpoint_required) {
        this.checkpoint_required = checkpoint_required;
    }
    
    public Location getCheckpoint(final int n) {
        Location location = null;
        for (final ParkourLocation parkourLocation : this.getCheckpoints()) {
            if (parkourLocation.getPosition() == n) {
                location = parkourLocation.getLocation();
            }
        }
        final Location clone = location.clone();
        for (final String s : (YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder(), "Parkours/" + this.getName() + ".yml"))).getStringList("Checkpoints")) {
            if (s.startsWith(clone.getWorld().getName() + ":" + clone.getBlockX() + ":" + clone.getBlockY() + ":" + clone.getBlockZ())) {
                final String[] split = s.split(":");
                if (split.length <= 3) {
                    continue;
                }
                clone.setYaw(Float.parseFloat(split[4]));
                clone.setPitch(Float.parseFloat(split[5]));
            }
        }
        clone.setX(clone.getBlockX() + 0.5);
        clone.setZ(clone.getBlockZ() + 0.5);
        return clone;
    }
    
    public List<Location> getCheckpointsLocations() {
        final ArrayList<Location> list = new ArrayList<>();
        for (final ParkourLocation parkourLocation : this.getCheckpoints()) {
            final Location clone = parkourLocation.getLocation().clone();
            clone.setX(clone.getBlockX() + 0.5);
            clone.setZ(clone.getBlockZ() + 0.5);
            list.add(parkourLocation.getLocation());
        }
        return list;
    }
    
    public List<ParkourLocation> getCheckpoints() {
        return this.locations;
    }
    
    public Location getResetLocation() {
        return this.reset;
    }
    
    public Location getStartLocation() {
        return this.start;
    }
    
    public Location getEndLocation() {
        return this.end;
    }
    
    public List<String> getBlockCommands() {
        return this.blockCommands;
    }
}
