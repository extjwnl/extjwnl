package net.sf.extjwnl.dictionary.morph;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.MorphologicalProcessor;
import net.sf.extjwnl.dictionary.POSKey;
import net.sf.extjwnl.util.cache.Cache;
import net.sf.extjwnl.util.cache.LRUCache;
import net.sf.extjwnl.util.factory.Param;
import net.sf.extjwnl.util.factory.ParamList;

import java.util.List;
import java.util.Map;

/**
 * Default implementation of <code>MorphologicalProcessor</code>. This isn't a true
 * morphological analyzer (it doesn't figure out all the characteristics of each word
 * it processes). This is basically a stemmer that uses WordNet exception files instead
 * of complex stemming rules. It also tries to be intelligent by removing delimiters and
 * doing concatenation.
 *
 * @author John Didion <jdidion@users.sourceforge.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public class DefaultMorphologicalProcessor implements MorphologicalProcessor {

    private Dictionary dictionary;

    /**
     * Parameter that determines the size of the base form cache
     */
    public static final String CACHE_CAPACITY = "cache_capacity";
    /**
     * Parameter that determines the operations this morphological processor will perform
     */
    public static final String OPERATIONS = "operations";

    private static final int DEFAULT_CACHE_CAPACITY = 1000;

    private Cache lookupCache;

    private Operation[] operations;

    public DefaultMorphologicalProcessor(Dictionary dictionary, Map<String, Param> params) throws JWNLException {
        this.dictionary = dictionary;
        ParamList operationParams = (ParamList) params.get(OPERATIONS);
        if (operationParams == null) {
            throw new JWNLException("DICTIONARY_EXCEPTION_026");
        }
        @SuppressWarnings("unchecked")
        List<Operation> operations = (List<Operation>) operationParams.create();
        this.operations = operations.toArray(new Operation[operations.size()]);

        Param param = params.get(CACHE_CAPACITY);
        int capacity = (param == null) ? DEFAULT_CACHE_CAPACITY : Integer.parseInt(param.getValue());
        lookupCache = new LRUCache(capacity);
    }

    /**
     * Lookup the base form of a word. Given a lemma, finds the WordNet
     * entry most like that lemma. This function returns the first base form
     * found. Subsequent calls to this function with the same part-of-speech
     * and word will return the same base form. To find another base form for
     * the pos/word, call lookupNextBaseForm.
     *
     * @param pos        the part-of-speech of the word to look up
     * @param derivation the word to look up
     * @return IndexWord the IndexWord found during lookup
     */
    public IndexWord lookupBaseForm(POS pos, String derivation) throws JWNLException {
        // See if we've already looked this word up
        LookupInfo info = getCachedLookupInfo(new POSKey(pos, derivation));
        if (info != null && info.getBaseForms().isCurrentFormAvailable()) {
            // get the last base form we retrieved. if you want
            // the next possible base form, use lookupNextBaseForm
            return null == dictionary ? null : dictionary.getIndexWord(pos, info.getBaseForms().getCurrentForm());
        } else {
            return lookupNextBaseForm(pos, derivation, info);
        }
    }

    private void cacheLookupInfo(POSKey key, LookupInfo info) {
        lookupCache.put(key, info);
    }

    private LookupInfo getCachedLookupInfo(POSKey key) {
        return (LookupInfo) lookupCache.get(key);
    }

    /**
     * Lookup the next base form of a pos/word pair. If a base form has not
     * yet been found for the pos/word, it will find the first base form,
     * otherwise it will find the next base form.
     *
     * @param pos        the part-of-speech of the word to look up
     * @param derivation the word to look up
     * @return IndexWord the IndexWord found during lookup, or null if an IndexWord is not found
     */
    private IndexWord lookupNextBaseForm(POS pos, String derivation, LookupInfo info) throws JWNLException {
        if (derivation == null || derivation.equals("")) {
            return null;
        }

        String str = null;
        if (info == null) {
            POSKey key = new POSKey(pos, derivation);
            info = getCachedLookupInfo(key);
            if (info == null) {
                info = new LookupInfo(pos, derivation, operations);
                cacheLookupInfo(key, info);
            }
        }

        // if we've already found another possible base form, return that one
        if (info.getBaseForms().isMoreFormsAvailable()) {
            str = info.getBaseForms().getNextForm();
        } else {
            while (str == null && info.isNextOperationAvailable() && !info.executeNextOperation()) {
            }
            if (info.getBaseForms().isMoreFormsAvailable()) {
                str = info.getBaseForms().getNextForm();
            }
        }

        return (str == null) ? null : (null == dictionary ? null : dictionary.getIndexWord(pos, str));
    }

    public List lookupAllBaseForms(POS pos, String derivation) throws JWNLException {
        LookupInfo info = getCachedLookupInfo(new POSKey(pos, derivation));
        if (info == null) {
            info = new LookupInfo(pos, derivation, operations);
            cacheLookupInfo(new POSKey(pos, derivation), info);
        }
        int index = info.getBaseForms().getIndex();
        while (info.isNextOperationAvailable()) {
            lookupNextBaseForm(pos, derivation, info);
        }
        info.getBaseForms().setIndex(index);
        return info.getBaseForms().getForms();
    }

    private class LookupInfo {
        private POS pos;
        private String derivation;
        private BaseFormSet baseForms;
        private Operation[] operations;
        private int currentOperation;

        public LookupInfo(POS pos, String derivation, Operation[] operations) {
            this.pos = pos;
            this.derivation = derivation;
            this.operations = operations;
            baseForms = new BaseFormSet();
            currentOperation = -1;
        }

        public boolean isNextOperationAvailable() {
            return currentOperation + 1 < operations.length;
        }

        public boolean executeNextOperation() throws JWNLException {
            if (!isNextOperationAvailable()) {
                throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_027");
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