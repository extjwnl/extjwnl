package net.didion.jwnl.dictionary;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.JWNLRuntimeException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.file.DictionaryFileType;
import net.didion.jwnl.dictionary.file_manager.FileManager;
import net.didion.jwnl.util.MessageLog;
import net.didion.jwnl.util.MessageLogLevel;
import net.didion.jwnl.util.factory.Param;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A <code>Dictionary</code> that retrieves objects from the text files
 * in the WordNet distribution directory.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class FileBackedDictionary extends AbstractCachingDictionary {

    private static final MessageLog log = new MessageLog(FileBackedDictionary.class);
    /**
     * Morphological processor class install parameter. The value should be the
     * class of MorphologicalProcessor to use.
     */
    public static final String MORPH = "morphological_processor";
    /**
     * File manager install parameter. The value should be the class of FileManager to use.
     */
    public static final String FILE_MANAGER = "file_manager";
    /**
     * The class of FileDictionaryElementFactory to use.
     */
    public static final String DICTIONARY_ELEMENT_FACTORY = "dictionary_element_factory";
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

    private FileManager db = null;
    private FileDictionaryElementFactory factory = null;

    public FileBackedDictionary(Document doc) throws JWNLException {
        super(doc);

        Param param = params.get(MORPH);
        MorphologicalProcessor morph = (param == null) ? null : (MorphologicalProcessor) param.create();
        FileManager manager = (FileManager) (params.get(FILE_MANAGER)).create();
        // caching is enabled by default
        FileDictionaryElementFactory factory =
                (FileDictionaryElementFactory) (params.get(DICTIONARY_ELEMENT_FACTORY)).create();
        boolean enableCaching =
                !params.containsKey(ENABLE_CACHING) || !params.get(ENABLE_CACHING).getValue().equalsIgnoreCase("false");

        this.setMorphologicalProcessor(morph);
        this.setCachingEnabled(enableCaching);
        this.db = manager;
        this.factory = factory;

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
    public void close() {
        db.close();
    }

    @Override
    public void delete() throws JWNLException {
        try {
            db.delete();
        } catch (IOException e) {
            throw new JWNLException("EXCEPTION_001", e.getMessage(), e);
        }
    }

    /**
     * Returns the file manager that backs this database.
     *
     * @return the file manager that backs this database
     */
    protected FileManager getFileManager() {
        return db;
    }

    public FileDictionaryElementFactory getDictionaryElementFactory() {
        return factory;
    }

    //
    // IndexWord methods
    //

    public Iterator<IndexWord> getIndexWordIterator(final POS pos) throws JWNLException {
        if (!isEditable()) {
            return new IndexFileLookaheadIterator(pos);
        } else {
            return super.getIndexWordIterator(pos);
        }
    }

    public Iterator<IndexWord> getIndexWordIterator(final POS pos, final String substring) throws JWNLException {
        if (!isEditable()) {
            return new SubstringIndexFileLookaheadIterator(pos, prepareQueryString(substring));
        } else {
            return super.getIndexWordIterator(pos, substring);
        }
    }


    public IndexWord getIndexWord(POS pos, String lemma) throws JWNLException {
        lemma = prepareQueryString(lemma);

        IndexWord word = null;
        if (lemma.length() > 0) {
            if (isCachingEnabled()) {
                word = getCachedIndexWord(new POSKey(pos, lemma));
            }
            if (!isEditable() && word == null) {
                try {
                    /** determines the offset within the index file */
                    long offset = getFileManager().getIndexedLinePointer(
                            pos, DictionaryFileType.INDEX, lemma.replace(' ', '_'));
                    if (offset >= 0) {
                        word = parseAndCacheIndexWordLine(pos, getFileManager().readLineAt(pos, DictionaryFileType.INDEX, offset));
                    }
                } catch (IOException e) {
                    throw new JWNLException("DICTIONARY_EXCEPTION_004", lemma, e);
                }
            }
        }
        return word;
    }

    public IndexWord getRandomIndexWord(POS pos) throws JWNLException {
        try {
            long offset = getFileManager().getRandomLinePointer(pos, DictionaryFileType.INDEX);
            return parseAndCacheIndexWordLine(pos, getFileManager().readLineAt(pos, DictionaryFileType.INDEX, offset));
        } catch (IOException e) {
            throw new JWNLException("DICTIONARY_EXCEPTION_004", e);
        }
    }

    private IndexWord parseAndCacheIndexWordLine(POS pos, String line) throws JWNLException {
        IndexWord word = factory.createIndexWord(pos, line);
        if (isCachingEnabled() && word != null) {
            cacheIndexWord(new POSKey(pos, word.getLemma()), word);
        }
        return word;
    }

    //
    // Synset methods
    //

    public Iterator<Synset> getSynsetIterator(POS pos) throws JWNLException {
        if (!isEditable()) {
            return new FileLookaheadIterator<Synset>(pos, DictionaryFileType.DATA) {
                protected Synset parseLine(POS pos, long offset, String line) {
                    try {
                        return getSynset(pos, offset, line);
                    } catch (JWNLException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        } else {
            return super.getSynsetIterator(pos);
        }
    }

    public Synset getSynsetAt(POS pos, long offset) throws JWNLException {
        return getSynset(pos, offset, null);
    }

    private Synset getSynset(POS pos, long offset, String line) throws JWNLException {
        POSKey key = new POSKey(pos, offset);
        Synset synset = getCachedSynset(key);
        if (!isEditable() && synset == null) {
            try {
                if (line == null) {
                    line = getFileManager().readLineAt(pos, DictionaryFileType.DATA, offset);
                }
                synset = factory.createSynset(pos, line);
                synset.getWords();

                cacheSynset(key, synset);
            } catch (IOException e) {
                throw new JWNLException("DICTIONARY_EXCEPTION_005", offset, e);
            }
        }
        return synset;
    }

    //
    // Exception methods
    //

    public Iterator<Exc> getExceptionIterator(POS pos) throws JWNLException {
        if (!isEditable()) {
            return new FileLookaheadIterator<Exc>(pos, DictionaryFileType.EXCEPTION) {
                protected Exc parseLine(POS pos, long offset, String line) throws JWNLException {
                    Exc exc = null;
                    if (isCachingEnabled()) {
                        String lemma = line.substring(0, line.indexOf(' '));
                        exc = getCachedException(new POSKey(pos, lemma));
                    }
                    if (exc == null) {
                        exc = parseAndCacheExceptionLine(pos, line);
                    }
                    return exc;
                }
            };
        } else {
            return super.getExceptionIterator(pos);
        }
    }

    public Exc getException(POS pos, String derivation) throws JWNLException {
        derivation = prepareQueryString(derivation);

        Exc exc = null;
        POSKey key;
        if (derivation != null) {
            if (isCachingEnabled()) {
                key = new POSKey(pos, derivation);
                exc = getCachedException(key);
            }
            if (!isEditable() && exc == null) {
                long offset;
                try {
                    offset = getFileManager().getIndexedLinePointer(
                            pos, DictionaryFileType.EXCEPTION, derivation.replace(' ', '_'));
                    if (offset >= 0) {
                        exc = parseAndCacheExceptionLine(pos, getFileManager().readLineAt(pos, DictionaryFileType.EXCEPTION, offset));
                    }
                } catch (IOException ex) {
                    throw new JWNLException("DICTIONARY_EXCEPTION_006", ex);
                }
            }
        }
        return exc;
    }

    private Exc parseAndCacheExceptionLine(POS pos, String line) throws JWNLException {
        Exc exc = factory.createExc(pos, line);
        if (isCachingEnabled() && exc != null) {
            cacheException(new POSKey(pos, exc.getLemma()), exc);
        }
        return exc;
    }

    /**
     * A lookahead iterator over a dictionary file. Each element in the enumeration
     * is a line in the enumerated file.
     */
    private abstract class FileLookaheadIterator<E extends DictionaryElement> implements Iterator<E> {
        private String currentLine = null;
        private long currentOffset = -1;
        private long nextOffset = 0;

        private boolean more = true;

        protected POS pos;
        protected DictionaryFileType fileType;

        public FileLookaheadIterator(POS pos, DictionaryFileType fileType) {
            this.pos = pos;
            this.fileType = fileType;
            try {
                nextOffset = db.getFirstLinePointer(pos, fileType);
                nextLine();
            } catch (IOException ex) {
                log.log(MessageLogLevel.WARN, "DICTIONARY_EXCEPTION_007", new Object[]{this.pos, this.fileType});
            }
        }

        protected abstract E parseLine(POS pos, long offset, String line) throws JWNLException;

        public final E next() {
            if (hasNext()) {
                E returnVal;
                try {
                    returnVal = parseLine(pos, currentOffset, currentLine);
                } catch (JWNLException e) {
                    throw new JWNLRuntimeException(e.getMessage(), e.getCause());
                }
                nextLine();
                return returnVal;
            } else {
                throw new NoSuchElementException();
            }
        }

        public final boolean hasNext() {
            return more;
        }

        /**
         * This method can be over-ridden to remove the currently pointed-at object
         * from the data source backing the iterator.
         */
        public void remove() {
        }

        /**
         * Read the next line in the iterated file.
         */
        protected final void nextLine() {
            try {
                currentLine = db.readLineAt(pos, fileType, nextOffset);
                if (currentLine != null) {
                    nextOffset();
                    return;
                }
            } catch (Exception e) {
                log.log(MessageLogLevel.ERROR, "EXCEPTION_001", e.getMessage(), e);
            }
            more = false;
        }

        protected final void nextOffset() throws JWNLException {
            currentOffset = nextOffset;
            nextOffset = getNextOffset(currentOffset);
        }

        protected long getNextOffset(long currentOffset) throws JWNLException {
            try {
                return db.getNextLinePointer(pos, fileType, currentOffset);
            } catch (IOException ex) {
                throw new JWNLException("DICTIONARY_EXCEPTION_008", new Object[]{pos, fileType}, ex);
            }
        }
    }

    private class IndexFileLookaheadIterator extends FileLookaheadIterator<IndexWord> {
        public IndexFileLookaheadIterator(POS pos) {
            super(pos, DictionaryFileType.INDEX);
        }

        protected IndexWord parseLine(POS pos, long offset, String line) throws JWNLException {
            IndexWord word = null;
            if (isCachingEnabled()) {
                word = getCachedIndexWord(new POSKey(this.pos, offset));
            }
            if (word == null) {
                word = parseAndCacheIndexWordLine(this.pos, line);
            }
            return word;
        }
    }

    private class SubstringIndexFileLookaheadIterator extends IndexFileLookaheadIterator {
        private String substring = null;

        public SubstringIndexFileLookaheadIterator(POS pos, String substring) throws JWNLException {
            super(pos);
            this.substring = substring;
            nextOffset();
        }

        protected long getNextOffset(long currentOffset) throws JWNLException {
            try {
                return getFileManager().getMatchingLinePointer(pos, DictionaryFileType.INDEX, currentOffset, substring);
            } catch (IOException ex) {
                throw new JWNLException("DICTIONARY_EXCEPTION_008", new Object[]{pos, fileType}, ex);
            }
        }
    }

    @Override
    public void edit() throws JWNLException {
        if (!isCachingEnabled()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_030");
        }
        cacheAll();
        try {
            db.edit();
        } catch (IOException e) {
            throw new JWNLException("EXCEPTION_001", e.getMessage(), e);
        }
        super.edit();
    }

    @Override
    public void save() throws JWNLException {
        try {
            db.save();
        } catch (IOException e) {
            throw new JWNLException("EXCEPTION_001", e.getMessage(), e);
        }
    }
}