package net.sf.extjwnl.util.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * A set of </code>Caches</code>, indexed by <code>CacheKey</code>.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public abstract class CacheSet<K, A, B> {
    public static final int DEFAULT_CACHE_CAPACITY = 1000;

    //K, V = Cache<A, B>
    private Map<K, Cache<A, B>> caches = new HashMap<K, Cache<A, B>>();

    public CacheSet(K[] keys) {
        this(keys, DEFAULT_CACHE_CAPACITY);
    }

    public CacheSet(K[] keys, int size) {
        for (K key : keys) {
            addCache(key, size);
        }
    }

    public CacheSet(K[] keys, int[] sizes) {
        for (int i = 0; i < keys.length; i++) {
            addCache(keys[i], sizes[i]);
        }
    }

    protected abstract Cache<A, B> createCache(int size);

    public void addCache(K key) {
        addCache(key, DEFAULT_CACHE_CAPACITY);
    }

    public void addCache(K key, int size) {
        caches.put(key, createCache(size));
    }

    public void cacheObject(K cacheKey, A key, B value) {
        getCache(cacheKey).put(key, value);
    }

    public void clearObject(K cacheKey, A key) {
        getCache(cacheKey).remove(key);
    }

    public B getCachedObject(K cacheKey, A key) {
        return getCache(cacheKey).get(key);
    }

    public void clearCache(K key) {
        getCache(key).clear();
    }

    public int getCacheSize(K cacheKey) {
        return getCache(cacheKey).getSize();
    }

    public int getCacheCapacity(K cacheKey) {
        return getCache(cacheKey).getCapacity();
    }

    public int setCacheCapacity(K cacheKey, int capacity) {
        return getCache(cacheKey).setCapacity(capacity);
    }

    public int getSize() {
        return caches.size();
    }

    public Cache<A, B> getCache(K cacheKey) {
        return caches.get(cacheKey);
    }
}