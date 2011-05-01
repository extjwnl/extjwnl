package net.sf.extjwnl.util.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A fixed-capacity <code>Cache</code> that stores the most recently used elements. Once the cache reaches
 * capacity, the least recently used elements will be removed.
 *
 * @author John Didion <jdidion@didion.net>
 * @author Aliaksandr Autayeu <aliaksandr@autayeu.com>
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> implements Cache<K, V> {

    private int capacity;

    /**
     * @param capacity the maximum number of elements that can be contained in the cache.
     */
    public LRUCache(int capacity) {
        super(capacity);
        setCapacity(capacity);
    }

    public boolean isFull() {
        return size() >= getCapacity();
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > getCapacity();
    }

    public int setCapacity(int capacity) {
        this.capacity = capacity;
        return this.capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSize() {
        return size();
    }
}