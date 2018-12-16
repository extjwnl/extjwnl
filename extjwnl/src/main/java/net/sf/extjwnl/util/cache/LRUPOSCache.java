package net.sf.extjwnl.util.cache;

import net.sf.extjwnl.data.POS;

import java.util.EnumMap;
import java.util.Map;

/**
 * A set of Caches split by POS.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class LRUPOSCache<K, V> implements POSCache<K, V> {

    private final Map<POS, Cache<K, V>> caches;

    public LRUPOSCache(int capacity) {
        caches = new EnumMap<>(POS.class);
        for (POS pos : POS.getAllPOS()) {
            caches.put(pos, new LRUCache<>(capacity));
        }
    }

    @Override
    public Cache<K, V> getCache(POS pos) {
        return caches.get(pos);
    }
}