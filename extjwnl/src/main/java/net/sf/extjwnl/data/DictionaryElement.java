package net.sf.extjwnl.data;

import net.sf.extjwnl.util.factory.Owned;

import java.io.Serializable;

/**
 * Any class that represents an element contained in the dictionary (<code>IndexWord</code>s,
 * <code>Synset</code>s, and <code>Exc</code>eptions) must implement this interface.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface DictionaryElement extends Serializable, Owned {

    /**
     * Returns a key that can be used to index this element.
     *
     * @return a key that can be used to index this element
     */
    Object getKey();


    /**
     * Returns the element's type.
     *
     * @return the element's type
     */
    DictionaryElementType getType();

    /**
     * Returns element's part of speech.
     *
     * @return element's part of speech
     */
    POS getPOS();
}