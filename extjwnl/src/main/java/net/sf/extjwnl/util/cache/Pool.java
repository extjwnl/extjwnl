package net.sf.extjwnl.util.cache;

/**
 * Pool interface for caching.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface Pool<T> {

    T replace(T object);

}
