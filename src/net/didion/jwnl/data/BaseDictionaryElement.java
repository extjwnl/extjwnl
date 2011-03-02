package net.didion.jwnl.data;

import net.didion.jwnl.dictionary.Dictionary;

/**
 * Base class for dictionary elements.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public abstract class BaseDictionaryElement implements DictionaryElement {

    private static final long serialVersionUID = 3230195199146939021L;

    protected transient Dictionary dictionary;

    protected BaseDictionaryElement(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }
}
