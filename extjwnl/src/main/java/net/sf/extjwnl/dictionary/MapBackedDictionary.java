package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.file.DictionaryCatalog;
import net.sf.extjwnl.dictionary.file.DictionaryCatalogSet;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.dictionary.file.ObjectDictionaryFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.*;

/**
 * A <code>Dictionary</code> backed by <code>Map</code>s. Warning: this has huge memory requirements.
 * Make sure to start the interpreter with a large enough free memory pool to accommodate this.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class MapBackedDictionary extends Dictionary {

    private static final Log log = LogFactory.getLog(MapBackedDictionary.class);

    /**
     * Random number generator used by getRandomIndexWord()
     */
    private static final Random rand = new Random(new Date().getTime());

    private final Map<MapTableKey, Map<Object, DictionaryElement>> tableMap;
    private final DictionaryCatalogSet<ObjectDictionaryFile> files;

    public MapBackedDictionary(Document doc) throws JWNLException {
        super(doc);
        files = new DictionaryCatalogSet<ObjectDictionaryFile>(this, params, ObjectDictionaryFile.class);
        tableMap = new HashMap<MapTableKey, Map<Object, DictionaryElement>>();
        this.load();
    }

    @Override
    public synchronized void delete() throws JWNLException {
        try {
            files.delete();
        } catch (IOException e) {
            throw new JWNLException("EXCEPTION_001", e.getMessage(), e);
        }
    }

    public IndexWord getIndexWord(POS pos, String lemma) {
        return (IndexWord) getTable(pos, DictionaryFileType.INDEX).get(prepareQueryString(lemma));
    }

    public Iterator<IndexWord> getIndexWordIterator(POS pos, String substring) {
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
        return new AbstractCachingDictionary.IndexWordIterator(itr, substring, start);
    }

    public Iterator<IndexWord> getIndexWordIterator(POS pos) {
        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        Iterator<IndexWord> result = (Iterator<IndexWord>) getIterator(getTable(pos, DictionaryFileType.INDEX));
        return result;
    }

    public IndexWord getRandomIndexWord(POS pos) throws JWNLException {
        // this is a very inefficient implementation, but a better
        // one would require a custom Map implementation that allowed
        // access to the underlying Entry array.
        int index = rand.nextInt(getTable(pos, DictionaryFileType.INDEX).size());
        Iterator<IndexWord> itr = getIndexWordIterator(pos);
        for (int i = 0; i < index && itr.hasNext(); i++) {
            itr.next();
        }
        return itr.hasNext() ? itr.next() : null;
    }

    public Iterator<Synset> getSynsetIterator(POS pos) {
        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        Iterator<Synset> result = (Iterator<Synset>) getIterator(getTable(pos, DictionaryFileType.DATA));
        return result;
    }

    public Iterator<Exc> getExceptionIterator(POS pos) {
        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        Iterator<Exc> result = (Iterator<Exc>) getIterator(getTable(pos, DictionaryFileType.EXCEPTION));
        return result;
    }

    private Iterator<? extends DictionaryElement> getIterator(Map<Object, ? extends DictionaryElement> map) {
        return map.values().iterator();
    }

    public Synset getSynsetAt(POS pos, long offset) {
        return (Synset) getTable(pos, DictionaryFileType.DATA).get(offset);
    }

    public Exc getException(POS pos, String derivation) {
        return (Exc) getTable(pos, DictionaryFileType.EXCEPTION).get(prepareQueryString(derivation));
    }

    public void close() {
        tableMap.clear();
        files.close();
    }

    @Override
    public synchronized void edit() throws JWNLException {
        if (!isEditable()) {
            try {
                super.edit();
                files.edit();

                //update max offsets for new synset creation
                //iteration might take time
                for (POS pos : POS.getAllPOS()) {
                    Long maxOff = 0L;
                    Iterator<Synset> si = getSynsetIterator(pos);
                    while (si.hasNext()) {
                        Synset s = si.next();
                        if (maxOff < s.getOffset()) {
                            maxOff = s.getOffset();
                        }
                    }
                    maxOffset.put(pos, maxOff);
                }
            } catch (IOException e) {
                throw new JWNLException("EXCEPTION_001", e.getMessage(), e);
            }
        }
    }

    @Override
    public synchronized void save() throws JWNLException {
        try {
            super.save();
            files.save();
        } catch (IOException e) {
            throw new JWNLException("EXCEPTION_001", e.getMessage(), e);
        }
    }

    @Override
    public void addSynset(Synset synset) throws JWNLException {
        super.addSynset(synset);
        getTable(synset.getPOS(), DictionaryFileType.DATA).put(synset.getKey(), synset);
    }

    @Override
    public void removeSynset(Synset synset) throws JWNLException {
        getTable(synset.getPOS(), DictionaryFileType.DATA).remove(synset.getKey());
        super.removeSynset(synset);
    }

    @Override
    public void addException(Exc exc) throws JWNLException {
        super.addException(exc);
        getTable(exc.getPOS(), DictionaryFileType.EXCEPTION).put(exc.getKey(), exc);
    }

    @Override
    public void removeException(Exc exc) throws JWNLException {
        getTable(exc.getPOS(), DictionaryFileType.EXCEPTION).remove(exc.getKey());
        super.removeException(exc);
    }

    @Override
    public void addIndexWord(IndexWord indexWord) throws JWNLException {
        super.addIndexWord(indexWord);
        getTable(indexWord.getPOS(), DictionaryFileType.INDEX).put(indexWord.getKey(), indexWord);
    }

    @Override
    public void removeIndexWord(IndexWord indexWord) throws JWNLException {
        getTable(indexWord.getPOS(), DictionaryFileType.INDEX).remove(indexWord.getKey());
        super.removeIndexWord(indexWord);
    }

    public Map<Object, DictionaryElement> getTable(POS pos, DictionaryFileType fileType) {
        return tableMap.get(new MapTableKey(pos, fileType));
    }

    private static final class MapTableKey {
        private final POS pos;
        private final DictionaryFileType fileType;

        private MapTableKey(POS pos, DictionaryFileType fileType) {
            this.pos = pos;
            this.fileType = fileType;
        }

        @Override
        public int hashCode() {
            return pos.hashCode() ^ fileType.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MapTableKey) {
                MapTableKey k = (MapTableKey) obj;
                return pos.equals(k.pos) && fileType.equals(k.fileType);
            }
            return false;
        }

        @Override
        public String toString() {
            return pos.getLabel() + "." + fileType.getName();
        }
    }

    private void load() throws JWNLException {
        synchronized (Dictionary.class) {//because restore variable is static
            Dictionary.setRestoreDictionary(this);
            try {
                if (!files.isOpen()) {
                    try {
                        files.open();
                    } catch (Exception e) {
                        throw new JWNLException("DICTIONARY_EXCEPTION_019", e);
                    }
                }
                // Load all the hash tables into memory
                if (log.isInfoEnabled()) {
                    log.info(JWNL.resolveMessage("DICTIONARY_INFO_009"));
                }
                if (log.isTraceEnabled()) {
                    log.trace(JWNL.resolveMessage("DICTIONARY_INFO_010", Runtime.getRuntime().freeMemory()));
                }

                for (DictionaryFileType fileType : DictionaryFileType.getAllDictionaryFileTypes()) {
                    DictionaryCatalog<ObjectDictionaryFile> catalog = files.get(fileType);
                    for (POS pos : POS.getAllPOS()) {
                        if (log.isInfoEnabled()) {
                            log.info(JWNL.resolveMessage("DICTIONARY_INFO_011", new Object[]{pos.getLabel(), fileType.getName()}));
                        }
                        putTable(pos, fileType, loadDictFile(catalog.get(pos)));
                        if (log.isTraceEnabled()) {
                            log.trace(JWNL.resolveMessage("DICTIONARY_INFO_012", Runtime.getRuntime().freeMemory()));
                        }
                    }
                }
                files.close();
            } finally {
                Dictionary.setRestoreDictionary(null);
            }
        }
    }

    private Map<Object, DictionaryElement> loadDictFile(ObjectDictionaryFile file) throws JWNLException {
        try {
            @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
            Map<Object, DictionaryElement> result = (Map<Object, DictionaryElement>) file.readObject();
            return result;
        } catch (Exception e) {
            throw new JWNLException("DICTIONARY_EXCEPTION_020", file.getFile(), e);
        }
    }

    /**
     * Use <var>table</var> for lookups to the file represented by <var>pos</var> and
     * <var>fileType</var>.
     *
     * @param pos      POS
     * @param fileType element type
     * @param table    hashmap with elements
     */
    private void putTable(POS pos, DictionaryFileType fileType, Map<Object, DictionaryElement> table) {
        tableMap.put(new MapTableKey(pos, fileType), table);
    }
}