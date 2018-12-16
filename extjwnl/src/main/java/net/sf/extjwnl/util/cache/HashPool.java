package net.sf.extjwnl.util.cache;

import java.util.HashMap;

/**
 * Pools objects through a HashSet.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class HashPool<T> implements Pool<T> {

    private final HashMap<T, T> cache = new HashMap<>();

    public T replace(T object) {
        T result = cache.get(object);
        if (null == result) {
            result = object;
            cache.put(object, object);
        }
        return result;
    }
}