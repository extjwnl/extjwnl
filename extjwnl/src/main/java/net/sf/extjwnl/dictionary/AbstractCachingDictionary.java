package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.util.cache.CacheSet;
import net.sf.extjwnl.util.cache.LRUCacheSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Extends <code>Dictionary</code> to provide caching of elements.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class AbstractCachingDictionary extends Dictionary {

    private static final Logger log = LoggerFactory.getLogger(AbstractCachingDictionary.class);

    public static final class IndexWordIterator implements Iterator<IndexWord> {
        private final Iterator<IndexWord> itr;
        private final String searchString;
        private IndexWord startWord;

        public IndexWordIterator(Iterator<IndexWord> itr, String searchString, IndexWord startWord) {
            this.itr = itr;
            this.searchString = searchString;
            this.startWord = startWord;
        }

        public boolean hasNext() {
            return (startWord != null);
        }

        public IndexWord next() {
            if (hasNext()) {
                IndexWord thisWord = startWord;
                startWord = null;
                while (itr.hasNext()) {
                    IndexWord word = itr.next();
                    if (word.getLemma().contains(searchString)) {
                        startWord = word;
                        break;
                    }
                }
                return thisWord;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private volatile CacheSet<DictionaryElementType, Object, DictionaryElement> caches;
    protected boolean isCachingEnabled;

    protected AbstractCachingDictionary(Document doc) throws JWNLException {
        super(doc);
        isCachingEnabled = true;
    }

    public boolean isCachingEnabled() {
        return isCachingEnabled;
    }

    public void setCachingEnabled(boolean cachingEnabled) {
        isCachingEnabled = cachingEnabled;
    }

    public int getCacheSizes(DictionaryElementType type) {
        return getCaches().getCacheSize(type);
    }

    public long getCacheCapacity(DictionaryElementType type) {
        return getCaches().getCacheCapacity(type);
    }

    public void setCacheCapacity(int size) {
        for (DictionaryElementType d : DictionaryElementType.getAllDictionaryElementTypes()) {
            setCacheCapacity(d, size);
        }
    }

    public void setCacheCapacity(DictionaryElementType type, int size) {
        getCaches().setCacheCapacity(type, size);
    }

    public void clearCache() {
        for (DictionaryElementType d : DictionaryElementType.getAllDictionaryElementTypes()) {
            clearCache(d);
        }
    }

    public void clearCache(DictionaryElementType elementType) {
        if (isCachingEnabled()) {
            getCaches().clearCache(elementType);
        }
    }

    protected IndexWord cacheIndexWord(IndexWord word) {
        return (IndexWord) cache(DictionaryElementType.INDEX_WORD, word);
    }

    protected void clearIndexWord(POS pos, Object key) {
        clear(DictionaryElementType.INDEX_WORD, pos, key);
    }

    protected IndexWord getCachedIndexWord(POS pos, Object key) {
        return (IndexWord) getCached(DictionaryElementType.INDEX_WORD, pos, key);
    }

    // public access to allow synset to update cache on offset change without extra hassle
    public void cacheSynset(Synset synset) {
        cache(DictionaryElementType.SYNSET, synset);
    }

    // public access to allow synset to update cache on offset change without extra hassle
    public void clearSynset(POS pos, Object key) {
        clear(DictionaryElementType.SYNSET, pos, key);
    }

    protected Synset getCachedSynset(POS pos, Object key) {
        return (Synset) getCached(DictionaryElementType.SYNSET, pos, key);
    }

    protected Exc cacheException(Exc exception) {
        return (Exc) cache(DictionaryElementType.EXCEPTION, exception);
    }

    protected void clearException(POS pos, Object key) {
        clear(DictionaryElementType.EXCEPTION, pos, key);
    }

    protected Exc getCachedException(POS pos, Object key) {
        return (Exc) getCached(DictionaryElementType.EXCEPTION, pos, key);
    }

    private CacheSet<DictionaryElementType, Object, DictionaryElement> getCaches() {
        if (!isCachingEnabled()) {
            throw new IllegalStateException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_022"));
        }
        // fixed DCL idiom: http://en.wikipedia.org/wiki/Double-checked_locking
        CacheSet<DictionaryElementType, Object, DictionaryElement> result = caches;
        if (null == result) {
            synchronized (this) {
                result = caches;
                if (null == result) {
                    caches = result = new LRUCacheSet<>(DictionaryElementType.getAllDictionaryElementTypes());
                }
            }
        }
        return result;
    }

    private DictionaryElement cache(DictionaryElementType fileType, DictionaryElement obj) {
        if (isCachingEnabled() && null != obj) {
            getCaches().cacheObject(fileType, obj.getPOS(), obj.getKey(), obj);
        }
        return obj;
    }

    private void clear(DictionaryElementType fileType, POS pos, Object key) {
        if (isCachingEnabled()) {
            getCaches().clearObject(fileType, pos, key);
        }
    }

    private DictionaryElement getCached(DictionaryElementType fileType, POS pos, Object key) {
        if (isCachingEnabled()) {
            return getCaches().getCachedObject(fileType, pos, key);
        }
        return null;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Iterator<Exc> getExceptionIterator(POS pos) throws JWNLException {
        return (Iterator<Exc>) (Object) caches.getCache(DictionaryElementType.EXCEPTION).getCache(pos).values().iterator();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Iterator<Synset> getSynsetIterator(POS pos) throws JWNLException {
        return (Iterator<Synset>) (Object) caches.getCache(DictionaryElementType.SYNSET).getCache(pos).values().iterator();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Iterator<IndexWord> getIndexWordIterator(POS pos) throws JWNLException {
        return (Iterator<IndexWord>) (Object) caches.getCache(DictionaryElementType.INDEX_WORD).getCache(pos).values().iterator();
    }

    @Override
    public Iterator<IndexWord> getIndexWordIterator(POS pos, String substring) throws JWNLException {
        substring = prepareQueryString(substring);

        final Iterator<IndexWord> itr = getIndexWordIterator(pos);
        IndexWord start = null;
        while (itr.hasNext()) {
            IndexWord word = itr.next();
            if (word.getLemma().contains(substring)) {
                start = word;
                break;
            }
        }
        return new IndexWordIterator(itr, substring, start);
    }

    @Override
    public synchronized void edit() throws JWNLException {
        if (!isEditable()) {
            cacheAll();
            super.edit();
            // resolving pointers here to use faster iterators on hashes
            resolveAllPointers();
        }
    }

    @Override
    public void addSynset(Synset synset) throws JWNLException {
        super.addSynset(synset);
        cacheSynset(synset);
    }

    @Override
    public void removeSynset(Synset synset) throws JWNLException {
        clearSynset(synset.getPOS(), synset.getKey());
        super.removeSynset(synset);
    }

    @Override
    public void addException(Exc exc) throws JWNLException {
        super.addException(exc);
        cacheException(exc);
    }

    @Override
    public void removeException(Exc exc) throws JWNLException {
        clearException(exc.getPOS(), exc.getKey());
        super.removeException(exc);
    }

    @Override
    public void addIndexWord(IndexWord indexWord) throws JWNLException {
        super.addIndexWord(indexWord);
        cacheIndexWord(indexWord);
    }

    @Override
    public void removeIndexWord(IndexWord indexWord) throws JWNLException {
        clearIndexWord(indexWord.getPOS(), indexWord.getKey());
        super.removeIndexWord(indexWord);
    }

    public synchronized void cacheAll() throws JWNLException {
        setCacheCapacity(Integer.MAX_VALUE);
        for (POS pos : POS.getAllPOS()) {
            cachePOS(pos);
        }
    }

    private void iterateIterator(final Iterator i) {
        int count = 0;
        while (i.hasNext()) {
            if (count % 10000 == 0) {
                if (log.isDebugEnabled()) {
                    log.debug(getMessages().resolveMessage("DICTIONARY_INFO_005", count));
                }
            }
            count++;
            i.next();
        }
        if (log.isDebugEnabled()) {
            log.debug(getMessages().resolveMessage("DICTIONARY_INFO_006", count));
        }
    }

    protected void cachePOS(POS pos) throws JWNLException {
        if (log.isDebugEnabled()) {
            log.debug(getMessages().resolveMessage("DICTIONARY_INFO_003", pos.getLabel()));
        }

        if (log.isDebugEnabled()) {
            log.debug(getMessages().resolveMessage("DICTIONARY_INFO_007"));
        }
        iterateIterator(getExceptionIterator(pos));

        if (log.isDebugEnabled()) {
            log.debug(getMessages().resolveMessage("DICTIONARY_INFO_008"));
        }
        iterateIterator(getSynsetIterator(pos));

        if (log.isDebugEnabled()) {
            log.debug(getMessages().resolveMessage("DICTIONARY_INFO_004"));
        }
        iterateIterator(getIndexWordIterator(pos));
    }
}