package psikuvit.parkour.PlaceholderAPIHook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import psikuvit.parkour.Main;
import psikuvit.parkour.PlayerParkour;
import psikuvit.parkour.Utils.ParkourUtils;

public class ClipPlaceholder extends PlaceholderExpansion
{
    public void load() {
        this.register();
    }
    
    public boolean persist() {
        return true;
    }
    
    public boolean canRegister() {
        return true;
    }
    
    public String getIdentifier() {
        return "Parkour";
    }
    
    public String getAuthor() {
        return Main.getInstance().getDescription().getAuthors().toString();
    }
    
    public String getVersion() {
        return Main.getInstance().getDescription().getVersion();
    }
    
    public String onPlaceholderRequest(final Player player, final String s) {
        if (player == null) {
            return "";
        }
        if (s.equals("parkour_time")) {
            final PlayerParkour playerParkour = ParkourUtils.getPlayerParkour(player);
            if (playerParkour != null) {
                return playerParkour.getCurrentTime();
            }
            return "";
        }
        else {
            if (!s.equals("parkour_checkpoint")) {
                return null;
            }
            final PlayerParkour playerParkour2 = ParkourUtils.getPlayerParkour(player);
            if (playerParkour2 != null) {
                return String.valueOf(playerParkour2.getCheckpoint());
            }
            return "";
        }
    }
}
