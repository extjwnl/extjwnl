package net.sf.extjwnl.princeton.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.cache.HashPool;
import net.sf.extjwnl.util.cache.Pool;
import net.sf.extjwnl.util.cache.ZeroPool;
import net.sf.extjwnl.util.factory.Param;

import java.util.BitSet;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for element factories. Holds some common code.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class AbstractDictionaryElementFactory implements DictionaryElementFactory {

    protected final Dictionary dictionary;

    // stores max offset for each POS
    protected final Map<POS, Long> maxOffset = new EnumMap<>(POS.class);

    /**
     * Whether to cache strings in .intern like manner. Reduces memory snapshot for fully-loaded WordNets, default true.
     * Applies on editing, when the WordNet is being fully cached.
     */
    public static final String CACHE_STRINGS_KEY = "cache_strings";
    protected boolean cacheStrings = true;
    protected Pool<String> stringCache = new ZeroPool<>();

    public AbstractDictionaryElementFactory(Dictionary dictionary, Map<String, Param> params) {
        this.dictionary = dictionary;
        if (null != params && params.containsKey(CACHE_STRINGS_KEY)) {
            cacheStrings = Boolean.parseBoolean(params.get(CACHE_STRINGS_KEY).getValue());
        }
        for (POS pos : POS.values()) {
            maxOffset.put(pos, 0L);
        }
    }

    public Exc createException(POS pos, String lemma, List<String> exceptions) throws JWNLException {
        return new Exc(dictionary, pos, lemma, exceptions);
    }

    public Synset createSynset(POS pos) throws JWNLException {
        if (POS.VERB == pos) {
            return new VerbSynset(dictionary, createNewOffset(pos));
        } else if (POS.ADJECTIVE == pos) {
            return new AdjectiveSynset(dictionary, createNewOffset(pos));
        } else {
            return new Synset(dictionary, pos, createNewOffset(pos));
        }
    }

    public IndexWord createIndexWord(POS pos, String lemma, Synset synset) throws JWNLException {
        return new IndexWord(dictionary, lemma, pos, synset);
    }

    protected static void initVerbFrameFlags(final Synset synset, final BitSet vFrames,
                                             final int frameNumber, final int wordIndex) {
        if (wordIndex > 0) {
            ((Verb) synset.getWords().get(wordIndex - 1)).getVerbFrameFlags().set(frameNumber);
        } else {
            for (Word w : synset.getWords()) {
                ((Verb) w).getVerbFrameFlags().set(frameNumber);
            }
            vFrames.set(frameNumber);
        }
    }

    /**
     * Creates a word.
     *
     * @param synset synset
     * @param lemma  lemma
     * @return word
     */
    protected Word createWord(Synset synset, String lemma) {
        if (POS.VERB == synset.getPOS()) {
            return new Verb(dictionary, synset, stringCache.replace(lemma), new BitSet());
        } else if (POS.ADJECTIVE == synset.getPOS()) {
            AdjectivePosition adjectivePosition = AdjectivePosition.NONE;
            if (lemma.charAt(lemma.length() - 1) == ')') {
                int left = lemma.indexOf('(');
                if  (left > 0) {
                    String marker = lemma
                        .substring(left + 1, lemma.length() - 1);
                    adjectivePosition = AdjectivePosition
                        .getAdjectivePositionForKey(marker);
                    lemma = lemma.substring(0, left);
                }
            }
            return new Adjective(dictionary, synset, stringCache.replace(lemma), adjectivePosition);
        } else {
            return new Word(dictionary, synset, stringCache.replace(lemma));
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
            stringCache = new HashPool<>();
        }
    }

    public void stopCaching() {
        if (cacheStrings) {
            stringCache = new ZeroPool<>();
        }
    }

    protected synchronized long createNewOffset(POS pos) {
        long result = maxOffset.get(pos) + 1;
        maxOffset.put(pos, result);
        return result;
    }
}