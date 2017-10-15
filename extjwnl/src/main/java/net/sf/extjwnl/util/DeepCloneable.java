package net.sf.extjwnl.util;

/**
 * A <code>DeepCloneable</code> is a cloneable object that can be cloned shallowly (by
 * creating a copy of the object that contains references to the same
 * members as the original) or deeply (by creating a copy of the object
 * and of all it's member objects).
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface DeepCloneable extends Cloneable {

    /**
     * Create a shallow clone of the object.
     *
	 * @return a shallow clone of the object
     * @throws CloneNotSupportedException CloneNotSupportedException
     */
    Object clone() throws CloneNotSupportedException;

    /**
     * Create a deep clone of the object.
     *
     * @return a deep clone of the object
     * @throws CloneNotSupportedException CloneNotSupportedException
     */
    Object deepClone() throws CloneNotSupportedException;
}
