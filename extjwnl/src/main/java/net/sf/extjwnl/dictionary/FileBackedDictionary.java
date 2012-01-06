package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.dictionary.file_manager.FileManager;
import net.sf.extjwnl.princeton.data.AbstractPrincetonDictionaryElementFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A <code>Dictionary</code> that retrieves objects from the text files
 * in the WordNet distribution directory.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class FileBackedDictionary extends AbstractCachingDictionary {

    private static final Log log = LogFactory.getLog(FileBackedDictionary.class);
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

    private final FileManager fileManager;
    private final FileDictionaryElementFactory factory;

    public FileBackedDictionary(Document doc) throws JWNLException {
        super(doc);

        FileManager manager = (FileManager) (params.get(FILE_MANAGER)).create();
        // caching is enabled by default
        FileDictionaryElementFactory factory =
                (FileDictionaryElementFactory) (params.get(DICTIONARY_ELEMENT_FACTORY)).create();
        boolean enableCaching =
                !params.containsKey(ENABLE_CACHING) || !params.get(ENABLE_CACHING).getValue().equalsIgnoreCase("false");

        this.setCachingEnabled(enableCaching);
        this.fileManager = manager;
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
        fileManager.close();
    }

    @Override
    public synchronized void delete() throws JWNLException {
        try {
            fileManager.delete();
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
        return fileManager;
    }

    public Iterator<IndexWord> getIndexWordIterator(final POS pos) throws JWNLException {
        if (!isEditable()) {
            return new IndexFileLookaheadIterator(pos);
        } else {
            return super.getIndexWordIterator(pos);
        }
    }

    public Iterator<IndexWord> getIndexWordIterator(final POS pos, final String substring) throws JWNLException {
        if (!isEditable()) {
            return new SubstringIndexFileLookaheadIterator(pos, prepareQueryString(substring.replace(' ', '_')));
        } else {
            return super.getIndexWordIterator(pos, substring);
        }
    }

    public IndexWord getIndexWord(POS pos, String lemma) throws JWNLException {
        lemma = prepareQueryString(lemma);

        IndexWord word = null;
        if (lemma.length() > 0) {
            if (isCachingEnabled()) {
                word = getCachedIndexWord(pos, lemma);
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
            cacheIndexWord(word);
        }
        return word;
    }

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
        Synset synset = getCachedSynset(pos, offset);
        if (!isEditable() && synset == null) {
            try {
                if (line == null) {
                    line = getFileManager().readLineAt(pos, DictionaryFileType.DATA, offset);
                }
                if (null != line) {
                    synset = factory.createSynset(pos, line);
                    for (Word w : synset.getWords()) {
                        w.setUseCount(fileManager.getUseCount(w.getSenseKeyWithAdjClass()));
                    }

                    cacheSynset(synset);
                }
            } catch (IOException e) {
                throw new JWNLException("DICTIONARY_EXCEPTION_005", offset, e);
            }
        }
        return synset;
    }

    public Iterator<Exc> getExceptionIterator(POS pos) throws JWNLException {
        if (!isEditable()) {
            return new FileLookaheadIterator<Exc>(pos, DictionaryFileType.EXCEPTION) {
                protected Exc parseLine(POS pos, long offset, String line) throws JWNLException {
                    Exc exc = null;
                    if (isCachingEnabled()) {
                        String lemma = line.substring(0, line.indexOf(' '));
                        exc = getCachedException(pos, lemma);
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
        if (derivation != null) {
            if (isCachingEnabled()) {
                exc = getCachedException(pos, derivation);
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
            cacheException(exc);
        }
        return exc;
    }

    /**
     * A lookahead iterator over a dictionary file. Each element in the enumeration
     * is a line in the enumerated file.
     */
    private abstract class FileLookaheadIterator<E extends DictionaryElement> implements Iterator<E> {
        protected String currentLine = null;
        protected long currentOffset = -1;
        protected long nextOffset = 0;

        protected boolean more = true;

        protected final POS pos;
        protected final DictionaryFileType fileType;

        public FileLookaheadIterator(POS pos, DictionaryFileType fileType) {
            this.pos = pos;
            this.fileType = fileType;
            try {
                nextOffset = fileManager.getFirstLinePointer(pos, fileType);
                nextLine();
            } catch (IOException ex) {
                if (log.isWarnEnabled()) {
                    log.warn(JWNL.resolveMessage("DICTIONARY_EXCEPTION_007", new Object[]{this.pos, this.fileType}));
                }
            } catch (JWNLException ex) {
                if (log.isWarnEnabled()) {
                    log.warn(JWNL.resolveMessage("DICTIONARY_EXCEPTION_007", new Object[]{this.pos, this.fileType}));
                }
            }
        }

        protected abstract E parseLine(POS pos, long offset, String line) throws JWNLException;

        public final E next() {
            if (hasNext()) {
                E returnVal;
                try {
                    returnVal = parseLine(pos, currentOffset, currentLine);
                    nextLine();
                } catch (JWNLException e) {
                    throw new JWNLRuntimeException(e.getMessage(), e.getCause());
                }
                return returnVal;
            } else {
                throw new NoSuchElementException();
            }
        }

        public final boolean hasNext() {
            return more;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Read the next line in the iterated file.
         */
        protected void nextLine() throws JWNLException {
            try {
                currentLine = fileManager.readLineAt(pos, fileType, nextOffset);
                if (currentLine != null) {
                    nextOffset();
                    return;
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(JWNL.resolveMessage("EXCEPTION_001", e.getMessage()), e);
                }
            }
            more = false;
        }

        protected void nextOffset() throws JWNLException {
            currentOffset = nextOffset;
            nextOffset = getNextOffset(currentOffset);
        }

        protected long getNextOffset(long currentOffset) throws JWNLException {
            try {
                return fileManager.getNextLinePointer(pos, fileType, currentOffset);
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
                String lemma = line.substring(0, line.indexOf(' '));
                word = getCachedIndexWord(this.pos, lemma);
            }
            if (word == null) {
                word = parseAndCacheIndexWordLine(this.pos, line);
            }
            return word;
        }
    }

    private class SubstringIndexFileLookaheadIterator extends IndexFileLookaheadIterator {
        private final String substring;

        public SubstringIndexFileLookaheadIterator(POS pos, String substring) throws JWNLException {
            super(pos);

            //reinitialize
            this.substring = substring;
            currentLine = null;
            currentOffset = -1;
            try {
                nextOffset = getNextOffset(fileManager.getFirstLinePointer(pos, fileType));
            } catch (IOException ex) {
                throw new JWNLException("DICTIONARY_EXCEPTION_008", new Object[]{pos, fileType}, ex);
            }
            nextLine();
        }

        protected void nextLine() throws JWNLException {
            try {
                if (-1 == nextOffset) {
                    currentLine = null;
                } else {
                    currentLine = fileManager.readLineAt(pos, fileType, nextOffset);
                }
                if (currentLine != null) {
                    nextOffset();
                    return;
                }
            } catch (IOException ex) {
                throw new JWNLException("DICTIONARY_EXCEPTION_008", new Object[]{pos, fileType}, ex);
            }
            more = false;
        }

        protected final void nextOffset() throws JWNLException {
            currentOffset = nextOffset;
            nextOffset = getNextOffset(super.getNextOffset(currentOffset));//search next offset from next line
        }

        protected long getNextOffset(long currentOffset) throws JWNLException {
            if (null != substring) {//null == substring in the init of a super, should be skipped
                try {
                    return getFileManager().getMatchingLinePointer(pos, DictionaryFileType.INDEX, currentOffset, substring);
                } catch (IOException ex) {
                    throw new JWNLException("DICTIONARY_EXCEPTION_008", new Object[]{pos, fileType}, ex);
                }
            } else {
                return super.getNextOffset(currentOffset);
            }
        }
    }

    @Override
    public synchronized void edit() throws JWNLException {
        if (!isEditable()) {
            if (!isCachingEnabled()) {
                throw new JWNLException("DICTIONARY_EXCEPTION_030");
            }
            super.edit();
            try {
                fileManager.edit();
            } catch (IOException e) {
                throw new JWNLException("EXCEPTION_001", e.getMessage(), e);
            }
        }
    }

    @Override
    public synchronized void save() throws JWNLException {
        try {
            super.save();
            fileManager.save();
        } catch (IOException e) {
            throw new JWNLException("EXCEPTION_001", e.getMessage(), e);
        }
    }

    @Override
    public synchronized void cacheAll() throws JWNLException {
        if (factory instanceof AbstractPrincetonDictionaryElementFactory) {
            ((AbstractPrincetonDictionaryElementFactory) factory).startCaching();
        }
        super.cacheAll();
        if (factory instanceof AbstractPrincetonDictionaryElementFactory) {
            ((AbstractPrincetonDictionaryElementFactory) factory).stopCaching();
        }
    }
}