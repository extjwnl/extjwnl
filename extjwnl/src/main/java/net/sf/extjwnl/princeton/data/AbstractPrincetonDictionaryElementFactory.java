package net.sf.extjwnl.princeton.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Owned;

import java.util.BitSet;

/**
 * Base class for element factories. Holds some common code.
 *
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public abstract class AbstractPrincetonDictionaryElementFactory implements Owned {

    protected Dictionary dictionary;

    public AbstractPrincetonDictionaryElementFactory(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * Creates a word.
     *
     * @param synset synset
     * @param index  index
     * @param lemma  lemma
     * @return word
     */
    protected Word createWord(Synset synset, int index, String lemma) {
        if (POS.VERB == synset.getPOS()) {
            return new Verb(dictionary, synset, index, lemma, new BitSet());
        } else if (POS.ADJECTIVE == synset.getPOS()) {
            AdjectivePosition adjectivePosition = AdjectivePosition.NONE;
            if (lemma.charAt(lemma.length() - 1) == ')' && lemma.indexOf('(') > 0) {
                int left = lemma.indexOf('(');
                String marker = lemma.substring(left + 1, lemma.length() - 1);
                adjectivePosition = AdjectivePosition.getAdjectivePositionForKey(marker);
                lemma = lemma.substring(0, left);
            }
            return new Adjective(dictionary, synset, index, lemma, adjectivePosition);
        } else {
            return new Word(dictionary, synset, index, lemma);
        }
    }

    @Override
    public void setDictionary(Dictionary dictionary) throws JWNLException {
        this.dictionary = dictionary;
    }

    @Override
    public Dictionary getDictionary() {
        return dictionary;
    }
}