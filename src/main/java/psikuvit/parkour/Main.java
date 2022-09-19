package psikuvit.parkour;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import psikuvit.parkour.PlaceholderAPIHook.ClipPlaceholder;
import psikuvit.parkour.Storage.Stats;
import psikuvit.parkour.Utils.HologramLocation;
import psikuvit.parkour.Utils.ParkourUtils;
import psikuvit.parkour.Utils.WorldGuardUtils;

import java.io.File;
import java.util.*;

public class Main extends JavaPlugin
{
    private final Map<String, String> permissions;
    private boolean actionBarSupported;
    private final Map<String, String> messages;
    private String pr;
    private boolean titleSupported;
    private boolean autoResumeJoinEnabled;
    private static Main m;
    private int listDisplay;
    private int statsUpdateDelay;
    private final Map<String, List<String>> holograms;
    private boolean parkourPermissionsEnabled;
    private boolean permissionsEnabled;
    private int statsSize;
    private ParkourListener parkourListener;
    private boolean flyDetectionEnabled;
    private boolean teleportDetectionEnabled;
    private boolean resumeEnabled;

    public static String guardPL;
    private final Map<String, String> parkourPermissions;

    public boolean isAutoResumeJoinEnabled() {
        return this.autoResumeJoinEnabled;
    }

    
    public boolean isParkourPermissionsEnabled() {
        return this.parkourPermissionsEnabled;
    }
    
    public String getPrefix() {
        return this.pr;
    }
    
    public boolean isResumeEnabled() {
        return this.resumeEnabled;
    }
    
    public void onDisable() {
    }
    
    public boolean isTeleportDetectionEnabled() {
        return this.teleportDetectionEnabled;
    }
    
    public boolean isActionBarSupported() {
        return this.actionBarSupported;
    }

    public int getStatsSize() {
        return this.statsSize;
    }

    public Main() {
        this.pr = "§7[§aParkour§7] ";
        this.messages = new HashMap<>();
        this.holograms = new HashMap<>();
        this.autoResumeJoinEnabled = false;
        this.resumeEnabled = false;
        this.flyDetectionEnabled = false;
        this.teleportDetectionEnabled = false;
        this.titleSupported = false;
        this.actionBarSupported = false;
        this.listDisplay = 10;
        this.parkourPermissions = new HashMap<>();
        this.permissions = new HashMap<>();
    }
    
    public boolean isTitleSupported() {
        return this.titleSupported;
    }
    
    public int getListDisplay() {
        return this.listDisplay;
    }
    
    public String getMessage(final String s) {
        return this.messages.get(s);
    }
    
