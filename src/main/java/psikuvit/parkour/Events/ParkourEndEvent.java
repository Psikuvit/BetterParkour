package psikuvit.parkour.Events;

import psikuvit.parkour.PlayerParkour;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Event;

public class ParkourEndEvent extends Event
{
    private static final HandlerList handlers;
    private final Player player;
    private final PlayerParkour playerParkour;
    
    public ParkourEndEvent(final Player player, final PlayerParkour playerParkour) {
        this.player = player;
        this.playerParkour = playerParkour;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public PlayerParkour getPlayerParkour() {
        return this.playerParkour;
    }
    
    public HandlerList getHandlers() {
        return ParkourEndEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return ParkourEndEvent.handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
}
