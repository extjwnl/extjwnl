package net.sf.extjwnl.util.cache;

/**
 * LeastRecentlyUsed cache set.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class LRUCacheSet<K, A, B> extends CacheSet<K, A, B> {

    public LRUCacheSet(K[] keys) {
        super(keys);
    }

    public LRUCacheSet(K[] keys, int size) {
        super(keys, size);
    }

    public LRUCacheSet(K[] keys, int[] sizes) {
        super(keys, sizes);
    }

    protected Cache<A, B> createCache(int size) {
        return new LRUCache<A, B>(size);
    }
}