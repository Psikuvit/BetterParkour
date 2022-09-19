package psikuvit.parkour.Storage;

import java.util.*;

public class LeaderboardUtils
{
    public static boolean resetStats(final String s) {
        final Map<String, Long> players = getPlayers(s);
        if (players.isEmpty()) {
            return false;
        }
        for (String value : players.keySet()) {
            resetStats(value, s);
        }
        return true;
    }
    
    public static boolean resetStats(final String s, final String anObject) {
        final Map<String, Long> players = getPlayers(anObject);
        if (!players.isEmpty() && players.containsKey(s)) {
            String s2 = null;
            if (new Stats().isEnabled()) {
                s2 = (String)new Stats().getField(s, "parkourstats");
            }
            final ArrayList<String> list = new ArrayList<>();
            final ArrayList<String> list2 = new ArrayList<>();
            boolean b = false;
            if (s2 != null) {
                for (final String s3 : s2.split(",")) {
                    if (!s3.split(":")[0].equals(anObject)) {
                        list2.add(s3);
                    }
                    else {
                        b = true;
                    }
                }
                for (final String s4 : list2) {
                    list.add(s4.split(":")[0] + ":" + Long.parseLong(s4.split(":")[1]));
                }
            }
            StringBuilder string = new StringBuilder();
            for (String value : list) {
                string.append(value).append(",");
            }
            if (b) {
                new Stats().updateField(s, "parkourstats", string.toString());
                return true;
            }
        }
        return false;
    }

    public static Map getPlayers(String paramString) {
        List<String> list = null;
        HashMap<Object, Object> hashMap = new HashMap<>();
        Stats stats = new Stats();
        if (stats.isEnabled())
            list = stats.getLeaderboard(paramString);
        if (list != null)
            for (String str1 : list) {
                String str2 = (String)stats.getField(str1, "parkourstats");
                String[] arrayOfString = str2.split(",");
                for (String str : arrayOfString) {
                    if (str.contains(":")) {
                        String str3 = str.split(":")[0];
                        long l = Long.parseLong(str.split(":")[1]);
                        if (str3.equals(paramString))
                            hashMap.put(str1, l);
                    }
                }
            }
        return hashMap;
    }
    
    public static Map<Integer, String> getLowestValue(final Map<String, Long> map, final int n) {
        final HashMap<Integer, String> hashMap = new HashMap<>();
        final HashMap<String, Long> hashMap2 = new HashMap<>(map);
        for (int i = 1; i <= n; ++i) {
            final String player = getPlayer(hashMap2);
            if (hashMap2.containsKey(player)) {
                hashMap2.remove(player);
                hashMap.put(i, player);
            }
        }
        return hashMap;
    }
    
    public static String getPlayer(final Map<String, Long> map) {
        long n = Long.MAX_VALUE;
        String s = "<?>";
        for (final Map.Entry<String, Long> entry : map.entrySet()) {
            if (entry.getValue() < n) {
                n = entry.getValue();
            }
            else {
                if (entry.getValue() != n) {
                    continue;
                }
            }
            s = entry.getKey();
        }
        return s;
    }
    
    public static String getLowest(final Map<String, Long> map, final int n) {
        String o = null;
        final HashMap<String, Long> hashMap = new HashMap<>(map);
        int i = 1;
        while (i <= map.size()) {
            if (o != null) {
                hashMap.remove(o);
            }
            o = null;
            long longValue = Long.MAX_VALUE;
            for (final String s : hashMap.keySet()) {
                if (hashMap.get(s) != null && hashMap.get(s) < longValue) {
                    o = s;
                    longValue = hashMap.get(s);
                }
            }
            if (o == null) {
                final Random random = new Random();
                if (hashMap.size() > 0) {
                    long longValue2 = random.nextInt(hashMap.size());
                    for (final String s2 : hashMap.keySet()) {
                        if (hashMap.get(s2) != null && hashMap.get(s2) == longValue2) {
                            o = s2;
                            longValue2 = hashMap.get(s2);
                        }
                    }
                }
            }
            if (i == n) {
                if (o == null) {
                    return "<?>";
                }
                return o;
            }
            else {
                ++i;
            }
        }
        return "<?>";
    }
}
