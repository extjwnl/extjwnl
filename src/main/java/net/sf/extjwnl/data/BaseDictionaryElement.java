package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.IOException;

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

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        dictionary = Dictionary.getRestoreDictionary();
    }
}
