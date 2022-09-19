package psikuvit.parkour.Utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MojangProfileReader
{
    public static Map<String, CachedClient> cachedUsernames;
    public static Map<String, CachedClient> cachedUUIDs;
    public static long cacheDelay;
    private final JsonParser parser;
    
    public MojangProfileReader() {
        this.parser = new JsonParser();
    }
    
    public String getName(final String s) {
        final CachedClient cachedClient = MojangProfileReader.cachedUsernames.get(s);
        if (cachedClient != null) {
            if (System.currentTimeMillis() < cachedClient.getCacheMillis() + MojangProfileReader.cacheDelay) {
                return cachedClient.getCacheName();
            }
            MojangProfileReader.cachedUsernames.remove(s);
        }
        this.cacheUser(s, false);
        if (!MojangProfileReader.cachedUsernames.containsKey(s)) {
            return s;
        }
        return MojangProfileReader.cachedUsernames.get(s).getCacheName();
    }
    
    public String getUUID(final String s) {
        final CachedClient cachedClient = MojangProfileReader.cachedUUIDs.get(s);
        if (cachedClient != null) {
            if (System.currentTimeMillis() < cachedClient.getCacheMillis() + MojangProfileReader.cacheDelay) {
                return cachedClient.getCacheUUID();
            }
            MojangProfileReader.cachedUUIDs.remove(s);
        }
        this.cacheUser(s, false);
        if (!MojangProfileReader.cachedUUIDs.containsKey(s)) {
            return null;
        }
        return MojangProfileReader.cachedUUIDs.get(s).getCacheUUID();
    }
    
    public String getTexture(final String s) {
        final CachedClient cachedClient = MojangProfileReader.cachedUsernames.get(s);
        if (cachedClient != null) {
            if (System.currentTimeMillis() < cachedClient.getCacheMillis() + MojangProfileReader.cacheDelay) {
                if (cachedClient.getTexture() != null) {
                    return cachedClient.getTexture();
                }
            }
            else {
                MojangProfileReader.cachedUsernames.remove(s);
            }
        }
        this.cacheUser(s, true);
        if (!MojangProfileReader.cachedUsernames.containsKey(s)) {
            return null;
        }
        return MojangProfileReader.cachedUsernames.get(s).getTexture();
    }
    
    public void cacheUser(final String s, final boolean b) {
        final String url = this.readURL("https://api.minetools.eu/uuid/" + s.replace("-", ""));
        if (url != null) {
            final JsonObject jsonObject = (JsonObject)this.parser.parse(url);
            final JsonElement value = jsonObject.get("name");
            final JsonElement value2 = jsonObject.get("id");
            if (value2.getAsString().equals("null")) {
                return;
            }
            MojangProfileReader.cachedUUIDs.put(value.getAsString(), new CachedClient(value.getAsString(), this.convertUUID(value2.getAsString()), System.currentTimeMillis()));
            MojangProfileReader.cachedUsernames.put(this.convertUUID(value2.getAsString()), new CachedClient(value.getAsString(), this.convertUUID(value2.getAsString()), System.currentTimeMillis()));
            if (b) {
                final String url2 = this.readURL("https://api.minetools.eu/profile/" + s.replace("-", ""));
                final JsonObject jsonObject2 = (JsonObject)this.parser.parse(url2);
                final String asString = jsonObject2.getAsJsonObject("decoded").getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
                MojangProfileReader.cachedUUIDs.get(value.getAsString()).setTexture(asString);
                MojangProfileReader.cachedUsernames.get(this.convertUUID(value2.getAsString())).setTexture(asString);
            }
        }
    }
    
    public String convertUUID(final String s) {
        return s.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
    }
    
    private String readURL(final String str) {
        try {
            final InputStreamReader in = new InputStreamReader(Runtime.getRuntime().exec("curl " + str).getInputStream());
            final BufferedReader bufferedReader = new BufferedReader(in);
            StringBuilder string = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                string.append(line);
            }
            if (string.toString().equals("")) {
                return null;
            }
            in.close();
            bufferedReader.close();
            return string.toString();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    static {
        MojangProfileReader.cachedUsernames = new HashMap<>();
        MojangProfileReader.cachedUUIDs = new HashMap<>();
        MojangProfileReader.cacheDelay = 320000L;
    }
}
