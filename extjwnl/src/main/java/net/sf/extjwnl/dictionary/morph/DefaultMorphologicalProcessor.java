package net.sf.extjwnl.dictionary.morph;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.MorphologicalProcessor;
import net.sf.extjwnl.util.cache.CacheSet;
import net.sf.extjwnl.util.cache.LRUPOSCache;
import net.sf.extjwnl.util.cache.POSCache;
import net.sf.extjwnl.util.factory.Param;
import net.sf.extjwnl.util.factory.ParamList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of <code>MorphologicalProcessor</code>. This isn't a true
 * morphological analyzer (it doesn't figure out all the characteristics of each word
 * it processes). This is basically a stemmer that uses WordNet exception files instead
 * of complex stemming rules. It also tries to be intelligent by removing delimiters and
 * doing concatenation.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class DefaultMorphologicalProcessor implements MorphologicalProcessor {

    /**
     * Parameter that determines the size of the base form cache.
     */
    public static final String CACHE_CAPACITY = "cache_capacity";

    /**
     * Parameter that determines the operations this morphological processor will perform.
     */
    public static final String OPERATIONS = "operations";

    private final POSCache<String, LookupInfo> lookupCache;

    private final Operation[] operations;

    private final Dictionary dictionary;

    public DefaultMorphologicalProcessor(Dictionary dictionary, Map<String, Param> params) throws JWNLException {
        this.dictionary = dictionary;
        ParamList operationParams = (ParamList) params.get(OPERATIONS);
        if (operationParams == null) {
            throw new JWNLException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_026"));
        }
        @SuppressWarnings("unchecked")
        List<Operation> operations = (List<Operation>) operationParams.create();
        this.operations = operations.toArray(new Operation[0]);

        Param param = params.get(CACHE_CAPACITY);
        int capacity = (param == null) ? CacheSet.DEFAULT_CACHE_CAPACITY : Integer.parseInt(param.getValue());
        lookupCache = new LRUPOSCache<>(capacity);
    }

    /**
     * Lookup the first base form of a word. Given a lemma, finds the WordNet
     * entry most like that lemma. This function returns the first base form
     * found. Subsequent calls to this function with the same part-of-speech
     * and word will return the same base form.
     *
     * @param pos        the part-of-speech of the word to look up
     * @param derivation the word to look up
     * @return IndexWord the IndexWord found during lookup or null
     */
    public IndexWord lookupBaseForm(POS pos, String derivation) throws JWNLException {
        if (null == pos || null == derivation || "".equals(derivation)) {
            return null;
        }
        // See if we've already looked this word up
        LookupInfo info = getCachedLookupInfo(pos, derivation);
        synchronized (info) {
            if (info.getBaseForms().isCurrentFormAvailable()) {
                // get the last base form we retrieved. if you want
                // the next possible base form, use lookupNextBaseForm
                return null == dictionary ? null : dictionary.getIndexWord(pos, info.getBaseForms().getCurrentForm());
            } else {
                return lookupNextBaseForm(pos, info);
            }
        }
    }

    /**
     * Returns all base forms of the derivation.
     * @param pos        part of speech
     * @param derivation derivation
     * @return base forms of the derivation
     * @throws JWNLException JWNLException
     */
    public List<String> lookupAllBaseForms(POS pos, String derivation) throws JWNLException {
        if (null == pos || null == derivation || "".equals(derivation)) {
            return Collections.emptyList();
        }

        final LookupInfo info = getCachedLookupInfo(pos, derivation);
        synchronized (info) {
            while (info.isNextOperationAvailable()) {
                info.executeNextOperation();
            }
        }
        return info.getBaseForms().getForms();
    }

    private LookupInfo getCachedLookupInfo(POS pos, String key) {
        //DCL idiom... careful...
        LookupInfo info = lookupCache.getCache(pos).get(key);
        if (info == null) {
            synchronized (this) {
                info = lookupCache.getCache(pos).get(key);
                if (info == null) {
                    info = new LookupInfo(pos, key);
                    lookupCache.getCache(pos).put(key, info);
                }
            }
        }
        return info;
    }

    /**
     * Lookup the next base form of a pos/word pair. If a base form has not
     * yet been found for the pos/word, it will find the first base form,
     * otherwise it will find the next base form.
     *
     *
     * @param pos        the part-of-speech of the word to look up
     * @param info       lookup info
     * @return IndexWord the IndexWord found during lookup, or null if an IndexWord is not found
     * @throws JWNLException JWNLException
     */
    private IndexWord lookupNextBaseForm(POS pos, LookupInfo info) throws JWNLException {
        String str = null;
        synchronized (info) {
            // if we've already found another possible base form, return that one
            if (info.getBaseForms().isMoreFormsAvailable()) {
                str = info.getBaseForms().getNextForm();
            } else {
                while (info.isNextOperationAvailable() && !info.executeNextOperation()) {
                }
                if (info.getBaseForms().isMoreFormsAvailable()) {
                    str = info.getBaseForms().getNextForm();
                }
            }
        }

        return (str == null) ? null : (null == dictionary ? null : dictionary.getIndexWord(pos, str));
    }

    private class LookupInfo {
        private final POS pos;
        private final String derivation;
        private final BaseFormSet baseForms;

        private int currentOperation;

        public LookupInfo(POS pos, String derivation) {
            this.pos = pos;
            this.derivation = derivation;
            baseForms = new BaseFormSet(dictionary);
            currentOperation = -1;
        }

        public boolean isNextOperationAvailable() {
            return currentOperation + 1 < operations.length;
        }

        public boolean executeNextOperation() throws JWNLException {
            if (!isNextOperationAvailable()) {
                throw new JWNLRuntimeException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_027"));
            }
            Operation o = operations[++currentOperation];
            return o.execute(pos, derivation, baseForms);
        }

        public BaseFormSet getBaseForms() {
            return baseForms;
        }
    }

    public Dictionary getDictionary() {
        return dictionary;
    }
}