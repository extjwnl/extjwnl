package net.sf.extjwnl.util.cache;

import java.util.Map;

/**
 * A <code>Cache</code> is a collection of values that are indexed by keys and that are stored for an
 * unspecified amount of time (which the implementor of <code>Cache</code> may further specify).
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface Cache<K, V> extends Map<K, V> {

    /**
     * Returns the maximum number of elements the cache can hold.
     *
     * @return the maximum number of elements the cache can hold
     */
    long getCapacity();

    /**
     * Sets the maximum number of elements the cache can hold.
     *
     * @param capacity capacity
     */
    void setCapacity(long capacity);
}