package psikuvit.parkour.Utils;

public class CachedClient
{
    private final long cacheMillis;
    private final String cacheName;
    private final String cacheUUID;
    private String texture;
    
    public CachedClient(final String cacheName, final String cacheUUID, final long cacheMillis) {
        this.cacheName = cacheName;
        this.cacheUUID = cacheUUID;
        this.cacheMillis = cacheMillis;
    }
    
    public void setTexture(final String texture) {
        this.texture = texture;
    }
    
    public String getTexture() {
        return this.texture;
    }
    
    public long getCacheMillis() {
        return this.cacheMillis;
    }
    
    public String getCacheName() {
        return this.cacheName;
    }
    
    public String getCacheUUID() {
        return this.cacheUUID;
    }
}
