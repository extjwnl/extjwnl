package net.didion.jwnl.util.cache;

import java.util.Collection;

/**
 * A <code>Cache</code> is a collection of values that are indexed by keys and that are stored for an
 * unspecified amount of time (which the implementor of <code>Cache</code> may further specify).
 */
public interface Cache<K, V> {
    /**
     * Store <var>value</var> in the cache, indexed by <var>key</var>.  This operation makes
     * it likely, although not certain, that a subsequent call to <code>get</code> with the
     * same (<code>equal</code>) key will retrieve the same (<code>==</code>) value.
     * <p/>
     * <P>Multiple calls to <code>put</code> with the same <var>key</var> and <var>value</var>
     * are idempotent.  A set of calls to <code>put</code> with the same <var>key</var> but
     * different <var>value</var>s has only the affect of the last call (assuming there were
     * no intervening calls to <code>get</code>).
     * @param key key
     * @param value value
     * @return value
     */
    V put(K key, V value);

    /**
     * If <var>key</var> was used in a previous call to <code>put</code>, this call may
     * return the <var>value</var> of that call.  Otherwise it returns <code>null</code>.
     * @param key key
     * @return value
     */
    V get(K key);

    /**
     * Removes the object associated with <var>key</var> and returns that object.
     * @param key key
     * @return removed object
     */
    V remove(K key);

    /**
     * Returns the maximum number of elements the cache can hold.
     * @return the maximum number of elements the cache can hold
     */
    int getCapacity();

    /**
     * Set the maximum number of elements the cache can hold.
     * @param capacity capacity
     * @return new capacity
     */
    int setCapacity(int capacity);

    /**
     * Returns the current size of the cache.
     * @return size
     */
    public int getSize();

    /**
     * Remove all values stored in this cache.  Subsequent calls to <code>get</code>
     * will return <code>null</code>.
     */
    void clear();

    Collection<V> values();
}