package net.sf.extjwnl.util.cache;

import java.util.List;

/**
 * LeastRecentlyUsed cache set.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class LRUCacheSet<K, A, B> extends CacheSet<K, A, B> {

    public LRUCacheSet(List<K> keys) {
        super(keys);
    }

    public LRUCacheSet(List<K> keys, int size) {
        super(keys, size);
    }

    public LRUCacheSet(List<K> keys, List<Integer> sizes) {
        super(keys, sizes);
    }

    protected POSCache<A, B> createCache(int size) {
        return new LRUPOSCache<>(size);
    }
}