package net.sf.extjwnl.dictionary.morph;

import net.sf.extjwnl.dictionary.Dictionary;

/**
 * Base class for operations.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class AbstractOperation implements Operation {

    protected Dictionary dictionary;

    protected AbstractOperation(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }
}