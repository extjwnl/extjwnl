package net.sf.extjwnl.util.cache;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A fixed-capacity <code>Cache</code> that stores the most recently used elements. Once the cache reaches
 * capacity, the least recently used elements will be removed.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class LRUCache<K, V> implements Cache<K, V> {

    private final ConcurrentLinkedHashMap<K, V> m;

    /**
     * @param capacity the maximum number of elements that can be contained in the cache.
     */
    public LRUCache(int capacity) {
        m = new ConcurrentLinkedHashMap.Builder<K, V>().maximumWeightedCapacity(capacity).build();
    }

    public void setCapacity(long capacity) {
        m.setCapacity(capacity);
    }

    public long getCapacity() {
        return m.capacity();
    }

    @Override
    public int size() {
        return m.size();
    }

    @Override
    public boolean isEmpty() {
        return m.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return m.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return m.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return m.get(key);
    }

    @Override
    public V put(K key, V value) {
        return m.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return m.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.m.putAll(m);
    }

    @Override
    public void clear() {
        m.clear();
    }

    @Override
    public Set<K> keySet() {
        return m.keySet();
    }

    @Override
    public Collection<V> values() {
        return m.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return m.entrySet();
    }
}