package net.didion.jwnl.dictionary;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.JWNLRuntimeException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.util.MessageLog;
import net.didion.jwnl.util.MessageLogLevel;
import net.didion.jwnl.util.cache.CacheSet;
import net.didion.jwnl.util.cache.LRUCacheSet;
import org.w3c.dom.Document;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Extends <code>Dictionary</code> to provide caching of elements.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public abstract class AbstractCachingDictionary extends Dictionary {

    private static final MessageLog log = new MessageLog(AbstractCachingDictionary.class);

    private CacheSet<DictionaryElementType, POSKey, DictionaryElement> caches;
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

    public int getCacheCapacity(DictionaryElementType type) {
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

    protected void cacheIndexWord(POSKey key, IndexWord word) {
        cache(DictionaryElementType.INDEX_WORD, key, word);
    }

    protected void clearIndexWord(POSKey key) {
        clear(DictionaryElementType.INDEX_WORD, key);
    }

    protected IndexWord getCachedIndexWord(POSKey key) {
        return (IndexWord) getCached(DictionaryElementType.INDEX_WORD, key);
    }

    //public access to allow synset to update cache on offset change without extra hassle
    public void cacheSynset(POSKey key, Synset synset) {
        cache(DictionaryElementType.SYNSET, key, synset);
    }

    //public access to allow synset to update cache on offset change without extra hassle
    public void clearSynset(POSKey key) {
        clear(DictionaryElementType.SYNSET, key);
    }

    protected Synset getCachedSynset(POSKey key) {
        return (Synset) getCached(DictionaryElementType.SYNSET, key);
    }

    protected void cacheException(POSKey key, Exc exception) {
        cache(DictionaryElementType.EXCEPTION, key, exception);
    }

    protected void clearException(POSKey key) {
        clear(DictionaryElementType.EXCEPTION, key);
    }

    protected Exc getCachedException(POSKey key) {
        return (Exc) getCached(DictionaryElementType.EXCEPTION, key);
    }

    private CacheSet<DictionaryElementType, POSKey, DictionaryElement> getCaches() {
        if (!isCachingEnabled()) {
            throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_022");
        }
        if (caches == null) {
            caches = new LRUCacheSet<DictionaryElementType, POSKey, DictionaryElement>
                    (DictionaryElementType.getAllDictionaryElementTypes().toArray(
                            new DictionaryElementType[DictionaryElementType.getAllDictionaryElementTypes().size()]));
        }
        return caches;
    }

    private void cache(DictionaryElementType fileType, POSKey key, DictionaryElement obj) {
        if (isCachingEnabled()) {
            getCaches().cacheObject(fileType, key, obj);
        }
    }

    private void clear(DictionaryElementType fileType, POSKey key) {
        if (isCachingEnabled()) {
            getCaches().clearObject(fileType, key);
        }
    }

    private DictionaryElement getCached(DictionaryElementType fileType, POSKey key) {
        if (isCachingEnabled()) {
            return getCaches().getCachedObject(fileType, key);
        }
        return null;
    }

    private static final class DictionaryElementIterator implements Iterator<DictionaryElement> {
        private Iterator<DictionaryElement> itr;
        private POS pos;
        private DictionaryElement start;

        public DictionaryElementIterator(Iterator<DictionaryElement> itr, POS pos, DictionaryElement start) {
            this.itr = itr;
            this.pos = pos;
            this.start = start;
        }

        public boolean hasNext() {
            return (start != null);
        }

        public DictionaryElement next() {
            if (hasNext()) {
                DictionaryElement d = start;
                start = null;
                while (itr.hasNext()) {
                    DictionaryElement n = itr.next();
                    if (n.getPOS().equals(pos)) {
                        start = n;
                        break;
                    }
                }
                return d;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static Iterator<DictionaryElement> getPOSIterator(POS pos, Iterator<DictionaryElement> iterator) {
        DictionaryElement start = null;
        while (iterator.hasNext()) {
            DictionaryElement n = iterator.next();
            if (n.getPOS().equals(pos)) {
                start = n;
                break;
            }
        }
        return new DictionaryElementIterator(iterator, pos, start);

    }

    @Override
    public Iterator<Exc> getExceptionIterator(POS pos) throws JWNLException {
        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        Iterator<Exc> result = (Iterator<Exc>) (Object) getPOSIterator(pos, caches.getCache(DictionaryElementType.EXCEPTION).values().iterator());
        return result;
    }

    @Override
    public Iterator<Synset> getSynsetIterator(POS pos) throws JWNLException {
        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        Iterator<Synset> result = (Iterator<Synset>) (Object) getPOSIterator(pos, caches.getCache(DictionaryElementType.SYNSET).values().iterator());
        return result;
    }

    @Override
    public Iterator<IndexWord> getIndexWordIterator(POS pos) throws JWNLException {
        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        Iterator<IndexWord> result = (Iterator<IndexWord>) (Object) getPOSIterator(pos, caches.getCache(DictionaryElementType.INDEX_WORD).values().iterator());
        return result;
    }

    @Override
    public Iterator<IndexWord> getIndexWordIterator(POS pos, String substring) throws JWNLException {
        substring = prepareQueryString(substring);

        final Iterator<IndexWord> itr = getIndexWordIterator(pos);
        IndexWord start = null;
        while (itr.hasNext()) {
            IndexWord word = itr.next();
            if (word.getLemma().indexOf(substring) != -1) {
                start = word;
                break;
            }
        }
        return new MapBackedDictionary.IndexWordIterator(itr, substring, start);
    }

    @Override
    public void edit() throws JWNLException {
        clearCache();
        setCacheCapacity(Integer.MAX_VALUE);
        super.edit();
    }

    @Override
    public void addSynset(Synset synset) throws JWNLException {
        super.addSynset(synset);
        cacheSynset(new POSKey(synset.getPOS(), synset.getOffset()), synset);
    }

    @Override
    public void removeSynset(Synset synset) throws JWNLException {
        clearSynset(new POSKey(synset.getPOS(), synset.getOffset()));
        super.removeSynset(synset);
    }

    @Override
    public void addException(Exc exc) throws JWNLException {
        super.addException(exc);
        cacheException(new POSKey(exc.getPOS(), exc.getLemma()), exc);
    }

    @Override
    public void removeException(Exc exc) throws JWNLException {
        clearException(new POSKey(exc.getPOS(), exc.getLemma()));
        super.removeException(exc);
    }

    @Override
    public void addIndexWord(IndexWord indexWord) throws JWNLException {
        super.addIndexWord(indexWord);
        cacheIndexWord(new POSKey(indexWord.getPOS(), indexWord.getLemma()), indexWord);
    }

    @Override
    public void removeIndexWord(IndexWord indexWord) throws JWNLException {
        clearIndexWord(new POSKey(indexWord.getPOS(), indexWord.getLemma()));
        super.removeIndexWord(indexWord);
    }

    protected void cacheAll() throws JWNLException {
        for (POS pos : POS.getAllPOS()) {
            cachePOS(pos);
        }
    }

    protected void cachePOS(POS pos) throws JWNLException {
        log.log(MessageLogLevel.INFO, "DICTIONARY_INFO_003", pos.getLabel());
        log.log(MessageLogLevel.INFO, "DICTIONARY_INFO_004");
        int count = 0;
        Iterator<IndexWord> ii = getIndexWordIterator(pos);
        while (ii.hasNext()) {
            if (count % 1000 == 0) {
                log.log(MessageLogLevel.INFO, "DICTIONARY_INFO_005", count);
            }
            count++;
            IndexWord iw = ii.next();
            iw.getSenses();//resolve pointers
        }
        log.log(MessageLogLevel.INFO, "DICTIONARY_INFO_006", count);

        log.log(MessageLogLevel.INFO, "DICTIONARY_INFO_007");
        count = 0;
        Iterator<Exc> ei = getExceptionIterator(pos);
        while (ei.hasNext()) {
            if (count % 1000 == 0) {
                log.log(MessageLogLevel.INFO, "DICTIONARY_INFO_005" + count);
            }
            count++;
            ei.next();
        }
        log.log(MessageLogLevel.INFO, "DICTIONARY_INFO_006", count);

        log.log(MessageLogLevel.INFO, "DICTIONARY_INFO_008");
        count = 0;
        maxOffset.put(pos, 0L);
        Iterator<Synset> si = getSynsetIterator(pos);
        while (si.hasNext()) {
            if (count % 1000 == 0) {
                log.log(MessageLogLevel.INFO, "DICTIONARY_INFO_005" + count);
            }
            count++;
            Synset s = si.next();
            if (maxOffset.get(pos) < s.getOffset()) {
                maxOffset.put(pos, s.getOffset());
            }
            for (Pointer p : s.getPointers()) {
                p.getTarget();//resolve pointers
            }
        }
        log.log(MessageLogLevel.INFO, "DICTIONARY_INFO_006", count);
    }
}