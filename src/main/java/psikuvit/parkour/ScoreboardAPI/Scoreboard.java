package psikuvit.parkour.ScoreboardAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.ScoreboardManager;
import psikuvit.parkour.Main;
import psikuvit.parkour.Parkour;
import psikuvit.parkour.Storage.LeaderboardUtils;
import psikuvit.parkour.Utils.MojangProfileReader;
import psikuvit.parkour.Utils.ParkourUtils;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.UUID;

public class Scoreboard {

    private static SimpleDateFormat format;
    private static MojangProfileReader mojangProfileReader;

    public static void sendScoreboard(Player p) {
        if (Main.checkWG()) {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            final org.bukkit.scoreboard.Scoreboard board = manager.getNewScoreboard();
            final Objective objective = board.registerNewObjective("test", "dummy");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.setDisplayName(ChatColor.RED + "Top Times");

            for (final Parkour parkour : ParkourUtils.getParkours()) {
                final String name = parkour.getName();
                final Map<String, Long> players = LeaderboardUtils.getPlayers(name);
                final Map<Integer, String> lowestValue = LeaderboardUtils.getLowestValue(players, 10);
                for (int i = 1; i <= 10; ++i) {
                    String s = "";
                    String message = Main.getInstance().getMessage("StatsList");
                    if (lowestValue.get(i) != null && players.get(lowestValue.get(i)) != null) {
                        final Player player = Bukkit.getPlayer(UUID.fromString(lowestValue.get(i)));
                        if (player != null) {
                            if (player.getName() != null) {
                                s = message.replace("%place_id%", i + "").replace("%player%", player.getName()).replace("%formatted_time%", Scoreboard.format.format(players.get(lowestValue.get(i))));
                            } else {
                                s = message.replace("%place_id%", i + "").replace("%player%", mojangProfileReader.getName(player.getUniqueId().toString())).replace("%formatted_time%", Scoreboard.format.format(players.get(lowestValue.get(i))));
                            }
                        } else {
                            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(lowestValue.get(i)));
                            if (offlinePlayer != null) {
                                if (offlinePlayer.getName() != null) {
                                    s = message.replace("%place_id%", i + "").replace("%player%", offlinePlayer.getName()).replace("%formatted_time%", Scoreboard.format.format(players.get(lowestValue.get(i))));
                                } else {
                                    s = message.replace("%place_id%", i + "").replace("%player%", mojangProfileReader.getName(offlinePlayer.getUniqueId().toString())).replace("%formatted_time%", Scoreboard.format.format(players.get(lowestValue.get(i))));
                                }
                            } else {
                                s = message.replace("%place_id%", i + "").replace("%player%", "<Error>").replace("%formatted_time%", Scoreboard.format.format(players.get(lowestValue.get(i))));
                            }
                        }
                    }
                    if (!s.equals("")) {
                        for (int x = 0; x < 11; x++) {
                            Score score = objective.getScore(s);
                            score.setScore(x);
                        }
                    }
                }
                p.setScoreboard(board);
            }
        }
    }
    public static void removeScoreboard(Player p) {
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

    }

    static {
        Scoreboard.format = new SimpleDateFormat("mm:ss.SSS");
        Scoreboard.mojangProfileReader = new MojangProfileReader();
    }
}
