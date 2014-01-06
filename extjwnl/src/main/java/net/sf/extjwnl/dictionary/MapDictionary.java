package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import org.w3c.dom.Document;

import java.util.*;

/**
 * Base class for map-based dictionaries.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class MapDictionary extends Dictionary {

    /**
     * Random number generator used by getRandomIndexWord()
     */
    protected static final Random rand = new Random();

    protected final Map<POS, Map<DictionaryFileType, Map<Object, DictionaryElement>>> tableMap;

    public MapDictionary(Document doc) throws JWNLException {
        super(doc);
        tableMap = new EnumMap<POS, Map<DictionaryFileType, Map<Object, DictionaryElement>>>(POS.class);
        for (POS pos : POS.values()) {
            Map<DictionaryFileType, Map<Object, DictionaryElement>> files = new EnumMap<DictionaryFileType, Map<Object, DictionaryElement>>(DictionaryFileType.class);
            tableMap.put(pos, files);
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
    }

    @Override
    public synchronized void edit() throws JWNLException {
        if (!isEditable()) {
            super.edit();
            resolveAllPointers();

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
        return tableMap.get(pos).get(fileType);
    }
}
