package net.didion.jwnl.dictionary.morph;

import net.didion.jwnl.dictionary.Dictionary;

/**
 * Base class for operations.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
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