package net.didion.jwnl.util.factory;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;

/**
 * Supports ownership information.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface Owned {

    /**
     * Returns the dictionary this object belongs to.
     *
     * @return the dictionary this object belongs to
     */
    Dictionary getDictionary();

    /**
     * Sets the dictionary this object belongs to.
     *
     * @param dictionary the dictionary this object belongs to
     */
    void setDictionary(Dictionary dictionary) throws JWNLException;
}
