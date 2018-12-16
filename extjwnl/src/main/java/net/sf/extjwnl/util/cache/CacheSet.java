package net.sf.extjwnl.util.cache;

import net.sf.extjwnl.data.POS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A set of <code>Caches</code>, indexed by <code>CacheKey</code>.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class CacheSet<K, A, B> {
    public static final int DEFAULT_CACHE_CAPACITY = 1000;

    //K, V = Cache<A, B>
    private final Map<K, POSCache<A, B>> caches = new HashMap<>();

    public CacheSet(List<K> keys) {
        this(keys, DEFAULT_CACHE_CAPACITY);
    }

    public CacheSet(List<K> keys, int size) {
        for (K key : keys) {
            addCache(key, size);
        }
    }

    public CacheSet(List<K> keys, List<Integer> sizes) {
        for (int i = 0; i < keys.size(); i++) {
            addCache(keys.get(i), sizes.get(i));
        }
    }

    protected abstract POSCache<A, B> createCache(int size);

    public void addCache(K key) {
        addCache(key, DEFAULT_CACHE_CAPACITY);
    }

    public void addCache(K key, int size) {
        caches.put(key, createCache(size));
    }

    public B cacheObject(K cacheKey, POS pos, A key, B value) {
        getCache(cacheKey).getCache(pos).put(key, value);
        return value;
    }

    public void clearObject(K cacheKey, POS pos, A key) {
        getCache(cacheKey).getCache(pos).remove(key);
    }

    public B getCachedObject(K cacheKey, POS pos, A key) {
        return getCache(cacheKey).getCache(pos).get(key);
    }

    public void clearCache(K key) {
        for (POS pos : POS.getAllPOS()) {
            getCache(key).getCache(pos).clear();
        }
    }

    public int getCacheSize(K cacheKey) {
        int result = 0;
        for (POS pos : POS.getAllPOS()) {
            result = result + getCache(cacheKey).getCache(pos).size();
        }
        return result;
    }

    public long getCacheCapacity(K cacheKey) {
        long result = 0;
        for (POS pos : POS.getAllPOS()) {
            result = result + getCache(cacheKey).getCache(pos).getCapacity();
        }
        return result;
    }

    public void setCacheCapacity(K cacheKey, int capacity) {
        for (POS pos : POS.getAllPOS()) {
            getCache(cacheKey).getCache(pos).setCapacity(capacity);
        }
    }

    public int getSize() {
        return caches.size();
    }

    public POSCache<A, B> getCache(K cacheKey) {
        return caches.get(cacheKey);
    }
}