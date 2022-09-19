
package psikuvit.parkour.Utils;

import org.bukkit.entity.Player;
import psikuvit.parkour.Main;

public class PermissionsHandler
{
    public static boolean hasPermission(final Player player, final String s) {
        return !Main.getInstance().isPermissionsEnabled() || player.hasPermission(s);
    }
}
