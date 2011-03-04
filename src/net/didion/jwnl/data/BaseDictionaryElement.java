package net.didion.jwnl.data;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;

/**
 * Base class for dictionary elements.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public abstract class BaseDictionaryElement implements DictionaryElement {

    private static final long serialVersionUID = 1L;

    protected transient Dictionary dictionary;

    private transient Dictionary settingDictionary;

    protected BaseDictionaryElement(Dictionary dictionary) {
        this.dictionary = dictionary;
        this.settingDictionary = dictionary;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) throws JWNLException {
        if (settingDictionary != dictionary) {
            settingDictionary = dictionary;
            if (null != this.dictionary) {
                this.dictionary.removeElement(this);
            }
            if (null != dictionary) {
                dictionary.addElement(this);
            }
        }
        this.dictionary = dictionary;
    }
}
