
package psikuvit.parkour;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import psikuvit.parkour.Events.ParkourEndEvent;
import psikuvit.parkour.Events.ParkourResetEvent;
import psikuvit.parkour.Events.ParkourStartEvent;
import psikuvit.parkour.Storage.Stats;
import psikuvit.parkour.Utils.ParkourUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkourListener implements Listener
{
    private final List<Player> teleportRequest;
    private final List<Player> delayedFly;
    private final List<Player> delayedEnd;
    private final List<Player> delayedCheckpoints;
    private final List<Player> reset;
    private final Map<Player, Long> delay;
    private final SimpleDateFormat delayFormat;
    
    public ParkourListener() {
        this.teleportRequest = new ArrayList<>();
        this.delayedFly = new ArrayList<>();
        this.delayedEnd = new ArrayList<>();
        this.delayedCheckpoints = new ArrayList<>();
        this.reset = new ArrayList<>();
        this.delay = new HashMap<>();
        this.delayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }
    
    @EventHandler
    public void clickBlockCommands(final PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.getAction() == Action.LEFT_CLICK_BLOCK || playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (playerInteractEvent.getClickedBlock() == null) {
                return;
            }
            final Player player = playerInteractEvent.getPlayer();
            if (ParkourUtils.isRunning(player)) {
                final PlayerParkour playerParkour = ParkourUtils.getPlayerParkour(player);
                this.teleportRequest.add(player);
                for (String value : playerParkour.getParkour().getBlockCommands()) {
                    final String[] split = value.split("#");
                    final String anObject = split[1];
                    final String s = split[2];
                    final String s2 = split[3];
                    final String s3 = split[4];
                    final Block clickedBlock = playerInteractEvent.getClickedBlock();
                    if (clickedBlock.getWorld().getName().equals(anObject) && clickedBlock.getX() == Integer.parseInt(s) && clickedBlock.getY() == Integer.parseInt(s2) && clickedBlock.getZ() == Integer.parseInt(s3)) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), split[0].replace("%player%", player.getName()));
                    }
                }
                this.teleportRequest.remove(player);
            }
        }
    }
    
    @EventHandler
    public void autoResume(final PlayerJoinEvent playerJoinEvent) {
        final Player player = playerJoinEvent.getPlayer();
        if (Main.getInstance().isAutoResumeJoinEnabled()) {
            this.resumePlayer(player);
        }
    }
    
    @EventHandler
    public void respawnFallBack(final PlayerRespawnEvent playerRespawnEvent) {
        final Player player = playerRespawnEvent.getPlayer();
        if (ParkourUtils.isRunning(player)) {
            final PlayerParkour playerParkour = ParkourUtils.getPlayerParkour(player);
            if (playerParkour.getParkour().isDeathFallBack()) {
                if (playerParkour.getCurrentLocation() != null) {
                    if (playerParkour.getCheckpoint() == 0) {
                        this.teleportRequest.add(playerParkour.getPlayer());
                        playerRespawnEvent.setRespawnLocation(playerParkour.getParkour().getResetLocation());
                    }
                    else {
                        this.teleportRequest.add(playerParkour.getPlayer());
                        playerRespawnEvent.setRespawnLocation(playerParkour.getParkour().getCheckpoint(playerParkour.getCheckpoint()));
                    }
                }
                playerParkour.saveCurrentLocation();
            }
        }
    }
    
    @EventHandler
    public void deathFallBack(final PlayerDeathEvent playerDeathEvent) {
        final Player entity = playerDeathEvent.getEntity();
        if (ParkourUtils.isRunning(entity)) {
            final PlayerParkour playerParkour = ParkourUtils.getPlayerParkour(entity);
            if (playerParkour.getParkour().isDeathFallBack() && playerParkour.getParkour().isAutomaticRespawnEnabled()) {
                new BukkitRunnable() {
                    public void run() {
                        entity.spigot().respawn();
                    }
                }.runTaskLater(Main.getInstance(), 10L);
            }
        }
    }
    
    @EventHandler
    public void moveIntoVoid(final PlayerMoveEvent playerMoveEvent) {
        if (playerMoveEvent.getTo().getBlockY() < 0) {
            final Player player = playerMoveEvent.getPlayer();
            if (ParkourUtils.isRunning(player)) {
                Bukkit.getServer().dispatchCommand(player, "parkour checkpoint");
            }
        }
    }
    
    @EventHandler
    public void ControlParkour(final PlayerInteractEvent playerInteractEvent) {
        final Player player = playerInteractEvent.getPlayer();
        if (playerInteractEvent.getAction() == Action.PHYSICAL) {
            if (playerInteractEvent.getClickedBlock().getType().name().equals("GOLD_PLATE") || playerInteractEvent.getClickedBlock().getType().name().equals("LIGHT_WEIGHTED_PRESSURE_PLATE")) {
                for (int i = 0; i < ParkourUtils.getParkours().size(); ++i) {
                    final Parkour parkour = ParkourUtils.getParkours().get(i);
                    if (parkour.getStartLocation().equals(playerInteractEvent.getClickedBlock().getLocation())) {
                        playerInteractEvent.setUseInteractedBlock(Event.Result.DENY);
                        if (Main.getInstance().isParkourPermissionsEnabled()) {
                            final String parkourPermission = Main.getInstance().getParkourPermission(parkour.getName());
                            if (parkourPermission != null && !player.hasPermission(parkourPermission)) {
                                final long currentTimeMillis = System.currentTimeMillis();
                                if (!this.delay.containsKey(player)) {
                                    player.sendMessage(Main.getInstance().getMessage("Message.NoParkourPermission").replace("%permission%", parkourPermission));
                                    this.delay.put(player, System.currentTimeMillis() + 3000L);
                                }
                                else if (currentTimeMillis > this.delay.get(player)) {
                                    this.delay.remove(player);
                                    player.sendMessage(Main.getInstance().getMessage("Message.NoParkourPermission").replace("%permission%", parkourPermission));
                                }
                                return;
                            }
                        }
                        if (parkour.getDelay() > 0L) {
                            final long currentTimeMillis2 = System.currentTimeMillis();
                            final long playerDelay = ParkourUtils.getPlayerDelay(parkour.getName(), player.getUniqueId().toString());
                            if (playerDelay > 0L) {
                                if (currentTimeMillis2 <= playerDelay) {
                                    if (!this.delay.containsKey(player)) {
                                        this.delay.put(player, System.currentTimeMillis() + 3000L);
                                    }
                                    else if (currentTimeMillis2 > this.delay.get(player)) {
                                        final String[] split = Main.getInstance().getMessage("Message.Delay").replace("%formatted_time%", this.delayFormat.format(playerDelay)).split("%next%");
                                        for (int length = split.length, j = 0; j < length; ++j) {
                                            player.sendMessage(split[j]);
                                        }
                                        this.delay.remove(player);
                                    }
                                    return;
                                }
                                this.delay.remove(player);
                                ParkourUtils.clearPlayerDelay(parkour.getName(), player.getUniqueId().toString());
                            }
                        }
                        PlayerParkour playerParkour = null;
                        for (int k = 0; k < ParkourUtils.getPlayerparkours().size(); ++k) {
                            final PlayerParkour playerParkour2 = ParkourUtils.getPlayerparkours().get(k);
                            if (playerParkour2.getPlayer().equals(player)) {
                                playerParkour = playerParkour2;
                            }
                        }
                        if (playerParkour == null) {
                            if (player.isFlying() && Main.getInstance().isFlyDetectionEnabled()) {
                                if (!this.delayedFly.contains(player)) {
                                    final String[] split2 = Main.getInstance().getMessage("Message.FlyOnStart").split("%next%");
                                    for (int length2 = split2.length, l = 0; l < length2; ++l) {
                                        player.sendMessage(split2[l]);
                                    }
                                    this.delayedFly.add(player);
                                    new BukkitRunnable() {
                                        public void run() {
                                            ParkourListener.this.delayedFly.remove(player);
                                        }
                                    }.runTaskLater(Main.getInstance(), 100L);
                                }
                            }
                            else {
                                final File file = new File(Main.getInstance().getDataFolder(), "resumes/" + player.getUniqueId().toString() + ".yml");
                                if (file.exists()) {
                                    file.delete();
                                }
                                final PlayerParkour playerParkour3 = new PlayerParkour(player);
                                playerParkour3.setParkour(parkour);
                                playerParkour3.setName(parkour.getName());
                                if (!ParkourUtils.getPlayerparkours().contains(playerParkour3)) {
                                    ParkourUtils.addPlayerParkour(player, playerParkour3);
                                }
                                player.playSound(player.getEyeLocation(), "random.click", 0.3F, 0.6F);
                                Bukkit.getPluginManager().callEvent(new ParkourStartEvent(player, playerParkour3));
                                final String[] split3 = Main.getInstance().getMessage("Message.ParkourStart").split("%next%");
                                for (int length3 = split3.length, n = 0; n < length3; ++n) {
                                    player.sendMessage(split3[n]);
                                }
                                this.reset.add(player);
                                new BukkitRunnable() {
                                    public void run() {
                                        ParkourListener.this.reset.remove(player);
                                    }
                                }.runTaskLater(Main.getInstance(), 100L);
                            }
                        }
                        else if (playerParkour.getName().equals(parkour.getName())) {
                            if (!this.reset.contains(player)) {
                                player.playSound(player.getEyeLocation(), "random.click", 0.3F, 0.6F);
                                Bukkit.getPluginManager().callEvent(new ParkourResetEvent(player, playerParkour));
                                final String[] split4 = Main.getInstance().getMessage("Message.ParkourTimeReset").split("%next%");
                                for (int length4 = split4.length, n2 = 0; n2 < length4; ++n2) {
                                    player.sendMessage(split4[n2]);
                                }
                                playerParkour.setPlayerCheckpoint(0);
                                playerParkour.resetTime();
                                playerParkour.loadCheckpoints();
                                this.reset.add(player);
                                new BukkitRunnable() {
                                    public void run() {
                                        ParkourListener.this.reset.remove(player);
                                    }
                                }.runTaskLater(Main.getInstance(), 100L);
                            }
                        }
                        else {
                            PlayerParkour playerParkour4 = null;
                            for (int n3 = 0; n3 < ParkourUtils.getPlayerparkours().size(); ++n3) {
                                final PlayerParkour playerParkour5 = ParkourUtils.getPlayerparkours().get(n3);
                                if (playerParkour5.getPlayer().equals(player)) {
                                    playerParkour4 = playerParkour5;
                                }
                            }
                            if (playerParkour4 != null) {
                                ParkourUtils.removePlayerParkourSilent(player, playerParkour4);
                            }
                        }
                    }
                }
                for (int n4 = 0; n4 < ParkourUtils.getParkours().size(); ++n4) {
                    final Parkour parkour2 = ParkourUtils.getParkours().get(n4);
                    if (parkour2.getEndLocation().equals(playerInteractEvent.getClickedBlock().getLocation())) {
                        playerInteractEvent.setUseInteractedBlock(Event.Result.DENY);
                        if (parkour2.getDelay() > 0L) {
                            ParkourUtils.setPlayerDelay(parkour2.getName(), player.getUniqueId().toString(), System.currentTimeMillis() + parkour2.getDelay());
                        }
                        PlayerParkour playerParkour6 = null;
                        for (int n5 = 0; n5 < ParkourUtils.getPlayerparkours().size(); ++n5) {
                            final PlayerParkour playerParkour7 = ParkourUtils.getPlayerparkours().get(n5);
                            if (playerParkour7.getPlayer().equals(player)) {
                                playerParkour6 = playerParkour7;
                            }
                        }
                        if (playerParkour6 != null) {
                            if (player.isFlying() && Main.getInstance().isFlyDetectionEnabled()) {
                                if (!this.delayedFly.contains(player)) {
                                    final String[] split5 = Main.getInstance().getMessage("Message.FlyOnEnd").split("%next%");
                                    for (int length5 = split5.length, n6 = 0; n6 < length5; ++n6) {
                                        player.sendMessage(split5[n6]);
                                    }
                                    this.delayedFly.add(player);
                                    new BukkitRunnable() {
                                        public void run() {
                                            ParkourListener.this.delayedFly.remove(player);
                                        }
                                    }.runTaskLater(Main.getInstance(), 100L);
                                }
                            }
                            else {
                                int n7 = 0;
                                if (playerParkour6.getReachedCheckpoints().size() > 0) {
                                    n7 = 1;
                                }
                                if (parkour2.isCheckpointRequired() && playerParkour6.getReachedCheckpoints().size() + n7 <= parkour2.getCheckpoints().size()) {
                                    if (!this.delayedCheckpoints.contains(player)) {
                                        final String[] split6 = Main.getInstance().getMessage("Message.CheckpointsRequired").split("%next%");
                                        for (int length6 = split6.length, n8 = 0; n8 < length6; ++n8) {
                                            player.sendMessage(split6[n8]);
                                        }
                                        this.delayedCheckpoints.add(player);
                                        new BukkitRunnable() {
                                            public void run() {
                                                ParkourListener.this.delayedCheckpoints.remove(player);
                                            }
                                        }.runTaskLater(Main.getInstance(), 50L);
                                    }
                                }
                                else if (playerParkour6.getName().equals(parkour2.getName())) {
                                    playerParkour6.finish();
                                    player.playSound(player.getEyeLocation(), "random.click", 0.3F, 0.6F);
                                    Bukkit.getPluginManager().callEvent(new ParkourEndEvent(player, playerParkour6));
                                    String s = null;
                                    if (new Stats().isEnabled()) {
                                        s = (String)new Stats().getField(player.getUniqueId().toString(), "parkourstats");
                                    }
                                    final ArrayList<String> list = new ArrayList<>();
                                    if (s != null) {
                                        final String[] split7 = s.split(",");
                                        long n9 = 0L;
                                        for (final String s2 : split7) {
                                            if (s2.contains(":")) {
                                                final String str = s2.split(":")[0];
                                                final long longValue = Long.parseLong(s2.split(":")[1]);
                                                if (str.equals(parkour2.getName())) {
                                                    n9 = longValue;
                                                }
                                                else {
                                                    list.add(str + ":" + longValue);
                                                }
                                            }
                                        }
                                        if (n9 == 0L) {
                                            final String[] split8 = Main.getInstance().getMessage("Message.ParkourFinish").replace("%time%", playerParkour6.getTime()).split("%next%");
                                            for (int length8 = split8.length, n11 = 0; n11 < length8; ++n11) {
                                                player.sendMessage(split8[n11]);
                                            }
                                            StringBuilder string = new StringBuilder();
                                            for (String value : list) {
                                                string.append(value).append(",");
                                            }
                                            String s3 = string + parkour2.getName() + ":" + playerParkour6.getRealTime() + ",";
                                            if (s3.endsWith(",")) {
                                                s3 = s3.substring(0, s3.length() - 1);
                                            }
                                            new Stats().updateField(player.getUniqueId().toString(), "parkourstats", s3);
                                        }
                                        else if (playerParkour6.isBetterTime(n9)) {
                                            final String[] split9 = Main.getInstance().getMessage("Message.ParkourFinishNotFailed").replace("%time%", playerParkour6.getTime()).replace("%old_time%", playerParkour6.getTime(n9)).split("%next%");
                                            for (int length9 = split9.length, n12 = 0; n12 < length9; ++n12) {
                                                player.sendMessage(split9[n12]);
                                            }
                                            StringBuilder string2 = new StringBuilder();
                                            for (String value : list) {
                                                string2.append(value).append(",");
                                            }
                                            String s4 = string2 + parkour2.getName() + ":" + playerParkour6.getRealTime() + ",";
                                            if (s4.endsWith(",")) {
                                                s4 = s4.substring(0, s4.length() - 1);
                                            }
                                            new Stats().updateField(player.getUniqueId().toString(), "parkourstats", s4);
                                        }
                                        else {
                                            final String[] split10 = Main.getInstance().getMessage("Message.ParkourFinishFailed").replace("%time%", playerParkour6.getTime()).replace("%old_time%", playerParkour6.getTime(n9)).split("%next%");
                                            for (int length10 = split10.length, n13 = 0; n13 < length10; ++n13) {
                                                player.sendMessage(split10[n13]);
                                            }
                                        }
                                    }
                                    else {
                                        final String[] split11 = Main.getInstance().getMessage("Message.ParkourFinish").replace("%time%", playerParkour6.getTime()).split("%next%");
                                        for (int length11 = split11.length, n14 = 0; n14 < length11; ++n14) {
                                            player.sendMessage(split11[n14]);
                                        }
                                        String s5 = "" + parkour2.getName() + ":" + playerParkour6.getRealTime() + ",";
                                        if (s5.endsWith(",")) {
                                            s5 = s5.substring(0, s5.length() - 1);
                                        }
                                        new Stats().updateField(player.getUniqueId().toString(), "parkourstats", s5);
                                    }
                                    final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder(), "Parkours/" + parkour2.getName() + ".yml"));
                                    if (ParkourUtils.getPlayerparkours().contains(playerParkour6)) {
                                        ParkourUtils.removePlayerParkour(player, playerParkour6);
                                    }
                                    for (String value : (loadConfiguration).getStringList("EndCommands")) {
                                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), value.replace("%time%", playerParkour6.getTime()).replace("%player%", player.getName()));
                                    }
                                    this.delayedEnd.add(player);
                                    new BukkitRunnable() {
                                        public void run() {
                                            ParkourListener.this.delayedEnd.remove(player);
                                        }
                                    }.runTaskLater(Main.getInstance(), 100L);
                                }
                            }
                        }
                        else if (!this.delayedEnd.contains(player)) {
                            final String[] split12 = Main.getInstance().getMessage("Message.NotStarted").split("%next%");
                            for (int length12 = split12.length, n15 = 0; n15 < length12; ++n15) {
                                player.sendMessage(split12[n15]);
                            }
                            this.delayedEnd.add(player);
                            new BukkitRunnable() {
                                public void run() {
                                    ParkourListener.this.delayedEnd.remove(player);
                                }
                            }.runTaskLater(Main.getInstance(), 100L);
                        }
                    }
                }
            }
            if (playerInteractEvent.getClickedBlock().getType().name().equals("IRON_PLATE") || playerInteractEvent.getClickedBlock().getType().name().equals("HEAVY_WEIGHTED_PRESSURE_PLATE")) {
                for (int n16 = 0; n16 < ParkourUtils.getParkours().size(); ++n16) {
                    final Parkour parkour3 = ParkourUtils.getParkours().get(n16);
                    if (parkour3.getCheckpointsLocations().contains(playerInteractEvent.getClickedBlock().getLocation())) {
                        playerInteractEvent.setUseInteractedBlock(Event.Result.DENY);
                        PlayerParkour playerParkour8 = null;
                        for (int n17 = 0; n17 < ParkourUtils.getPlayerparkours().size(); ++n17) {
                            final PlayerParkour playerParkour9 = ParkourUtils.getPlayerparkours().get(n17);
                            if (playerParkour9.getPlayer().equals(player)) {
                                playerParkour8 = playerParkour9;
                            }
                        }
                        if (playerParkour8 != null) {
                            final int checkReached = playerParkour8.checkReached(playerInteractEvent.getClickedBlock().getLocation());
                            if (checkReached != 0) {
                                if (parkour3.isCheckpointRequired() && checkReached != playerParkour8.getCheckpoint() + 1) {
                                    if (!this.delayedCheckpoints.contains(player)) {
                                        final String[] split13 = Main.getInstance().getMessage("Message.CheckpointsRequired").split("%next%");
                                        for (int length13 = split13.length, n18 = 0; n18 < length13; ++n18) {
                                            player.sendMessage(split13[n18]);
                                        }
                                        this.delayedCheckpoints.add(player);
                                        new BukkitRunnable() {
                                            public void run() {
                                                ParkourListener.this.delayedCheckpoints.remove(player);
                                            }
                                        }.runTaskLater(Main.getInstance(), 50L);
                                    }
                                }
                                else {
                                    playerParkour8.handleReached(playerInteractEvent.getClickedBlock().getLocation(), checkReached);
                                    playerParkour8.setPlayerCheckpoint(checkReached);
                                    player.playSound(player.getEyeLocation(), "random.click", 0.3F, 0.6F);
                                    final String[] split14 = Main.getInstance().getMessage("Message.ReachedCheckpoint").replace("%checkpoint%", String.valueOf(checkReached)).split("%next%");
                                    for (int length14 = split14.length, n19 = 0; n19 < length14; ++n19) {
                                        player.sendMessage(split14[n19]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void ServerQuit(final PlayerQuitEvent playerQuitEvent) {
        final Player player = playerQuitEvent.getPlayer();
        for (int i = 0; i < ParkourUtils.getPlayerparkours().size(); ++i) {
            final PlayerParkour playerParkour = ParkourUtils.getPlayerparkours().get(i);
            if (playerParkour.getPlayer().equals(player)) {
                playerParkour.log();
                ParkourUtils.removePlayerParkour(player, playerParkour);
            }
        }
        this.teleportRequest.remove(player);
    }
    
    @EventHandler
    public void ServerKick(final PlayerKickEvent playerKickEvent) {
        final Player player = playerKickEvent.getPlayer();
        for (int i = 0; i < ParkourUtils.getPlayerparkours().size(); ++i) {
            final PlayerParkour playerParkour = ParkourUtils.getPlayerparkours().get(i);
            if (playerParkour.getPlayer().equals(player)) {
                playerParkour.log();
                ParkourUtils.removePlayerParkour(player, playerParkour);
            }
        }
        this.teleportRequest.remove(player);
    }
    
    @EventHandler
    public void ToggleFlight(final PlayerToggleFlightEvent playerToggleFlightEvent) {
        final Player player = playerToggleFlightEvent.getPlayer();
        if (!Main.getInstance().isFlyDetectionEnabled()) {
            return;
        }
        if (playerToggleFlightEvent.isFlying()) {
            for (int i = 0; i < ParkourUtils.getPlayerparkours().size(); ++i) {
                final PlayerParkour playerParkour = ParkourUtils.getPlayerparkours().get(i);
                if (playerParkour.getPlayer().equals(player)) {
                    ParkourUtils.removePlayerParkour(player, playerParkour);
                    final String[] split = Main.getInstance().getMessage("Message.FlyDetected").split("%next%");
                    for (int length = split.length, j = 0; j < length; ++j) {
                        player.sendMessage(split[j]);
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void PlayerTeleport(final PlayerTeleportEvent playerTeleportEvent) {
        final Player player = playerTeleportEvent.getPlayer();
        if (playerTeleportEvent.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN) {
            return;
        }
        if (!Main.getInstance().isTeleportDetectionEnabled()) {
            return;
        }
        if (this.teleportRequest.contains(player)) {
            this.teleportRequest.remove(player);
            return;
        }
        for (int i = 0; i < ParkourUtils.getPlayerparkours().size(); ++i) {
            final PlayerParkour playerParkour = ParkourUtils.getPlayerparkours().get(i);
            if (playerParkour.getPlayer().equals(player)) {
                ParkourUtils.removePlayerParkour(player, playerParkour);
                final String[] split = Main.getInstance().getMessage("Message.TeleportDetected").split("%next%");
                for (int length = split.length, j = 0; j < length; ++j) {
                    player.sendMessage(split[j]);
                }
            }
        }
    }
    
    public List<Player> getTeleportRequest() {
        return this.teleportRequest;
    }
    
    public boolean resumePlayer(final Player player) {
        if (ParkourUtils.getPlayerParkour(player) != null) {
            return false;
        }
        final File file = new File(Main.getInstance().getDataFolder(), "resumes/" + player.getUniqueId().toString() + ".yml");
        if (!file.exists()) {
            return false;
        }
        final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(file);
        if (System.currentTimeMillis() > (loadConfiguration).getLong("RunOut")) {
            file.delete();
            return false;
        }
        file.delete();
        final PlayerParkour playerParkour = new PlayerParkour(player);
        Parkour parkour = null;
        for (int i = 0; i < ParkourUtils.getParkours().size(); ++i) {
            final Parkour parkour2 = ParkourUtils.getParkours().get(i);
            if (parkour2.getName().equals((loadConfiguration).getString("Name"))) {
                parkour = parkour2;
            }
        }
        if (parkour == null) {
            return false;
        }
        final String string = (loadConfiguration).getString("LastLocation.World");
        final double double1 = (loadConfiguration).getDouble("LastLocation.X");
        final double double2 = (loadConfiguration).getDouble("LastLocation.Y");
        final double double3 = (loadConfiguration).getDouble("LastLocation.Z");
        final double double4 = (loadConfiguration).getDouble("LastLocation.Yaw");
        final double double5 = (loadConfiguration).getDouble("LastLocation.Pitch");
        final World world = Bukkit.getWorld(string);
        if (world == null) {
            return false;
        }
        final Location currentLocation = new Location(world, double1, double2, double3);
        currentLocation.setYaw((float)double4);
        currentLocation.setPitch((float)double5);
        playerParkour.setCurrentLocation(currentLocation);
        playerParkour.setParkour(parkour);
        playerParkour.setPlayerCheckpoint((loadConfiguration).getInt("Checkpoint"));
        playerParkour.setStart(System.currentTimeMillis() - (loadConfiguration).getLong("Delay"));
        playerParkour.setName(parkour.getName());
        playerParkour.loadCheckpoints();
        for (int j = 1; j <= playerParkour.getCheckpoint(); ++j) {
            playerParkour.removeCheckpoint(j);
        }
        if (!ParkourUtils.getPlayerparkours().contains(playerParkour)) {
            ParkourUtils.addPlayerParkour(player, playerParkour);
            player.playSound(player.getEyeLocation(), "random.click", 0.3F, 0.6F);
            Bukkit.getPluginManager().callEvent(new ParkourStartEvent(player, playerParkour));
            final String[] split = Main.getInstance().getMessage("Message.ParkourStart").split("%next%");
            for (int length = split.length, k = 0; k < length; ++k) {
                player.sendMessage(split[k]);
            }
        }
        return true;
    }
}
