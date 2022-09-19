package psikuvit.parkour;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import psikuvit.parkour.ActionBarAPI.ActionBar;
import psikuvit.parkour.TitleAPI.Title;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerParkour
{
    private long start;
    private long end;
    private int checkpoint;
    private String name;
    private final Player p;
    private Parkour parkour;
    private boolean finished;
    private boolean logged;
    private Location current;
    private final List<Location> reached_checkpoints;
    private Map<Location, Integer> locs;
    private final Title title;
    private final ActionBar actionBar;
    
    public PlayerParkour(final Player p) {
        this.checkpoint = 0;
        this.reached_checkpoints = new ArrayList();
        this.locs = new HashMap<>();
        this.p = p;
        this.start = System.currentTimeMillis();
        this.title = new Title();
        this.actionBar = new ActionBar();
        this.title.setTitle(Main.getInstance().getMessage("Title.Begin").replace("%checkpoint%", String.valueOf(this.getCheckpoint())).replace("%time%", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.start))));
        this.title.setFadeIn(5);
        this.title.setStay(20);
        this.title.setFadeOut(5);
        if (Main.getInstance().isTitleSupported()) {
            this.title.sendTimes(Collections.singletonList(p));
            this.title.sendTitle(Collections.singletonList(p));
        }
        this.actionBar.setMessage(Main.getInstance().getMessage("ActionBar.Begin").replace("%checkpoint%", String.valueOf(this.getCheckpoint())).replace("%time%", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.start))));
        if (Main.getInstance().isActionBarSupported()) {
            this.actionBar.send(p);
        }
    }
    
    public void finish() {
        if (!this.finished) {
            this.end = System.currentTimeMillis();
            this.finished = true;
            this.title.setTitle(Main.getInstance().getMessage("Title.Ending").replace("%checkpoint%", String.valueOf(this.getCheckpoint())).replace("%time%", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.start))));
            this.title.setFadeIn(5);
            this.title.setStay(20);
            this.title.setFadeOut(5);
            if (Main.getInstance().isTitleSupported()) {
                this.title.sendTimes(Collections.singletonList(this.p));
                this.title.sendTitle(Collections.singletonList(this.p));
            }
            this.actionBar.setMessage(Main.getInstance().getMessage("ActionBar.Ending").replace("%checkpoint%", String.valueOf(this.getCheckpoint())).replace("%time%", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.start))));
            if (Main.getInstance().isActionBarSupported()) {
                this.actionBar.send(this.p);
            }
        }
    }
    
    public boolean isFinished() {
        return this.finished;
    }
    
    public Player getPlayer() {
        return this.p;
    }
    
    public String getCurrentTime() {
        return String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.start));
    }
    
    public String getTime() {
        return new SimpleDateFormat("mm:ss.SSS").format(this.end - this.start);
    }
    
    public String getTime(final long l) {
        return new SimpleDateFormat("mm:ss.SSS").format(l);
    }
    
    public long getRealTime() {
        return this.end - this.start;
    }
    
    public boolean isBetterTime(final long n) {
        return n > this.end - this.start;
    }
    
    public void setName(final String name) {
        this.name = name;
        this.loadCheckpoints();
    }
    
    public void loadCheckpoints() {
        this.locs = new HashMap<>();
        final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder(), "Parkours/" + this.name + ".yml"));
        int i = 1;
        for (String s : (loadConfiguration).getStringList("Checkpoints")) {
            final String[] split = s.split(":");
            this.locs.put(new Location(Bukkit.getWorld(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3])), i);
            ++i;
        }
    }
    
    public void resetTime() {
        this.start = System.currentTimeMillis();
        this.loadCheckpoints();
    }
    
    public void setStart(final long start) {
        this.start = start;
    }
    
    public void setParkour(final Parkour parkour) {
        this.parkour = parkour;
    }
    
    public Parkour getParkour() {
        return this.parkour;
    }
    
    public int checkReached(final Location location) {
        int intValue = 0;
        if (this.locs.containsKey(location)) {
            intValue = this.locs.get(location);
        }
        return intValue;
    }
    
    public void handleReached(final Location location, final int n) {
        this.locs.remove(location);
        this.reached_checkpoints.add(location);
        this.clearLocs(n);
    }
    
    public void clearLocs(final int n) {
        for (final Location location : this.getLocations()) {
            if (this.locs.get(location) < n) {
                this.locs.remove(location);
            }
        }
    }
    
    public void removeCheckpoint(final int n) {
        Object o = null;
        for (final Location location : this.locs.keySet()) {
            if (this.locs.get(location) == n) {
                o = location;
            }
        }
        if (o != null) {
            this.locs.remove(o);
        }
    }
    
    private List<Location> getLocations() {
        return new ArrayList(this.locs.keySet());
    }
    
    public List<Location> getReachedCheckpoints() {
        return this.reached_checkpoints;
    }
    
    public void saveCurrentLocation() {
        this.current = this.getPlayer().getLocation();
    }
    
    public void setCurrentLocation(final Location current) {
        this.current = current;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setPlayerCheckpoint(final int checkpoint) {
        this.checkpoint = checkpoint;
    }
    
    public int getCheckpoint() {
        return this.checkpoint;
    }
    
    public Location getCurrentLocation() {
        return this.current;
    }
    
    private boolean isLogged() {
        return this.logged;
    }
    
    public void log() {
        if (!Main.getInstance().isResumeEnabled()) {
            return;
        }
        if (this.isLogged()) {
            return;
        }
        this.logged = true;
        if (this.isFinished()) {
            return;
        }
        final File file = new File(Main.getInstance().getDataFolder(), "resumes");
        if (!file.exists()) {
            file.mkdirs();
        }
        this.saveCurrentLocation();
        final File file2 = new File(Main.getInstance().getDataFolder(), "resumes/" + this.getPlayer().getUniqueId().toString() + ".yml");
        final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(file2);
        (loadConfiguration).options().copyDefaults(true);
        (loadConfiguration).addDefault("RunOut", System.currentTimeMillis() + Main.getInstance().getConfig().getInt("ResumeDelay"));
        (loadConfiguration).addDefault("Name", this.getParkour().getName());
        (loadConfiguration).addDefault("Checkpoint", this.getCheckpoint());
        (loadConfiguration).addDefault("Delay", System.currentTimeMillis() - this.start);
        (loadConfiguration).addDefault("LastLocation.World", this.current.getWorld().getName());
        (loadConfiguration).addDefault("LastLocation.X", this.current.getX());
        (loadConfiguration).addDefault("LastLocation.Y", this.current.getY());
        (loadConfiguration).addDefault("LastLocation.Z", this.current.getZ());
        (loadConfiguration).addDefault("LastLocation.Yaw", this.current.getYaw());
        (loadConfiguration).addDefault("LastLocation.Pitch", this.current.getPitch());
        try {
            (loadConfiguration).save(file2);
            (loadConfiguration).load(file2);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void run() {
        if (Main.getInstance().isActionBarSupported()) {
            this.actionBar.setMessage(Main.getInstance().getMessage("ActionBar.Running").replace("%checkpoint%", String.valueOf(this.getCheckpoint())).replace("%time%", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.start))));
            this.actionBar.send(this.p);
        }
    }
}
