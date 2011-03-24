package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.util.cache.CacheSet;
import net.sf.extjwnl.util.cache.LRUCacheSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Extends <code>Dictionary</code> to provide caching of elements.
 *
 * @author John Didion <jdidion@didion.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public abstract class AbstractCachingDictionary extends Dictionary {

    private static final Log log = LogFactory.getLog(AbstractCachingDictionary.class);

    private CacheSet<DictionaryElementType, Object, DictionaryElement> caches;
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

    protected void cacheIndexWord(IndexWord word) {
        cache(DictionaryElementType.INDEX_WORD, word);
    }

    protected void clearIndexWord(POS pos, Object key) {
        clear(DictionaryElementType.INDEX_WORD, pos, key);
    }

    protected IndexWord getCachedIndexWord(POS pos, Object key) {
        return (IndexWord) getCached(DictionaryElementType.INDEX_WORD, pos, key);
    }

    //public access to allow synset to update cache on offset change without extra hassle
    public void cacheSynset(Synset synset) {
        cache(DictionaryElementType.SYNSET, synset);
    }

    //public access to allow synset to update cache on offset change without extra hassle
    public void clearSynset(POS pos, Object key) {
        clear(DictionaryElementType.SYNSET, pos, key);
    }

    protected Synset getCachedSynset(POS pos, Object key) {
        return (Synset) getCached(DictionaryElementType.SYNSET, pos, key);
    }

    protected void cacheException(Exc exception) {
        cache(DictionaryElementType.EXCEPTION, exception);
    }

    protected void clearException(POS pos, Object key) {
        clear(DictionaryElementType.EXCEPTION, pos, key);
    }

    protected Exc getCachedException(POS pos, Object key) {
        return (Exc) getCached(DictionaryElementType.EXCEPTION, pos, key);
    }

    private CacheSet<DictionaryElementType, Object, DictionaryElement> getCaches() {
        if (!isCachingEnabled()) {
            throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_022");
        }
        if (caches == null) {
            caches = new LRUCacheSet<DictionaryElementType, Object, DictionaryElement>
                    (DictionaryElementType.getAllDictionaryElementTypes().toArray(
                            new DictionaryElementType[DictionaryElementType.getAllDictionaryElementTypes().size()]));
        }
        return caches;
    }

    private void cache(DictionaryElementType fileType, DictionaryElement obj) {
        if (isCachingEnabled()) {
            getCaches().cacheObject(fileType, obj.getPOS(), obj.getKey(), obj);
        }
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
        Iterator<Exc> result = (Iterator<Exc>) (Object) getPOSIterator(pos, caches.getCache(DictionaryElementType.EXCEPTION).getCache(pos).values().iterator());
        return result;
    }

    @Override
    public Iterator<Synset> getSynsetIterator(POS pos) throws JWNLException {
        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        Iterator<Synset> result = (Iterator<Synset>) (Object) getPOSIterator(pos, caches.getCache(DictionaryElementType.SYNSET).getCache(pos).values().iterator());
        return result;
    }

    @Override
    public Iterator<IndexWord> getIndexWordIterator(POS pos) throws JWNLException {
        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        Iterator<IndexWord> result = (Iterator<IndexWord>) (Object) getPOSIterator(pos, caches.getCache(DictionaryElementType.INDEX_WORD).getCache(pos).values().iterator());
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
        setCacheCapacity(Integer.MAX_VALUE);
        cacheAll();
        super.edit();
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

    public void cacheAll() throws JWNLException {
        for (POS pos : POS.getAllPOS()) {
            cachePOS(pos);
        }
    }

    protected void cachePOS(POS pos) throws JWNLException {
        if (log.isInfoEnabled()) {
            log.info(JWNL.resolveMessage("DICTIONARY_INFO_003", pos.getLabel()));
        }

        {
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("DICTIONARY_INFO_007"));
            }
            int count = 0;
            Iterator<Exc> ei = getExceptionIterator(pos);
            while (ei.hasNext()) {
                if (count % 1000 == 0) {
                    if (log.isInfoEnabled()) {
                        log.info(JWNL.resolveMessage("DICTIONARY_INFO_005", count));
                    }
                }
                count++;
                ei.next();
            }
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("DICTIONARY_INFO_006", count));
            }
        }

        {
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("DICTIONARY_INFO_008"));
            }
            int count = 0;
            maxOffset.put(pos, 0L);
            Iterator<Synset> si = getSynsetIterator(pos);
            while (si.hasNext()) {
                if (count % 1000 == 0) {
                    if (log.isInfoEnabled()) {
                        log.info(JWNL.resolveMessage("DICTIONARY_INFO_005", count));
                    }
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
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("DICTIONARY_INFO_006", count));
            }

        }

        {
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("DICTIONARY_INFO_004"));
            }
            int count = 0;
            Iterator<IndexWord> ii = getIndexWordIterator(pos);
            while (ii.hasNext()) {
                if (count % 1000 == 0) {
                    if (log.isInfoEnabled()) {
                        log.info(JWNL.resolveMessage("DICTIONARY_INFO_005", count));
                    }
                }
                count++;
                IndexWord iw = ii.next();
                iw.getSenses().iterator();//resolve pointers
            }
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("DICTIONARY_INFO_006", count));
            }
        }
    }
}