package net.sf.extjwnl.util.cache;

/**
 * Pool which does nothing. Hopefully nothing harmful too.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class ZeroPool<T> implements Pool<T> {

    public T replace(T object) {
        return object;
    }

}