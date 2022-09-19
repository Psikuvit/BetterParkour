package psikuvit.parkour.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import psikuvit.parkour.Main;
import psikuvit.parkour.Parkour;
import psikuvit.parkour.PlayerParkour;
import psikuvit.parkour.Storage.LeaderboardUtils;
import psikuvit.parkour.Storage.Stats;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class ParkourUtils
{
    private static List<Parkour> parkours;
    private static List<PlayerParkour> playerparkours;
    private static Map<Player, PlayerParkour> silentPlayerParkours;
    public static SimpleDateFormat format;
    private static MojangProfileReader mojangProfileReader;
    
    public static void loadStatsHologram() {
        new BukkitRunnable() {
            final List<Hologram> hologramList = new ArrayList();
            final Stats stats = new Stats();
            
            public void run() {
                for (Hologram value : this.hologramList) {
                    value.destroy();
                }
                this.hologramList.clear();
                if (this.stats.isEnabled()) {
                    final ArrayList<Location> list = new ArrayList();
                    for (final Parkour parkour : ParkourUtils.parkours) {
                        if (parkour.getStatsLocations().size() > 0) {
                            final String name = parkour.getName();
                            final Map<String, Long> players = LeaderboardUtils.getPlayers(name);
                            final Map<Integer, String> lowestValue = LeaderboardUtils.getLowestValue(players, Main.getInstance().getStatsSize());
                            final ArrayList<String> lines = new ArrayList(Main.getInstance().getHologram(name, "Hologram.StatsHeader"));
                            for (int i = 1; i <= Main.getInstance().getStatsSize(); ++i) {
                                String s = "";
                                final String message = Main.getInstance().getMessage("StatsList");
                                if (lowestValue.get(i) != null && players.get(lowestValue.get(i)) != null) {
                                    final Player player = Bukkit.getPlayer(UUID.fromString(lowestValue.get(i)));
                                    if (player != null) {
                                        if (player.getName() != null) {
                                            s = message.replace("%place_id%", i + "").replace("%player%", player.getName()).replace("%formatted_time%", ParkourUtils.format.format(players.get(lowestValue.get(i))));
                                        }
                                        else {
                                            s = message.replace("%place_id%", i + "").replace("%player%", ParkourUtils.mojangProfileReader.getName(player.getUniqueId().toString())).replace("%formatted_time%", ParkourUtils.format.format(players.get(lowestValue.get(i))));
                                        }
                                    }
                                    else {
                                        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(lowestValue.get(i)));
                                        if (offlinePlayer != null) {
                                            if (offlinePlayer.getName() != null) {
                                                s = message.replace("%place_id%", i + "").replace("%player%", offlinePlayer.getName()).replace("%formatted_time%", ParkourUtils.format.format(players.get(lowestValue.get(i))));
                                            }
                                            else {
                                                s = message.replace("%place_id%", i + "").replace("%player%", ParkourUtils.mojangProfileReader.getName(offlinePlayer.getUniqueId().toString())).replace("%formatted_time%", ParkourUtils.format.format(players.get(lowestValue.get(i))));
                                            }
                                        }
                                        else {
                                            s = message.replace("%place_id%", i + "").replace("%player%", "<Error>").replace("%formatted_time%", ParkourUtils.format.format(players.get(lowestValue.get(i))));
                                        }
                                    }
                                }
                                if (!s.equals("")) {
                                    lines.add(s);
                                }
                            }
                            for (final Location rawCenter : parkour.getStatsLocations()) {
                                final Hologram hologram = new Hologram();
                                hologram.setRawCenter(rawCenter);
                                hologram.setLines(lines);
                                hologram.show();
                                this.hologramList.add(hologram);
                                list.addAll(hologram.getLocations());
                            }
                        }
                    }
                    new HologramLocation(list).save();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, (Main.getInstance().getStatsUpdateDelay() * 20L));
    }
    
    public static void handleMoveBack() {
        new BukkitRunnable() {
            public void run() {
                for (final PlayerParkour playerParkour : ParkourUtils.playerparkours) {
                    if (playerParkour.getParkour().isFallBack()) {
                        int n = playerParkour.getPlayer().getLocation().getBlockY();
                        boolean b = false;
                        if (playerParkour.getParkour().getFallBackY() != Integer.MAX_VALUE) {
                            n = playerParkour.getParkour().getFallBackY();
                            b = true;
                        }
                        final int checkpoint = playerParkour.getCheckpoint();
                        if (playerParkour.getParkour().hasCheckpointFallBack(checkpoint)) {
                            n = playerParkour.getParkour().getCheckpointFallBackY(checkpoint);
                        }
                        if (playerParkour.getCurrentLocation() == null || ((playerParkour.getCurrentLocation().getBlockY() >= n || !b) && (playerParkour.getCurrentLocation().getBlockY() <= n || b))) {
                            continue;
                        }
                        final Player player = playerParkour.getPlayer();
                        if (playerParkour.getCheckpoint() == 0) {
                            Main.getInstance().getParkourListener().getTeleportRequest().add(playerParkour.getPlayer());
                            player.teleport(playerParkour.getParkour().getResetLocation());
                        }
                        else {
                            Main.getInstance().getParkourListener().getTeleportRequest().add(playerParkour.getPlayer());
                            player.teleport(playerParkour.getParkour().getCheckpoint(playerParkour.getCheckpoint()));
                        }
                    }
                }
                for (final PlayerParkour playerParkour2 : ParkourUtils.playerparkours) {
                    playerParkour2.run();
                    playerParkour2.saveCurrentLocation();
                }
            }
        }.runTaskTimer(Main.getInstance(), 5L, 5L);
        loadBlockCommands();
    }
    
    private static void loadBlockCommands() {
        new BukkitRunnable() {
            public void run() {
                for (final PlayerParkour playerParkour : ParkourUtils.playerparkours) {
                    for (String value : playerParkour.getParkour().getBlockCommands()) {
                        final String[] split = value.split("#");
                        final String anObject = split[1];
                        final String s = split[2];
                        final String s2 = split[3];
                        final String s3 = split[4];
                        final Block relative = playerParkour.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
                        if (relative.getWorld().getName().equals(anObject) && relative.getX() == Integer.parseInt(s) && relative.getY() == Integer.parseInt(s2) && relative.getZ() == Integer.parseInt(s3)) {
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), split[0].replace("%player%", playerParkour.getPlayer().getName()));
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 5L, 5L);
    }
    
    public static void loadParkours() {
        final File file = new File(Main.getInstance().getDataFolder(), "Parkours");
        if (!file.exists()) {
            file.mkdirs();
        }
        final File[] listFiles = file.listFiles();
        ParkourUtils.parkours.clear();
        for (final File file2 : listFiles) {
            final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(file2);
            final Parkour parkour = new Parkour();
            parkour.setName(file2.getName().replace(".yml", ""));
            parkour.setDelay(getDelay(parkour.getName()));
            parkour.setStartLocation(new Location(Bukkit.getWorld((loadConfiguration).getString("Start.World")), (loadConfiguration).getInt("Start.X"), (loadConfiguration).getInt("Start.Y"), (loadConfiguration).getInt("Start.Z")));
            parkour.setEndLocation(new Location(Bukkit.getWorld((loadConfiguration).getString("End.World")), (loadConfiguration).getInt("End.X"), (loadConfiguration).getInt("End.Y"), (loadConfiguration).getInt("End.Z")));
            parkour.setResetLocation(new Location(Bukkit.getWorld((loadConfiguration).getString("Reset.World")), (loadConfiguration).getInt("Reset.X"), (loadConfiguration).getInt("Reset.Y"), (loadConfiguration).getInt("Reset.Z"), (float)(loadConfiguration).getDouble("Reset.yaw"), (float)(loadConfiguration).getDouble("Reset.pitch")));
            parkour.setFinishRedirect((loadConfiguration).getBoolean("FinishRedirect"));
            parkour.setLeaveRedirect((loadConfiguration).getBoolean("LeaveRedirect"));
            parkour.setCheckpointsRequired((loadConfiguration).getBoolean("CheckpointsRequired"));
            parkour.setFallBack((loadConfiguration).getBoolean("FallBack"));
            parkour.setDeathFallBack((loadConfiguration).getBoolean("DeathFallBack"));
            parkour.setAutomaticRespawnEnabled((loadConfiguration).getBoolean("AutomaticDeathRespawn"));
            if ((loadConfiguration).contains("FallBackY")) {
                parkour.setFallBackY((loadConfiguration).getInt("FallBackY"));
            }
            int n = 1;
            for (String s : (loadConfiguration).getStringList("Checkpoints")) {
                final String[] split = s.split(":");
                parkour.addCheckPoint(n, new Location(Bukkit.getWorld(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3])));
                if ((loadConfiguration).contains("CheckpointFallBackY." + n)) {
                    parkour.addCheckpointFallBack(n, (loadConfiguration).getInt("CheckpointFallBackY." + n));
                }
                ++n;
            }
            parkour.loadHologram();
            parkour.loadBlockCommands();
            parkour.loadStatsLocations();
            addParkour(parkour);
        }
        handleMoveBack();
    }
    
    public static void addParkour(final Parkour parkour) {
        if (!ParkourUtils.parkours.contains(parkour)) {
            ParkourUtils.parkours.add(parkour);
        }
    }
    
    public static void addPlayerParkour(final Player player, final PlayerParkour playerParkour) {
        ParkourUtils.playerparkours.add(playerParkour);
        ParkourUtils.silentPlayerParkours.put(player, playerParkour);
    }
    
    public static void removeParkour(final Parkour parkour) {
        ParkourUtils.parkours.remove(parkour);
    }
    
    public static void removePlayerParkour(final Player player, final PlayerParkour playerParkour) {
        if (playerParkour.getPlayer() != null) {
            if (playerParkour.isFinished()) {
                if (playerParkour.getParkour().isFinishRedirect() && playerParkour.getParkour().getResetLocation() != null) {
                    Main.getInstance().getParkourListener().getTeleportRequest().add(playerParkour.getPlayer());
                    playerParkour.getPlayer().teleport(playerParkour.getParkour().getResetLocation());
                }
            }
            else if (playerParkour.getParkour().isLeaveRedirect() && playerParkour.getParkour().getResetLocation() != null) {
                Main.getInstance().getParkourListener().getTeleportRequest().add(playerParkour.getPlayer());
                playerParkour.getPlayer().teleport(playerParkour.getParkour().getResetLocation());
            }
        }
        ParkourUtils.playerparkours.remove(playerParkour);
        ParkourUtils.silentPlayerParkours.remove(player);
    }
    
    public static void removePlayerParkourSilent(final Player player, final PlayerParkour playerParkour) {
        ParkourUtils.playerparkours.remove(playerParkour);
        ParkourUtils.silentPlayerParkours.remove(player);
    }
    
    public static PlayerParkour getPlayerParkour(final Player player) {
        return ParkourUtils.silentPlayerParkours.get(player);
    }
    
    public static boolean isRunning(final Player player) {
        return ParkourUtils.silentPlayerParkours.containsKey(player);
    }
    
    public static List<PlayerParkour> getPlayerparkours() {
        return ParkourUtils.playerparkours;
    }
    
    public static List<Parkour> getParkours() {
        return ParkourUtils.parkours;
    }
    
    public static long getDelay(final String str) {
        final File file = new File(Main.getInstance().getDataFolder(), "Parkours/" + str + ".yml");
        if (file.exists()) {
            return (YamlConfiguration.loadConfiguration(file)).getLong("Delay");
        }
        return 0L;
    }
    
    public static long getPlayerDelay(final String str, final String str2) {
        final File file = new File(Main.getInstance().getDataFolder(), "player_delay.yml");
        if (file.exists()) {
            return (YamlConfiguration.loadConfiguration(file)).getLong(str + "." + str2);
        }
        return 0L;
    }
    
    public static void setPlayerDelay(final String str, final String str2, final long l) {
        final File file = new File(Main.getInstance().getDataFolder(), "player_delay.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(file);
        (loadConfiguration).set(str + "." + str2, l);
        try {
            (loadConfiguration).save(file);
            (loadConfiguration).load(file);
        }
        catch (Exception ex2) {
            ex2.printStackTrace();
        }
    }
    
    public static void clearPlayerDelay(final String s, final String s2) {
        setPlayerDelay(s, s2, 0L);
    }
    
    static {
        ParkourUtils.parkours = new ArrayList<>();
        ParkourUtils.playerparkours = new ArrayList<>();
        ParkourUtils.silentPlayerParkours = new HashMap<>();
        ParkourUtils.format = new SimpleDateFormat("mm:ss.SSS");
        ParkourUtils.mojangProfileReader = new MojangProfileReader();
    }
}
