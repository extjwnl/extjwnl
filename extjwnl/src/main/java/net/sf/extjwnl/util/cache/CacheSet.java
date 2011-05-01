package net.sf.extjwnl.util.cache;

import net.sf.extjwnl.data.POS;

import java.util.HashMap;
import java.util.Map;

/**
 * A set of </code>Caches</code>, indexed by <code>CacheKey</code>.
 *
 * @author John Didion <jdidion@didion.net>
 * @author Aliaksandr Autayeu <aliaksandr@autayeu.com>
 */
public abstract class CacheSet<K, A, B> {
    public static final int DEFAULT_CACHE_CAPACITY = 1000;

    //K, V = Cache<A, B>
    private Map<K, POSCache<A, B>> caches = new HashMap<K, POSCache<A, B>>();

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

    protected abstract POSCache<A, B> createCache(int size);

    public void addCache(K key) {
        addCache(key, DEFAULT_CACHE_CAPACITY);
    }

    public void addCache(K key, int size) {
        caches.put(key, createCache(size));
    }

    public void cacheObject(K cacheKey, POS pos, A key, B value) {
        getCache(cacheKey).getCache(pos).put(key, value);
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
            result = result + getCache(cacheKey).getCache(pos).getSize();
        }
        return result;
    }

    public int getCacheCapacity(K cacheKey) {
        int result = 0;
        for (POS pos : POS.getAllPOS()) {
            result = result + getCache(cacheKey).getCache(pos).getCapacity();
        }
        return result;
    }

    public int setCacheCapacity(K cacheKey, int capacity) {
        int result = 0;
        for (POS pos : POS.getAllPOS()) {
            result = result + getCache(cacheKey).getCache(pos).setCapacity(capacity);
        }
        return result;
    }

    public int getSize() {
        return caches.size();
    }

    public POSCache<A, B> getCache(K cacheKey) {
        return caches.get(cacheKey);
    }
}