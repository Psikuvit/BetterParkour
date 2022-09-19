
package psikuvit.parkour;

import org.bukkit.Location;

public class ParkourLocation
{
    private final int position;
    private final Location location;
    
    public ParkourLocation(final int position, final Location location) {
        this.position = position;
        this.location = location;
    }
    
    public int getPosition() {
        return this.position;
    }
    
    public Location getLocation() {
        return this.location;
    }
}
