
package psikuvit.parkour;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import psikuvit.parkour.Storage.LeaderboardUtils;
import psikuvit.parkour.Storage.Stats;
import psikuvit.parkour.Utils.Hologram;
import psikuvit.parkour.Utils.HologramLocation;
import psikuvit.parkour.Utils.ParkourUtils;
import psikuvit.parkour.Utils.PermissionsHandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class ParkourCommand implements CommandExecutor
{
    private static SimpleDateFormat format;
    
    public boolean onCommand(final CommandSender commandSender, final Command command, final String str, final String[] array) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("This Command is not available for the console!");
            return false;
        }
        final Player obj = (Player)commandSender;
        if (command.getName().equalsIgnoreCase("parkour")) {
            if (array.length == 0) {
                if (PermissionsHandler.hasPermission(obj, Main.getInstance().getPermission("Permissions.Reset"))) {
                    obj.sendMessage("§3/" + str + " reset");
                }
                if (PermissionsHandler.hasPermission(obj, Main.getInstance().getPermission("Permissions.Checkpoint"))) {
                    obj.sendMessage("§3/" + str + " checkpoint");
                }
                if (PermissionsHandler.hasPermission(obj, Main.getInstance().getPermission("Permissions.Leave"))) {
                    obj.sendMessage("§3/" + str + " leave");
                }
                if (PermissionsHandler.hasPermission(obj, Main.getInstance().getPermission("Permissions.Resume"))) {
                    obj.sendMessage("§3/" + str + " resume");
                }
                if (PermissionsHandler.hasPermission(obj, Main.getInstance().getPermission("Permissions.Leaderboard"))) {
                    obj.sendMessage("§3/" + str + " leaderboard <parkour>");
                }
                if (obj.hasPermission("parkour.admin")) {
                    obj.sendMessage("§3/" + str + " add <name>");
                    obj.sendMessage("§3/" + str + " remove <name>");
                    obj.sendMessage("§3/" + str + " list");
                    obj.sendMessage("§3/" + str + " setreset <name>");
                    obj.sendMessage("§3/" + str + " setstart <name>");
                    obj.sendMessage("§3/" + str + " setend <name>");
                    obj.sendMessage("§3/" + str + " addcheckpoint <name>");
                    obj.sendMessage("§3/" + str + " addBlockCommand <name#yourcommand>");
                    obj.sendMessage("§3/" + str + " addEndCommand <name#yourcommand>");
                    obj.sendMessage("§3/" + str + " removeEndCommand <name#yourcommand>");
                    obj.sendMessage("§3/" + str + " toggleFallBack <name>");
                    obj.sendMessage("§3/" + str + " togglecheckpoints <name>");
                    obj.sendMessage("§3/" + str + " setFallBack <y> <name>");
                    obj.sendMessage("§3/" + str + " setFallBack <y> <checkpoint> <name>");
                    obj.sendMessage("§3/" + str + " setdelay <ms> <name>");
                    obj.sendMessage("§3/" + str + " resetleaderboard <player> <parkour>");
                    obj.sendMessage("§3/" + str + " resetleaderboard <parkour>");
                    obj.sendMessage("§3/" + str + " addStatsHologram <name> <parkour>");
                    obj.sendMessage("§3/" + str + " removeStatsHologram <name> <parkour>");
                    obj.sendMessage("§3/" + str + " reload");
                }
            }
            if (array.length >= 4 && array[0].equalsIgnoreCase("setFallBack")) {
                if (!obj.hasPermission("parkour.admin")) {
                    obj.sendMessage(Main.getInstance().getMessage("Message.NoPermission"));
                    return false;
                }
                int int1;
                try {
                    int1 = Integer.parseInt(array[1]);
                }
                catch (Exception ex) {
                    obj.sendMessage(array[1] + " is not a valid number!");
                    return false;
                }
                int int2;
                try {
                    int2 = Integer.parseInt(array[2]);
                }
                catch (Exception ex2) {
                    obj.sendMessage(array[2] + " is not a valid number!");
                    return false;
                }
                StringBuilder string = new StringBuilder(array[3]);
                for (int i = 4; i < array.length; ++i) {
                    string.append(" ").append(array[i]);
                }
                final File file = new File(Main.getInstance().getDataFolder(), "Parkours/" + string + ".yml");
                final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(file);
                (loadConfiguration).set("CheckpointFallBackY." + int2, int1);
                this.reloadFile(loadConfiguration, file);
                obj.sendMessage(Main.getInstance().getPrefix() + "§eFallBack Y-Value§7:§f " + int1 + " §efor Checkpoint §f" + int2 + " §esaved");
                return false;
            }
            else {
                if (array.length >= 3) {
                    if (array[0].equalsIgnoreCase("removeStatsHologram")) {
                        if (!obj.hasPermission("parkour.admin")) {
                            obj.sendMessage(Main.getInstance().getMessage("Message.NoPermission"));
                            return false;
                        }
                        final String s = array[1];
                        StringBuilder string2 = new StringBuilder(array[2]);
                        for (int j = 3; j < array.length; ++j) {
                            string2.append(" ").append(array[j]);
                        }
                        final File file2 = new File(Main.getInstance().getDataFolder(), "Parkours/" + string2 + ".yml");
                        if (!file2.exists()) {
                            obj.sendMessage(Main.getInstance().getPrefix() + "§3No Parkour exists with the name §7" + string2 + "§3!");
                            return false;
                        }
                        final YamlConfiguration loadConfiguration2 = YamlConfiguration.loadConfiguration(file2);
                        if ((loadConfiguration2).getString("ParkourStatsHologram." + s + ".Location") == null) {
                            obj.sendMessage(Main.getInstance().getPrefix() + "§3An Stats Hologram with that Name doesn't exists!");
                            return false;
                        }
                        (loadConfiguration2).set("ParkourStatsHologram." + s, null);
                        this.reloadFile(loadConfiguration2, file2);
                        obj.sendMessage(Main.getInstance().getPrefix() + "§aStats Hologram Location has been removed from Parkour §7" + string2 + "§a!");
                    }
                    if (array[0].equalsIgnoreCase("addStatsHologram")) {
                        if (!obj.hasPermission("parkour.admin")) {
                            obj.sendMessage(Main.getInstance().getMessage("Message.NoPermission"));
                            return false;
                        }
                        final String s2 = array[1];
                        StringBuilder string3 = new StringBuilder(array[2]);
                        for (int k = 3; k < array.length; ++k) {
                            string3.append(" ").append(array[k]);
                        }
                        final File file3 = new File(Main.getInstance().getDataFolder(), "Parkours/" + string3 + ".yml");
                        if (!file3.exists()) {
                            obj.sendMessage(Main.getInstance().getPrefix() + "§3No Parkour exists with the name §7" + string3 + "§3!");
                            return false;
                        }
                        final YamlConfiguration loadConfiguration3 = YamlConfiguration.loadConfiguration(file3);
                        if ((loadConfiguration3).getString("ParkourStatsHologram." + s2 + ".Location") != null) {
                            obj.sendMessage(Main.getInstance().getPrefix() + "§3An Stats Hologram with that Name already exists!");
                            return false;
                        }
                        (loadConfiguration3).set("ParkourStatsHologram." + s2 + ".Location", obj.getWorld().getName() + ":" + obj.getLocation().getX() + ":" + obj.getLocation().getY() + ":" + obj.getLocation().getZ());
                        this.reloadFile(loadConfiguration3, file3);
                        obj.sendMessage(Main.getInstance().getPrefix() + "§aStats Hologram Location has been saved for Parkour §7" + string3 + "§a!");
                    }
                }
                if (array.length >= 2 && array[0].equalsIgnoreCase("leaderboard")) {
                    if (!PermissionsHandler.hasPermission(obj, Main.getInstance().getPermission("Permissions.Leaderboard"))) {
                        obj.sendMessage(Main.getInstance().getMessage("Message.NoPermission"));
                        return false;
                    }
                    StringBuilder string4 = new StringBuilder(array[1]);
                    for (int l = 2; l < array.length; ++l) {
                        string4.append(" ").append(array[l]);
                    }
                    if (!new File(Main.getInstance().getDataFolder(), "Parkours/" + string4 + ".yml").exists()) {
                        obj.sendMessage(Main.getInstance().getMessage("Leaderboard.Message.ParkourNotExist").replace("%name%", string4.toString()));
                        return false;
                    }
                    if (new Stats().isEnabled()) {
                        final Map<String, Long> players = LeaderboardUtils.getPlayers(string4.toString());
                        final Map<Integer, String> lowestValue = LeaderboardUtils.getLowestValue(players, 10);
                        for (int n = 1; n <= Main.getInstance().getListDisplay(); ++n) {
                            if (lowestValue.get(n) != null && players.get(lowestValue.get(n)) != null) {
                                final Player player = Bukkit.getPlayer(UUID.fromString(lowestValue.get(n)));
                                if (player != null) {
                                    if (player.getName() != null) {
                                        final String[] split = Main.getInstance().getMessage("Leaderboard.Message.PlaceRegistered").replace("%place_id%", n + "").replace("%player%", player.getName()).replace("%formatted_time%", ParkourCommand.format.format(players.get(lowestValue.get(n)))).split("%next%");
                                        for (int length = split.length, n2 = 0; n2 < length; ++n2) {
                                            obj.sendMessage(split[n2]);
                                        }
                                    }
                                    else {
                                        final String[] split2 = Main.getInstance().getMessage("Leaderboard.Message.PlaceRegistered").replace("%place_id%", n + "").replace("%player%", player.getUniqueId().toString()).replace("%formatted_time%", ParkourCommand.format.format(players.get(lowestValue.get(n)))).split("%next%");
                                        for (int length2 = split2.length, n3 = 0; n3 < length2; ++n3) {
                                            obj.sendMessage(split2[n3]);
                                        }
                                    }
                                }
                                else {
                                    final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(lowestValue.get(n)));
                                    if (offlinePlayer != null) {
                                        if (offlinePlayer.getName() != null) {
                                            final String[] split3 = Main.getInstance().getMessage("Leaderboard.Message.PlaceRegistered").replace("%place_id%", n + "").replace("%player%", offlinePlayer.getName()).replace("%formatted_time%", ParkourCommand.format.format(players.get(lowestValue.get(n)))).split("%next%");
                                            for (int length3 = split3.length, n4 = 0; n4 < length3; ++n4) {
                                                obj.sendMessage(split3[n4]);
                                            }
                                        }
                                        else {
                                            final String[] split4 = Main.getInstance().getMessage("Leaderboard.Message.PlaceRegistered").replace("%place_id%", n + "").replace("%player%", offlinePlayer.getUniqueId().toString()).replace("%formatted_time%", ParkourCommand.format.format(players.get(lowestValue.get(n)))).split("%next%");
                                            for (int length4 = split4.length, n5 = 0; n5 < length4; ++n5) {
                                                obj.sendMessage(split4[n5]);
                                            }
                                        }
                                    }
                                    else {
                                        final String[] split5 = Main.getInstance().getMessage("Leaderboard.Message.PlaceUnregistered").replace("%place_id%", n + "").replace("%player%", "<Error>").replace("%formatted_time%", ParkourCommand.format.format(players.get(lowestValue.get(n)))).split("%next%");
                                        for (int length5 = split5.length, n6 = 0; n6 < length5; ++n6) {
                                            obj.sendMessage(split5[n6]);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return false;
                }
                else {
                    if (array.length == 1) {
                        if (array[0].equalsIgnoreCase("leave")) {
                            if (!PermissionsHandler.hasPermission(obj, Main.getInstance().getPermission("Permissions.Leave"))) {
                                obj.sendMessage(Main.getInstance().getMessage("Message.NoPermission"));
                                return false;
                            }
                            PlayerParkour playerParkour = null;
                            for (int n7 = 0; n7 < ParkourUtils.getPlayerparkours().size(); ++n7) {
                                final PlayerParkour playerParkour2 = ParkourUtils.getPlayerparkours().get(n7);
                                if (playerParkour2.getPlayer().equals(obj)) {
                                    playerParkour = playerParkour2;
                                }
                            }
                            if (playerParkour != null) {
                                ParkourUtils.removePlayerParkour(obj, playerParkour);
                                final String[] split6 = Main.getInstance().getMessage("Message.CommandLeave").split("%next%");
                                for (int length6 = split6.length, n8 = 0; n8 < length6; ++n8) {
                                    obj.sendMessage(split6[n8]);
                                }
                                obj.playSound(obj.getEyeLocation(), "random.click", 0.3F, 0.6F);
                            }
                            else {
                                final String[] split7 = Main.getInstance().getMessage("Message.NoRace").split("%next%");
                                for (int length7 = split7.length, n9 = 0; n9 < length7; ++n9) {
                                    obj.sendMessage(split7[n9]);
                                }
                            }
                        }
                        if (array[0].equalsIgnoreCase("reset")) {
                            if (!PermissionsHandler.hasPermission(obj, Main.getInstance().getPermission("Permissions.Reset"))) {
                                obj.sendMessage(Main.getInstance().getMessage("Message.NoPermission"));
                                return false;
                            }
                            PlayerParkour playerParkour3 = null;
                            for (int n10 = 0; n10 < ParkourUtils.getPlayerparkours().size(); ++n10) {
                                final PlayerParkour playerParkour4 = ParkourUtils.getPlayerparkours().get(n10);
                                if (playerParkour4.getPlayer().equals(obj)) {
                                    playerParkour3 = playerParkour4;
                                }
                            }
                            if (playerParkour3 != null) {
                                Main.getInstance().getParkourListener().getTeleportRequest().add(obj);
                                playerParkour3.setPlayerCheckpoint(0);
                                obj.teleport(playerParkour3.getParkour().getResetLocation());
                                obj.playSound(obj.getEyeLocation(), "random.click", 0.3F, 0.6F);
                            }
                            else {
                                final String[] split8 = Main.getInstance().getMessage("Message.NoRace").split("%next%");
                                for (int length8 = split8.length, n11 = 0; n11 < length8; ++n11) {
                                    obj.sendMessage(split8[n11]);
                                }
                            }
                        }
                        if (array[0].equalsIgnoreCase("resume")) {
                            if (!PermissionsHandler.hasPermission(obj, Main.getInstance().getPermission("Permissions.Resume"))) {
                                obj.sendMessage(Main.getInstance().getMessage("Message.NoPermission"));
                                return false;
                            }
                            if (Main.getInstance().getParkourListener().resumePlayer(obj)) {
                                obj.sendMessage(Main.getInstance().getMessage("Message.Resume"));
                                obj.teleport(ParkourUtils.getPlayerParkour(obj).getCurrentLocation(), PlayerTeleportEvent.TeleportCause.UNKNOWN);
                            }
                            else {
                                obj.sendMessage(Main.getInstance().getMessage("Message.ResumeFailed"));
                            }
                        }
                        if (array[0].equalsIgnoreCase("checkpoint")) {
                            if (!PermissionsHandler.hasPermission(obj, Main.getInstance().getPermission("Permissions.Checkpoint"))) {
                                obj.sendMessage(Main.getInstance().getMessage("Message.NoPermission"));
                                return false;
                            }
                            PlayerParkour playerParkour5 = null;
                            for (int n12 = 0; n12 < ParkourUtils.getPlayerparkours().size(); ++n12) {
                                final PlayerParkour playerParkour6 = ParkourUtils.getPlayerparkours().get(n12);
                                if (playerParkour6.getPlayer().equals(obj)) {
                                    playerParkour5 = playerParkour6;
                                }
                            }
                            if (playerParkour5 != null) {
                                if (playerParkour5.getCheckpoint() == 0) {
                                    Main.getInstance().getParkourListener().getTeleportRequest().add(obj);
                                    obj.teleport(playerParkour5.getParkour().getResetLocation());
                                }
                                else {
                                    Main.getInstance().getParkourListener().getTeleportRequest().add(obj);
                                    obj.teleport(playerParkour5.getParkour().getCheckpoint(playerParkour5.getCheckpoint()));
                                }
                                obj.playSound(obj.getEyeLocation(), "random.click", 0.3F, 0.6F);
                            }
                            else {
                                final String[] split9 = Main.getInstance().getMessage("Message.NoRace").split("%next%");
                                for (int length9 = split9.length, n13 = 0; n13 < length9; ++n13) {
                                    obj.sendMessage(split9[n13]);
                                }
                            }
                        }
                        if (array[0].equalsIgnoreCase("list")) {
                            if (!obj.hasPermission("parkour.admin")) {
                                obj.sendMessage(Main.getInstance().getMessage("Message.NoPermission"));
                                return false;
                            }
                            StringBuilder str2 = new StringBuilder();
                            final File file4 = new File(Main.getInstance().getDataFolder(), "Parkours");
                            if (!file4.exists()) {
                                file4.mkdirs();
                            }
                            final File[] listFiles = file4.listFiles();
                            for (int n14 = 0; n14 < listFiles.length; ++n14) {
                                if (n14 + 1 >= listFiles.length) {
                                    str2.append("§a").append(listFiles[n14].getName().replace(".yml", ""));
                                }
                                else {
                                    str2.append("§a").append(listFiles[n14].getName().replace(".yml", "")).append("§7, ");
                                }
                            }
                            obj.sendMessage(Main.getInstance().getPrefix() + str2);
                        }
                        if (array[0].equalsIgnoreCase("reload")) {
                            if (!obj.hasPermission("parkour.admin")) {
                                obj.sendMessage(Main.getInstance().getMessage("Message.NoPermission"));
                                return false;
                            }
                            final long currentTimeMillis = System.currentTimeMillis();
                            obj.sendMessage("§aReload started!");
                            obj.sendMessage("§aRemoving player parkours §8| §7state: §6in-progress");
                            for (int n15 = 0; n15 < ParkourUtils.getPlayerparkours().size(); ++n15) {
                                ParkourUtils.removePlayerParkour(ParkourUtils.getPlayerparkours().get(n15).getPlayer(), ParkourUtils.getPlayerparkours().get(n15));
                            }
                            obj.sendMessage("§aRemoving player parkours §8| §7state: §adone");
                            obj.sendMessage("§aReloading Messages §8| §7state: §6in-progress");
                            Main.getInstance().setupConfiguration();
                            obj.sendMessage("§aReloading Messages §8| §7state: §adone");
                            obj.sendMessage("§aUpdating all Parkours §8| §7state: §6in-progress");
                            for (int n16 = 0; n16 < ParkourUtils.getParkours().size(); ++n16) {
                                for (Hologram hologram : ParkourUtils.getParkours().get(n16).getHolograms()) {
                                    hologram.destroy();
                                }
                                ParkourUtils.removeParkour(ParkourUtils.getParkours().get(n16));
                                new HologramLocation().reset();
                            }
                            ParkourUtils.loadParkours();
                            obj.sendMessage("§aUpdating all Parkours §8| §7state: §adone");
                            obj.sendMessage("§aReloading MySQL Settings §8| §7state: §6in-progress");
                            new Stats().register();
                            obj.sendMessage("§aReloading MySQL Settings §8| §7state: §adone");
                            obj.sendMessage("§aReload complete. §eTook: " + (System.currentTimeMillis() - currentTimeMillis) + " ms");
                        }
                    }
                    if (array.length >= 3) {
                        if (!obj.hasPermission("parkour.admin")) {
                            obj.sendMessage(Main.getInstance().getMessage("Message.NoPermission"));
                            return false;
                        }
                        if (array[0].equalsIgnoreCase("resetleaderboard")) {
                            final String s3 = array[1];
                            StringBuilder string5 = new StringBuilder(array[2]);
                            for (int n17 = 3; n17 < array.length; ++n17) {
                                string5.append(" ").append(array[n17]);
                            }
                            if (LeaderboardUtils.resetStats(Bukkit.getOfflinePlayer(s3).getUniqueId().toString(), string5.toString())) {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§aStats for Parkour §e" + string5 + " §afrom Player §e" + s3 + " §awere deleted successfully!");
                            }
                            else {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§3Sorry, but there are not Stats available from Player §e" + s3 + " §3in Parkour §e" + string5 + "§3!");
                            }
                        }
                        if (array[0].equalsIgnoreCase("setFallBack")) {
                            int int3;
                            try {
                                int3 = Integer.parseInt(array[1]);
                            }
                            catch (Exception ex3) {
                                obj.sendMessage(array[1] + " is not a valid number!");
                                return false;
                            }
                            StringBuilder string6 = new StringBuilder(array[2]);
                            for (int n18 = 3; n18 < array.length; ++n18) {
                                string6.append(" ").append(array[n18]);
                            }
                            final File file5 = new File(Main.getInstance().getDataFolder(), "Parkours/" + string6 + ".yml");
                            final YamlConfiguration loadConfiguration4 = YamlConfiguration.loadConfiguration(file5);
                            (loadConfiguration4).set("FallBackY", int3);
                            this.reloadFile(loadConfiguration4, file5);
                            obj.sendMessage(Main.getInstance().getPrefix() + "§eFallBack Y-Value§7:§f " + int3 + " §esaved");
                        }
                        if (array[0].equalsIgnoreCase("setdelay")) {
                            long long1;
                            try {
                                long1 = Long.parseLong(array[1]);
                            }
                            catch (Exception ex4) {
                                obj.sendMessage(array[1] + " is not a valid number!");
                                return false;
                            }
                            StringBuilder string7 = new StringBuilder(array[2]);
                            for (int n19 = 3; n19 < array.length; ++n19) {
                                string7.append(" ").append(array[n19]);
                            }
                            final File file6 = new File(Main.getInstance().getDataFolder(), "Parkours/" + string7 + ".yml");
                            final YamlConfiguration loadConfiguration5 = YamlConfiguration.loadConfiguration(file6);
                            (loadConfiguration5).set("Delay", long1);
                            this.reloadFile(loadConfiguration5, file6);
                            obj.sendMessage(Main.getInstance().getPrefix() + "§eDelay Value (Seconds) §7:§f " + long1 + " §esaved");
                        }
                    }
                    if (array.length >= 2) {
                        if (!obj.hasPermission("parkour.admin")) {
                            obj.sendMessage(Main.getInstance().getMessage("Message.NoPermission"));
                            return false;
                        }
                        StringBuilder string8 = new StringBuilder(array[1]);
                        for (int n20 = 2; n20 < array.length; ++n20) {
                            string8.append(" ").append(array[n20]);
                        }
                        if (array[0].equalsIgnoreCase("resetleaderboard")) {
                            if (LeaderboardUtils.resetStats(string8.toString())) {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§aStats for Parkour §e" + string8 + " §awere deleted successfully!");
                            }
                            else {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§3Sorry, but there are not Stats available from Parkour §e" + string8 + "§3!");
                            }
                        }
                        if (array[0].equalsIgnoreCase("addBlockCommand")) {
                            final String s4 = string8.toString().split("#")[0];
                            final String s5 = string8.toString().split("#")[1];
                            final Location location = obj.getTargetBlock((Set<Material>)null, 20).getLocation();
                            final File file7 = new File(Main.getInstance().getDataFolder(), "Parkours/" + s4 + ".yml");
                            if (!file7.exists()) {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§3Parkour §7" + string8 + " §3does not exist!");
                                return false;
                            }
                            final YamlConfiguration loadConfiguration6 = YamlConfiguration.loadConfiguration(file7);
                            final List<String> stringList = (loadConfiguration6).getStringList("BlockCommands");
                            final String string9 = s5 + "#" + location.getWorld().getName() + "#" + location.getBlockX() + "#" + location.getBlockY() + "#" + location.getBlockZ();
                            if (stringList.contains(string9)) {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§3That Command is already registered!");
                                return false;
                            }
                            stringList.add(string9);
                            (loadConfiguration6).set("BlockCommands", stringList);
                            this.reloadFile(loadConfiguration6, file7);
                            obj.sendMessage(Main.getInstance().getPrefix() + "§aAdded Command <§7" + s5 + "§a> for Parkour §7" + s4 + " §aat Location <§7 " + location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + " §a> !");
                        }
                        if (array[0].equalsIgnoreCase("addEndCommand")) {
                            final String s6 = string8.toString().split("#")[0];
                            final String str3 = string8.toString().split("#")[1];
                            final File file8 = new File(Main.getInstance().getDataFolder(), "Parkours/" + s6 + ".yml");
                            if (!file8.exists()) {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§3Parkour §7" + string8 + " §3does not exist!");
                                return false;
                            }
                            final YamlConfiguration loadConfiguration7 = YamlConfiguration.loadConfiguration(file8);
                            final List<String> stringList2 = (loadConfiguration7).getStringList("EndCommands");
                            if (stringList2.contains(str3)) {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§3That Command is already registered!");
                                return false;
                            }
                            stringList2.add(str3);
                            (loadConfiguration7).set("EndCommands", stringList2);
                            this.reloadFile(loadConfiguration7, file8);
                            obj.sendMessage(Main.getInstance().getPrefix() + "§aAdded Command <§7" + str3 + "§a> for Parkour §7" + s6 + "§a!");
                        }
                        if (array[0].equalsIgnoreCase("removeEndCommand")) {
                            final String s7 = string8.toString().split("#")[0];
                            final String str4 = string8.toString().split("#")[1];
                            final File file9 = new File(Main.getInstance().getDataFolder(), "Parkours/" + s7 + ".yml");
                            if (!file9.exists()) {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§3Parkour §7" + string8 + " §3does not exist!");
                                return false;
                            }
                            final YamlConfiguration loadConfiguration8 = YamlConfiguration.loadConfiguration(file9);
                            final List<String> stringList3 = (loadConfiguration8).getStringList("EndCommands");
                            if (!stringList3.contains(str4)) {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§3That Command isn't registered!");
                                return false;
                            }
                            stringList3.remove(str4);
                            (loadConfiguration8).set("EndCommands", stringList3);
                            this.reloadFile(loadConfiguration8, file9);
                            obj.sendMessage(Main.getInstance().getPrefix() + "§aRemoved Command <§7" + str4 + "§a> for Parkour §7" + s7 + "§a!");
                        }
                        if (array[0].equalsIgnoreCase("add")) {
                            final File file10 = new File(Main.getInstance().getDataFolder(), "Parkours/" + string8 + ".yml");
                            if (file10.exists()) {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§3Parkour §7" + string8 + " §3does already exist!");
                                return false;
                            }
                            final YamlConfiguration loadConfiguration9 = YamlConfiguration.loadConfiguration(file10);
                            (loadConfiguration9).options().copyDefaults(true);
                            (loadConfiguration9).set("FinishRedirect", false);
                            (loadConfiguration9).set("LeaveRedirect", false);
                            this.reloadFile(loadConfiguration9, file10);
                            obj.sendMessage(Main.getInstance().getPrefix() + "§aAdded Parkour §7" + string8 + "§a!");
                        }
                        if (array[0].equalsIgnoreCase("remove")) {
                            final File file11 = new File(Main.getInstance().getDataFolder(), "Parkours/" + string8 + ".yml");
                            if (!file11.exists()) {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§3No Parkour exists with the name §7" + string8 + "§3!");
                                return false;
                            }
                            for (final Parkour parkour : ParkourUtils.getParkours()) {
                                if (parkour.getName().equals(string8.toString())) {
                                    for (int n21 = 0; n21 < parkour.getHolograms().size(); ++n21) {
                                        parkour.getHolograms().get(n21).destroy();
                                    }
                                }
                            }
                            for (int n22 = 0; n22 < ParkourUtils.getPlayerparkours().size(); ++n22) {
                                final PlayerParkour playerParkour7 = ParkourUtils.getPlayerparkours().get(n22);
                                if (playerParkour7.getName().equals(string8.toString())) {
                                    ParkourUtils.removePlayerParkour(playerParkour7.getPlayer(), playerParkour7);
                                }
                            }
                            for (int n23 = 0; n23 < ParkourUtils.getParkours().size(); ++n23) {
                                final Parkour parkour2 = ParkourUtils.getParkours().get(n23);
                                if (parkour2.getName().equals(string8.toString())) {
                                    ParkourUtils.removeParkour(parkour2);
                                }
                            }
                            file11.delete();
                            obj.sendMessage(Main.getInstance().getPrefix() + "§aRemoved Parkour §7" + string8 + "§a!");
                        }
                        if (array[0].equalsIgnoreCase("togglecheckpoints")) {
                            final File file12 = new File(Main.getInstance().getDataFolder(), "Parkours/" + string8 + ".yml");
                            final YamlConfiguration loadConfiguration10 = YamlConfiguration.loadConfiguration(file12);
                            final boolean boolean1 = (loadConfiguration10).getBoolean("CheckpointsRequired");
                            (loadConfiguration10).set("CheckpointsRequired", !boolean1);
                            this.reloadFile(loadConfiguration10, file12);
                            obj.sendMessage(Main.getInstance().getPrefix() + "§aRequired Checkpoints toggled §7'" + (boolean1 ? "§3off" : "§aon") + "§7'");
                        }
                        if (array[0].equalsIgnoreCase("toggleFallBack")) {
                            final File file16 = new File(Main.getInstance().getDataFolder(), "Parkours/" + string8 + ".yml");
                            final YamlConfiguration loadConfiguration14 = YamlConfiguration.loadConfiguration(file16);
                            final boolean boolean5 = (loadConfiguration14).getBoolean("FallBack");
                            (loadConfiguration14).set("FallBack", !boolean5);
                            this.reloadFile(loadConfiguration14, file16);
                            obj.sendMessage(Main.getInstance().getPrefix() + "§aFallBack toggled §7'" + (boolean5 ? "§3off" : "§aon") + "§7'");
                        }
                        if (array[0].equalsIgnoreCase("setreset")) {
                            final File file18 = new File(Main.getInstance().getDataFolder(), "Parkours/" + string8 + ".yml");
                            if (!file18.exists()) {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§3Parkour §7" + string8 + " §3does not exist!");
                                return false;
                            }
                            final YamlConfiguration loadConfiguration16 = YamlConfiguration.loadConfiguration(file18);
                            final String name = obj.getLocation().getWorld().getName();
                            final int blockX = obj.getLocation().getBlockX();
                            final int blockY = obj.getLocation().getBlockY();
                            final int blockZ = obj.getLocation().getBlockZ();
                            final double d = obj.getLocation().getYaw();
                            final double d2 = obj.getLocation().getPitch();
                            (loadConfiguration16).set("Reset.World", name);
                            (loadConfiguration16).set("Reset.X", blockX);
                            (loadConfiguration16).set("Reset.Y", blockY);
                            (loadConfiguration16).set("Reset.Z", blockZ);
                            (loadConfiguration16).set("Reset.yaw", d);
                            (loadConfiguration16).set("Reset.pitch", d2);
                            this.reloadFile(loadConfiguration16, file18);
                            obj.sendMessage(Main.getInstance().getPrefix() + "§aSaved the §eReset§a Position for Parkour §7" + string8 + "§a!");
                        }
                        if (array[0].equalsIgnoreCase("setstart")) {
                            final File file19 = new File(Main.getInstance().getDataFolder(), "Parkours/" + string8 + ".yml");
                            if (!file19.exists()) {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§3Parkour §7" + string8 + " §3does not exist!");
                                return false;
                            }
                            final YamlConfiguration loadConfiguration17 = YamlConfiguration.loadConfiguration(file19);
                            final Location location2 = obj.getTargetBlock((Set<Material>)null, 20).getLocation();
                            final String name2 = location2.getWorld().getName();
                            final int blockX2 = location2.getBlockX();
                            final int blockY2 = location2.getBlockY();
                            final int blockZ2 = location2.getBlockZ();
                            (loadConfiguration17).set("Start.World", name2);
                            (loadConfiguration17).set("Start.X", blockX2);
                            (loadConfiguration17).set("Start.Y", blockY2);
                            (loadConfiguration17).set("Start.Z", blockZ2);
                            this.reloadFile(loadConfiguration17, file19);
                            obj.sendMessage(Main.getInstance().getPrefix() + "§aSaved the §eStart§a Position for Parkour §7" + string8 + "§a!");
                        }
                        if (array[0].equalsIgnoreCase("setend")) {
                            final File file20 = new File(Main.getInstance().getDataFolder(), "Parkours/" + string8 + ".yml");
                            if (!file20.exists()) {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§3Parkour §7" + string8 + " §3does not exist!");
                                return false;
                            }
                            final YamlConfiguration loadConfiguration18 = YamlConfiguration.loadConfiguration(file20);
                            final Location location3 = obj.getTargetBlock((Set<Material>)null, 20).getLocation();
                            final String name3 = location3.getWorld().getName();
                            final int blockX3 = location3.getBlockX();
                            final int blockY3 = location3.getBlockY();
                            final int blockZ3 = location3.getBlockZ();
                            (loadConfiguration18).set("End.World", name3);
                            (loadConfiguration18).set("End.X", blockX3);
                            (loadConfiguration18).set("End.Y", blockY3);
                            (loadConfiguration18).set("End.Z", blockZ3);
                            this.reloadFile(loadConfiguration18, file20);
                            obj.sendMessage(Main.getInstance().getPrefix() + "§aSaved the §eEnd§a Position for Parkour §7" + string8 + "§a!");
                        }
                        if (array[0].equalsIgnoreCase("addcheckpoint")) {
                            final File file21 = new File(Main.getInstance().getDataFolder(), "Parkours/" + string8 + ".yml");
                            if (!file21.exists()) {
                                obj.sendMessage(Main.getInstance().getPrefix() + "§3Parkour §7" + string8 + " §3does not exist!");
                                return false;
                            }
                            final YamlConfiguration loadConfiguration19 = YamlConfiguration.loadConfiguration(file21);
                            final Location location4 = obj.getLocation();
                            final String name4 = location4.getWorld().getName();
                            final int blockX4 = location4.getBlockX();
                            final int blockY4 = location4.getBlockY();
                            final int blockZ4 = location4.getBlockZ();
                            final double d3 = obj.getEyeLocation().getYaw();
                            final double d4 = obj.getEyeLocation().getPitch();
                            final List<String> stringList4 = (loadConfiguration19).getStringList("Checkpoints");
                            stringList4.add(name4 + ":" + blockX4 + ":" + blockY4 + ":" + blockZ4 + ":" + d3 + ":" + d4);
                            (loadConfiguration19).set("Checkpoints", stringList4);
                            this.reloadFile(loadConfiguration19, file21);
                            obj.sendMessage(Main.getInstance().getPrefix() + "§aAdded §eCheckpoint §afor Parkour §7" + string8 + "§a!");
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public void reloadFile(final FileConfiguration fileConfiguration, final File file) {
        try {
            fileConfiguration.save(file);
            fileConfiguration.load(file);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    static {
        ParkourCommand.format = new SimpleDateFormat("mm:ss.SSS");
    }
}
