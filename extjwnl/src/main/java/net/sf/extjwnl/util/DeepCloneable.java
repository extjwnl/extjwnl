package net.sf.extjwnl.util;

/**
 * A <code>DeepCloneable</code> is a cloneable object that can be cloned shallowly (by
 * creating a copy of the object that contains references to the same
 * members as the original) or deeply (by creating a copy of the object
 * and of all it's member objects).
 *
 * @author John Didion <jdidion@users.sourceforge.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public interface DeepCloneable extends Cloneable {

    /**
     * Create a shallow clone of the object.
     *
     * @throws CloneNotSupportedException CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException;

    /**
     * Create a deep clone of the object.
     *
     * @return a deep clone of the object
     * @throws CloneNotSupportedException CloneNotSupportedException
     */
    public Object deepClone() throws CloneNotSupportedException;
}
