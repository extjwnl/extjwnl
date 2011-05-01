package net.sf.extjwnl.util.cache;

/**
 * Pool which does nothing. Hopefully nothing harmful too.
 *
 * @author Aliaksandr Autayeu <aliaksandr@autayeu.com>
 */
public class ZeroPool<T> implements Pool<T> {

    public T replace(T object) {
        return object;
    }

}