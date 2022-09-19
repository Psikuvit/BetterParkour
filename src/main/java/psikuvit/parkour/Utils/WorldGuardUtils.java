package psikuvit.parkour.Utils;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import psikuvit.parkour.Main;
import psikuvit.parkour.ScoreboardAPI.Scoreboard;

import java.util.logging.Level;

public class WorldGuardUtils extends BukkitRunnable {

    public static boolean checkRegion(Location l) {
        if (Main.guardPL == null)
            return true;
        if (Main.guardPL.equals("worldguard")) {
            WorldGuardPlugin WorldGuard= (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
            for(ProtectedRegion r : WorldGuard.getRegionManager(l.getWorld()).getApplicableRegions(l)) {
                if (r.getId().equalsIgnoreCase("Parkour")) {
                    return true;
                }
            }
            return false;
        }

        Bukkit.getLogger().log(Level.WARNING, "Please install WorldGuard.");
        return true;
    }


    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (checkRegion(p.getLocation())) {
                Scoreboard.sendScoreboard(p);
            } else {
                Scoreboard.removeScoreboard(p);
            }
        }
    }
}
