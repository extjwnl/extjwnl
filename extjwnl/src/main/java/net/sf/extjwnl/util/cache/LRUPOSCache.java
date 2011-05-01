package net.sf.extjwnl.util.cache;

import net.sf.extjwnl.data.POS;

import java.util.HashMap;
import java.util.Map;

/**
 * A set of Caches split by POS.
 *
 * @author Aliaksandr Autayeu <aliaksandr@autayeu.com>
 */
public class LRUPOSCache<K, V> implements POSCache<K, V> {

    private Map<POS, Cache<K, V>> caches = new HashMap<POS, Cache<K, V>>();
    private int capacity;

    public LRUPOSCache(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public Cache<K, V> getCache(POS pos) {
        Cache<K, V> result = caches.get(pos);
        if (null == result) {
            result = new LRUCache<K, V>(capacity);
            caches.put(pos, result);
        }
        return result;
    }
}