    public void setupConfiguration() {
        this.permissions.clear();
        this.messages.clear();
        this.holograms.clear();
        final File file = new File(getInstance().getDataFolder(), "config.yml");
        final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(file);
        (loadConfiguration).options().copyDefaults(true);
        (loadConfiguration).addDefault("Permissions.Enabled", true);
        (loadConfiguration).addDefault("Permissions.Reset", "parkour.reset");
        (loadConfiguration).addDefault("Permissions.Checkpoint", "parkour.checkpoint");
        (loadConfiguration).addDefault("Permissions.Leave", "parkour.leave");
        (loadConfiguration).addDefault("Permissions.Resume", "parkour.resume");
        (loadConfiguration).addDefault("Permissions.Leaderboard", "parkour.leaderboard");
        (loadConfiguration).addDefault("ParkourPermissions.Enabled", false);
        (loadConfiguration).addDefault("ParkourPermissionList.yourParkourName.Permission", "Parkour.join.yourParkourName");
        (loadConfiguration).addDefault("Hologram.ParkourStart", "&e&lParkour%next%&a&lStart");
        (loadConfiguration).addDefault("Hologram.ParkourEnd", "&e&lParkour%next%&c&lEnd");
        (loadConfiguration).addDefault("Hologram.Checkpoint", "&e&lCheckpoint%next%&b #%checkpoint%");
        (loadConfiguration).addDefault("Hologram.StatsHeader", "&7Current Stats of Parkour%next%&c%name%");
        (loadConfiguration).addDefault("Hologram.StatsList", "&e%place_id%&7. &a%player% &7- &a%formatted_time%");
        (loadConfiguration).addDefault("Hologram.StatsSize", 10);
        (loadConfiguration).addDefault("Hologram.StatsUpdateDelay", 30);
        (loadConfiguration).addDefault("DelayedTicks", 50);
        (loadConfiguration).addDefault("ResumeDelay", 86400000);
        (loadConfiguration).addDefault("AutoResumeJoinEnabled", false);
        (loadConfiguration).addDefault("ResumeEnabled", false);
        (loadConfiguration).addDefault("FlyDetectionEnabled", true);
        (loadConfiguration).addDefault("TeleportDetectionEnabled", true);
        (loadConfiguration).addDefault("ParkourHologramsEnabled", true);
        (loadConfiguration).addDefault("TitleSupport", true);
        (loadConfiguration).addDefault("ActionBarSupport", true);
        (loadConfiguration).addDefault("Message.Prefix", "&7[&aParkour&7] ");
        (loadConfiguration).addDefault("Message.NoPermission", "%prefix% &cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
        (loadConfiguration).addDefault("Message.NoRace", "%prefix% &cYou are currently not in a parkour race.");
        (loadConfiguration).addDefault("Message.CommandLeave", "%prefix% &aYou stopped the parkour.");
        (loadConfiguration).addDefault("Message.FlyOnStart", "%prefix% &c&lYou must not be flying to start the parkour!");
        (loadConfiguration).addDefault("Message.FlyOnEnd", "%prefix% &c&lYou must not be flying to end the parkour!");
        (loadConfiguration).addDefault("Message.ParkourStart", "%prefix% &a&lParkour challenge started! Use &e&l/parkour reset &a&lto restart!");
        (loadConfiguration).addDefault("Message.ParkourFinishFailed", "%prefix% &a&lYour time of &e&l%time% &a&ldid not beat your previous record of &e&l%old_time%&a&l! Try again to beat your old record!");
        (loadConfiguration).addDefault("Message.ParkourFinishNotFailed", "%prefix% &a&lYou beat your previous record of &e&l%old_time% &a&lwith &e&l%time%&a&l!");
        (loadConfiguration).addDefault("Message.ParkourFinish", "%prefix% &a&lCongratulations on completing the parkour! You finished in &e&l%time%&a&l! Try again to get an even better record!");
        (loadConfiguration).addDefault("Message.ParkourTimeReset", "%prefix% &a&lReset your timer to 00:00! Get to the finish line!");
        (loadConfiguration).addDefault("Message.NotStarted", "%prefix% &a&lThis is the finish line for the parkour! Get to the start line and climb back up here!");
        (loadConfiguration).addDefault("Message.ReachedCheckpoint", "%prefix% &a&lYou reached &e&lCheckpoint #%checkpoint%&a&l. You can type &e&l/parkour checkpoint &a&lto get back to this place.");
        (loadConfiguration).addDefault("Message.FlyDetected", "%prefix% &c&lParkour challenge failed! Do not fly!");
        (loadConfiguration).addDefault("Message.TeleportDetected", "%prefix% &c&lParkour challenge failed! Do not teleport!");
        (loadConfiguration).addDefault("Message.Delay", "%prefix% &cYou need to wait until %formatted_time% to do this parkour again!");
        (loadConfiguration).addDefault("Message.CheckpointsRequired", "%prefix% &cYou haven't reached your last checkpoint!");
        (loadConfiguration).addDefault("Message.Resume", "%prefix% &cYou resumed your last parkour!");
        (loadConfiguration).addDefault("Message.ResumeFailed", "%prefix% &cYou don't have a parkour to resume!");
        (loadConfiguration).addDefault("Message.NoParkourPermission", "%prefix% &cYou don't have &e%permission% &cPermission to join this Parkour!");
        (loadConfiguration).addDefault("Message.Title.Begin", "&eBegin!");
        (loadConfiguration).addDefault("Message.Title.Ending", "&aYou finished it!");
        (loadConfiguration).addDefault("Message.ActionBar.Begin", "&eBegin!");
        (loadConfiguration).addDefault("Message.ActionBar.Ending", "&aYou finished it!");
        (loadConfiguration).addDefault("Message.ActionBar.Running", "&e%time% seconds");
        (loadConfiguration).addDefault("Leaderboard.ListDisplay", 10);
        (loadConfiguration).addDefault("Leaderboard.Message.ParkourNotExist", "&cParkour &7%name% &cdoes not exist!");
        (loadConfiguration).addDefault("Leaderboard.Message.PlaceRegistered", "&aPlace &7%place_id%&a. &e%player% &7(&c%formatted_time%&7)");
        (loadConfiguration).addDefault("Leaderboard.Message.PlaceUnregistered", "&aPlace &7%place_id%&a. &e<?> &7(&c%formatted_time%&7)");
        try {
            (loadConfiguration).save(file);
            (loadConfiguration).load(file);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        for (final String str : (loadConfiguration).getConfigurationSection("ParkourPermissionList").getKeys(false)) {
            this.parkourPermissions.put(str, (loadConfiguration).getString("ParkourPermissionList." + str + ".Permission"));
        }
        this.permissionsEnabled = (loadConfiguration).getBoolean("Permissions.Enabled");
        this.parkourPermissionsEnabled = (loadConfiguration).getBoolean("ParkourPermissions.Enabled");
        this.permissions.put("Permissions.Reset", (loadConfiguration).getString("Permissions.Reset"));
        this.permissions.put("Permissions.Checkpoint", (loadConfiguration).getString("Permissions.Checkpoint"));
        this.permissions.put("Permissions.Leave", (loadConfiguration).getString("Permissions.Leave"));
        this.permissions.put("Permissions.Resume", (loadConfiguration).getString("Permissions.Resume"));
        this.permissions.put("Permissions.Leaderboard", (loadConfiguration).getString("Permissions.Leaderboard"));
        this.holograms.put("Hologram.StatsHeader", Arrays.asList(ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Hologram.StatsHeader")).split("%next%")));
        this.messages.put("StatsList", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Hologram.StatsList")));
        this.statsSize = (loadConfiguration).getInt("Hologram.StatsSize");
        this.statsUpdateDelay = (loadConfiguration).getInt("Hologram.StatsUpdateDelay");
        this.holograms.put("Hologram.ParkourStart", Arrays.asList(ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Hologram.ParkourStart")).split("%next%")));
        this.holograms.put("Hologram.ParkourEnd", Arrays.asList(ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Hologram.ParkourEnd")).split("%next%")));
        this.holograms.put("Hologram.Checkpoint", Arrays.asList(ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Hologram.Checkpoint")).split("%next%")));
        this.autoResumeJoinEnabled = (loadConfiguration).getBoolean("AutoResumeJoinEnabled");
        this.resumeEnabled = (loadConfiguration).getBoolean("ResumeEnabled");
        this.flyDetectionEnabled = (loadConfiguration).getBoolean("FlyDetectionEnabled");
        this.teleportDetectionEnabled = (loadConfiguration).getBoolean("TeleportDetectionEnabled");
        this.titleSupported = (loadConfiguration).getBoolean("TitleSupport");
        this.actionBarSupported = (loadConfiguration).getBoolean("ActionBarSupport");
        this.pr = ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.Prefix"));
        this.messages.put("Message.NoPermission", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.NoPermission")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.NoRace", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.NoRace")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.CommandLeave", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.CommandLeave")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.FlyOnStart", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.FlyOnStart")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.FlyOnEnd", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.FlyOnEnd")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.ParkourStart", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.ParkourStart")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.ParkourFinishFailed", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.ParkourFinishFailed")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.ParkourFinishNotFailed", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.ParkourFinishNotFailed")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.ParkourFinish", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.ParkourFinish")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.ParkourTimeReset", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.ParkourTimeReset")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.NotStarted", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.NotStarted")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.ReachedCheckpoint", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.ReachedCheckpoint")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.FlyDetected", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.FlyDetected")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.TeleportDetected", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.TeleportDetected")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.Delay", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.Delay")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.CheckpointsRequired", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.CheckpointsRequired")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.Resume", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.Resume")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.ResumeFailed", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.ResumeFailed")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Message.NoParkourPermission", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.NoParkourPermission")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Leaderboard.Message.ParkourNotExist", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Leaderboard.Message.ParkourNotExist")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Leaderboard.Message.PlaceRegistered", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Leaderboard.Message.PlaceRegistered")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Leaderboard.Message.PlaceUnregistered", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Leaderboard.Message.PlaceUnregistered")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Title.Begin", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.Title.Begin")).replace("%prefix%", this.getPrefix()));
        this.messages.put("Title.Ending", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.Title.Ending")).replace("%prefix%", this.getPrefix()));
        this.messages.put("ActionBar.Begin", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.ActionBar.Begin")).replace("%prefix%", this.getPrefix()));
        this.messages.put("ActionBar.Ending", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.ActionBar.Ending")).replace("%prefix%", this.getPrefix()));
        this.messages.put("ActionBar.Running", ChatColor.translateAlternateColorCodes('&', (loadConfiguration).getString("Message.ActionBar.Running")).replace("%prefix%", this.getPrefix()));
        this.listDisplay = (loadConfiguration).getInt("Leaderboard.ListDisplay");
    }

    public boolean isPermissionsEnabled() {
        return this.permissionsEnabled;
    }

    public List<String> getHologram(final String replacement, final String s) {
        final List<String> list = this.holograms.get(s);
        final ArrayList<String> list2 = new ArrayList<>();
        for (String value : list) {
            list2.add(value.replace("%name%", replacement));
        }
        return list2;
    }
    
    public String getPermission(final String s) {
        return this.permissions.get(s);
    }
    
    public int getStatsUpdateDelay() {
        return this.statsUpdateDelay;
    }
    
    public void onEnable() {
        (Main.m = this).setupConfiguration();
        this.getCommand("parkour").setExecutor(new ParkourCommand());
        Bukkit.getPluginManager().registerEvents((this.parkourListener = new ParkourListener()), this);
        if (checkWG()) {
            guardPL = "worldguard";
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Hooked to WorldGuard");
        }
        (new WorldGuardUtils()).runTaskTimer(this, 0L,20L);

        new Stats().register();
        new BukkitRunnable() {
            public void run() {
                new HologramLocation().reset();
                ParkourUtils.loadParkours();
                ParkourUtils.loadStatsHologram();
            }
        }.runTaskLater(getInstance(), getInstance().getConfig().getInt("DelayedTicks"));
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClipPlaceholder().load();
        }
    }

    public static Main getInstance() {
        return Main.m;
    }

    public static boolean checkWG() {
        Plugin pWG = Bukkit.getPluginManager().getPlugin("WorldGuard");
        return (pWG != null);
    }
    
    public boolean isFlyDetectionEnabled() {
        return this.flyDetectionEnabled;
    }
    
    public String getParkourPermission(final String s) {
        if (this.parkourPermissions.containsKey(s)) {
            return this.parkourPermissions.get(s);
        }
        return null;
    }
    
    public ParkourListener getParkourListener() {
        return this.parkourListener;
    }

}
