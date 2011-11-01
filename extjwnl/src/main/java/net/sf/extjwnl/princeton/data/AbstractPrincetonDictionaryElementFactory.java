package net.sf.extjwnl.princeton.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.cache.HashPool;
import net.sf.extjwnl.util.cache.Pool;
import net.sf.extjwnl.util.cache.ZeroPool;
import net.sf.extjwnl.util.factory.Owned;
import net.sf.extjwnl.util.factory.Param;

import java.util.BitSet;
import java.util.Map;

/**
 * Base class for element factories. Holds some common code.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class AbstractPrincetonDictionaryElementFactory implements Owned {

    protected final Dictionary dictionary;

    /**
     * Whether to cache strings in .intern like manner. Reduces memory snapshot for fully-loaded WordNets, default true.
     * Applies on editing, when the WordNet is being fully cached.
     */
    public static final String CACHE_STRINGS_KEY = "cache_strings";
    protected boolean cacheStrings = true;
    protected Pool<String> stringCache = new ZeroPool<String>();

    public AbstractPrincetonDictionaryElementFactory(Dictionary dictionary, Map<String, Param> params) {
        this.dictionary = dictionary;
        if (params.containsKey(CACHE_STRINGS_KEY)) {
            cacheStrings = Boolean.parseBoolean(params.get(CACHE_STRINGS_KEY).getValue());
        }
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
            return new Verb(dictionary, synset, index, stringCache.replace(lemma), new BitSet());
        } else if (POS.ADJECTIVE == synset.getPOS()) {
            AdjectivePosition adjectivePosition = AdjectivePosition.NONE;
            if (lemma.charAt(lemma.length() - 1) == ')' && lemma.indexOf('(') > 0) {
                int left = lemma.indexOf('(');
                String marker = lemma.substring(left + 1, lemma.length() - 1);
                adjectivePosition = AdjectivePosition.getAdjectivePositionForKey(marker);
                lemma = lemma.substring(0, left);
            }
            return new Adjective(dictionary, synset, index, stringCache.replace(lemma), adjectivePosition);
        } else {
            return new Word(dictionary, synset, index, stringCache.replace(lemma));
        }
    }

    @Override
    public void setDictionary(Dictionary dictionary) throws JWNLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dictionary getDictionary() {
        return dictionary;
    }

    public void startCaching() {
        if (cacheStrings) {
            stringCache = new HashPool<String>();
        }
    }

    public void stopCaching() {
        if (cacheStrings) {
            stringCache = new ZeroPool<String>();
        }
    }
}