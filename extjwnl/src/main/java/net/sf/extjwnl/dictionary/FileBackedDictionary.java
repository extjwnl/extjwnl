package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLIOException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.dictionary.file_manager.FileManager;
import net.sf.extjwnl.princeton.data.AbstractDictionaryElementFactory;
import net.sf.extjwnl.util.PointedCharSequence;
import org.w3c.dom.Document;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A <code>Dictionary</code> that retrieves objects from the text files
 * in the WordNet distribution directory.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class FileBackedDictionary extends AbstractCachingDictionary {

    /**
     * File manager install parameter. The value should be the class of FileManager to use.
     */
    public static final String FILE_MANAGER = "file_manager";

    /**
     * The value should be "true" or "false". The default is "true".
     */
    public static final String ENABLE_CACHING = "enable_caching";

    /**
     * The default cache size.
     */
    public static final String CACHE_SIZE = "cache_size";

    /**
     * Size of the index word cache. Overrides the default cache size
     */
    public static final String INDEX_WORD_CACHE_SIZE = "index_word_cache_size";

    /**
     * Size of the synset cache. Overrides the default cache size
     */
    public static final String SYNSET_WORD_CACHE_SIZE = "synset_word_cache_size";

    /**
     * Size of the exception cache. Overrides the default cache size
     */
    public static final String EXCEPTION_WORD_CACHE_SIZE = "exception_word_cache_size";

    private final FileManager fileManager;
    private final FileDictionaryElementFactory factory;

    public FileBackedDictionary(Document doc) throws JWNLException {
        super(doc);

        FileManager manager = (FileManager) (params.get(FILE_MANAGER)).create();

        // caching is enabled by default
        boolean enableCaching =
                !params.containsKey(ENABLE_CACHING) || !params.get(ENABLE_CACHING).getValue().equalsIgnoreCase("false");

        this.setCachingEnabled(enableCaching);
        this.fileManager = manager;
        this.factory = (FileDictionaryElementFactory) elementFactory;

        if (params.containsKey(CACHE_SIZE)) {
            this.setCacheCapacity(Integer.parseInt((params.get(CACHE_SIZE)).getValue()));
        } else {
            if (params.containsKey(INDEX_WORD_CACHE_SIZE)) {
                this.setCacheCapacity(DictionaryElementType.INDEX_WORD,
                        Integer.parseInt(params.get(INDEX_WORD_CACHE_SIZE).getValue()));
            }
            if (params.containsKey(SYNSET_WORD_CACHE_SIZE)) {
                this.setCacheCapacity(DictionaryElementType.SYNSET,
                        Integer.parseInt(params.get(SYNSET_WORD_CACHE_SIZE).getValue()));
            }
            if (params.containsKey(EXCEPTION_WORD_CACHE_SIZE)) {
                this.setCacheCapacity(DictionaryElementType.EXCEPTION,
                        Integer.parseInt(params.get(EXCEPTION_WORD_CACHE_SIZE).getValue()));
            }
        }
    }

    @Override
    public synchronized void close() throws JWNLException {
        fileManager.close();
    }

    @Override
    public synchronized boolean delete() throws JWNLException {
        return fileManager.delete();
    }

    @Override
    public Iterator<IndexWord> getIndexWordIterator(final POS pos) throws JWNLException {
        if (!isEditable()) {
            return new IndexFileLookaheadIterator(pos);
        } else {
            return super.getIndexWordIterator(pos);
        }
    }

    @Override
    public Iterator<IndexWord> getIndexWordIterator(final POS pos, final String substring) throws JWNLException {
        if (!isEditable()) {
            // replace here kind of "leaks out" file format
            return new SubstringIndexFileLookaheadIterator(pos, prepareQueryString(substring.replace(' ', '_')));
        } else {
            return super.getIndexWordIterator(pos, substring);
        }
    }

    @Override
    public IndexWord getIndexWord(POS pos, String lemma) throws JWNLException {
        lemma = prepareQueryString(lemma);

        IndexWord word = null;
        if (lemma.length() > 0) {
            if (isCachingEnabled()) {
                word = getCachedIndexWord(pos, lemma);
            }
            if (!isEditable() && null == word) {
                try {
                    // replace here kind of "leaks out" file format
                    CharSequence line = fileManager.getIndexedLine(pos, DictionaryFileType.INDEX, lemma.replace(' ', '_'));
                    if (null != line) {
                        word = parseAndCacheIndexWord(pos, line);
                    }
                } catch (JWNLIOException e) {
                    throw new JWNLException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_004", new Object[]{pos.getLabel(), lemma}), e);
                }
            }
        }
        return word;
    }

    @Override
    public IndexWord getRandomIndexWord(POS pos) throws JWNLException {
        if (!isEditable()) {
            CharSequence line = fileManager.getRandomLine(pos, DictionaryFileType.INDEX);
            return parseAndCacheIndexWord(pos, line);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Iterator<Synset> getSynsetIterator(POS pos) throws JWNLException {
        if (!isEditable()) {
            return new FileLookaheadIterator<Synset>(pos, DictionaryFileType.DATA) {
                @Override
                protected Synset parseLine(POS pos, PointedCharSequence line) {
                    try {
                        return parseAndCacheSynset(pos, line);
                    } catch (JWNLException e) {
                        throw new JWNLRuntimeException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_005",
                                new Object[]{pos.getLabel(), "?"}), e);
                    }
                }
            };
        } else {
            return super.getSynsetIterator(pos);
        }
    }

    @Override
    public Synset getSynsetAt(POS pos, long offset) throws JWNLException {
        Synset synset = getCachedSynset(pos, offset);
        if (!isEditable() && null == synset) {
            PointedCharSequence line = fileManager.readLineAt(pos, DictionaryFileType.DATA, offset);
            synset = parseAndCacheSynset(pos, line);
        }
        return synset;
    }

    @Override
    public Iterator<Exc> getExceptionIterator(POS pos) throws JWNLException {
        if (!isEditable()) {
            return new FileLookaheadIterator<Exc>(pos, DictionaryFileType.EXCEPTION) {
                @Override
                protected Exc parseLine(POS pos, PointedCharSequence line) throws JWNLException {
                    return parseAndCacheExceptionLine(pos, line);
                }
            };
        } else {
            return super.getExceptionIterator(pos);
        }
    }

    public Exc getException(POS pos, String derivation) throws JWNLException {
        Exc exc = null;
        if (null != derivation) {
            derivation = prepareQueryString(derivation);
            if (derivation.length() > 0) {
                if (isCachingEnabled()) {
                    exc = getCachedException(pos, derivation);
                }
                if (!isEditable() && null == exc) {
                    try {
                        CharSequence line = fileManager.getIndexedLine(
                                // replace here kind of "leaks out" file format
                                pos, DictionaryFileType.EXCEPTION, derivation.replace(' ', '_'));
                        if (null != line) {
                            exc = parseAndCacheExceptionLine(pos, line);
                        }
                    } catch (JWNLIOException e) {
                        throw new JWNLException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_006",
                                new Object[]{pos.getLabel(), derivation}), e);
                    }
                }
            }
        }
        return exc;
    }

    @Override
    public synchronized void edit() throws JWNLException {
        if (!isEditable()) {
            if (!isCachingEnabled()) {
                throw new JWNLException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_030"));
            }
            super.edit();
            fileManager.edit();
        }
    }

    @Override
    public synchronized void save() throws JWNLException {
        super.save();
        fileManager.save();
    }

    @Override
    public synchronized void cacheAll() throws JWNLException {
        if (factory instanceof AbstractDictionaryElementFactory) {
            ((AbstractDictionaryElementFactory) factory).startCaching();
        }
        super.cacheAll();
        if (factory instanceof AbstractDictionaryElementFactory) {
            ((AbstractDictionaryElementFactory) factory).stopCaching();
        }
    }

    /**
     * A lookahead iterator over a dictionary file.
     * Each element in the enumeration is a line in the enumerated file.
     */
    private abstract class FileLookaheadIterator<E extends DictionaryElement> implements Iterator<E> {

        protected PointedCharSequence next;
        protected final POS pos;
        protected final DictionaryFileType fileType;

        public FileLookaheadIterator(POS pos, DictionaryFileType fileType, boolean skipLookup) throws JWNLException {
            this.pos = pos;
            this.fileType = fileType;
        }

        public FileLookaheadIterator(POS pos, DictionaryFileType fileType) throws JWNLException {
            this.pos = pos;
            this.fileType = fileType;
            next = fileManager.readLineAt(pos, fileType, fileManager.getFirstLineOffset(pos, fileType));
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            E result;
            try {
                result = parseLine(pos, next);
                next = fileManager.readLineAt(pos, fileType, next.getLastBytePosition() + 1);
            } catch (JWNLException e) {
                throw new JWNLRuntimeException(e);
            }
            return result;
        }

        @Override
        public boolean hasNext() {
            return null != next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        protected abstract E parseLine(POS pos, PointedCharSequence line) throws JWNLException;
    }

    private class IndexFileLookaheadIterator extends FileLookaheadIterator<IndexWord> {
        public IndexFileLookaheadIterator(POS pos) throws JWNLException {
            super(pos, DictionaryFileType.INDEX);
        }

        public IndexFileLookaheadIterator(POS pos, boolean skipLookup) throws JWNLException {
            super(pos, DictionaryFileType.INDEX, true);
        }

        @Override
        protected IndexWord parseLine(POS pos, PointedCharSequence line) throws JWNLException {
            return parseAndCacheIndexWord(pos, line);
        }
    }

    private class SubstringIndexFileLookaheadIterator extends IndexFileLookaheadIterator {
        private final String substring;

        public SubstringIndexFileLookaheadIterator(POS pos, String substring) throws JWNLException {
            super(pos, true);
            this.substring = substring;
            next = fileManager.getMatchingLine(pos, fileType, fileManager.getFirstLineOffset(pos, fileType), substring);
        }

        @Override
        public IndexWord next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            IndexWord result;
            try {
                result = parseLine(pos, next);
                next = fileManager.getMatchingLine(pos, fileType, next.getLastBytePosition() + 1, substring);
            } catch (JWNLException e) {
                throw new JWNLRuntimeException(e);
            }
            return result;
        }

        @Override
        public boolean hasNext() {
            return null != next;
        }
    }

    private IndexWord parseAndCacheIndexWord(POS pos, CharSequence line) throws JWNLException {
        return cacheIndexWord(factory.createIndexWord(pos, line));
    }

    private Exc parseAndCacheExceptionLine(POS pos, CharSequence line) throws JWNLException {
        return cacheException(factory.createExc(pos, line));
    }

    private Synset parseAndCacheSynset(POS pos, PointedCharSequence line) throws JWNLException {
        Synset result = null;
        if (null != line) {
            result = factory.createSynset(pos, line);
            for (Word w : result.getWords()) {
                w.setUseCount(fileManager.getUseCount(w.getSenseKeyWithAdjClass()));
            }

            cacheSynset(result);
        }
        return result;
    }
}