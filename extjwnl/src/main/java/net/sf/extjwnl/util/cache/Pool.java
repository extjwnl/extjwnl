package net.sf.extjwnl.util.cache;

/**
 * Pool interface for caching.
 *
 * @author Aliaksandr Autayeu <aliaksandr@autayeu.com>
 */
public interface Pool<T> {

    T replace(T object);

}
