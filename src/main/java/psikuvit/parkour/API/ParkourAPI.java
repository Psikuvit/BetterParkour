package psikuvit.parkour.API;

import org.bukkit.entity.Player;
import psikuvit.parkour.Main;
import psikuvit.parkour.PlayerParkour;
import psikuvit.parkour.Utils.ParkourUtils;

public class ParkourAPI
{
    public boolean isFlyDetectionEnabled() {
        return Main.getInstance().isFlyDetectionEnabled();
    }
    
    public boolean isParkourPlayer(final Player obj) {
        boolean b = false;
        for (int i = 0; i < ParkourUtils.getPlayerparkours().size(); ++i) {
            if (ParkourUtils.getPlayerparkours().get(i).getPlayer().equals(obj)) {
                b = true;
            }
        }
        return b;
    }
    
    public void quitParkour(final Player obj) {
        for (int i = 0; i < ParkourUtils.getPlayerparkours().size(); ++i) {
            final PlayerParkour playerParkour = ParkourUtils.getPlayerparkours().get(i);
            if (playerParkour.getPlayer().equals(obj)) {
                ParkourUtils.removePlayerParkour(obj, playerParkour);
            }
        }
    }
}
