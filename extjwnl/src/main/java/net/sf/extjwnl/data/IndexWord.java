package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.ResourceBundleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * An <code>IndexWord</code> represents a line of the <b>pos</b><code>.index</code> file.
 * An <code>IndexWord</code> is created or retrieved via {@link Dictionary#lookupIndexWord lookupIndexWord}.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class IndexWord extends BaseDictionaryElement {

    private static final long serialVersionUID = 5L;

    private static final Logger log = LoggerFactory.getLogger(IndexWord.class);

    /**
     * This word's part-of-speech
     */
    private POS pos;
    /**
     * The string representation of this IndexWord
     */
    private String lemma;
    /**
     * senses are initially stored as offsets, and paged in on demand.
     */
    private volatile long[] synsetOffsets;
    /**
     * This is null until getSenses has been called.
     */
    private transient SynsetList synsets = null;

    private class SynsetList extends ArrayList<Synset> {
        private SynsetList() {
            super();
        }

        private SynsetList(int initialCapacity) {
            super(initialCapacity);
        }

        private void replaceSenses(Collection<Synset> senses) {
            super.clear();
            super.addAll(senses);
        }

        @Override
        public int size() {
            if (null != synsetOffsets) {
                return synsetOffsets.length;
            } else {
                return super.size();
            }
        }

        @Override
        public boolean isEmpty() {
            if (null != synsetOffsets) {
                return 0 == synsetOffsets.length;
            } else {
                return super.isEmpty();
            }
        }

        @Override
        public boolean contains(Object o) {
            loadAllSynsets();
            return super.contains(o);
        }

        @Override
        public int indexOf(Object o) {
            loadAllSynsets();
            return super.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            loadAllSynsets();
            return super.lastIndexOf(o);
        }

        @Override
        public Object clone() {
            loadAllSynsets();
            return super.clone();
        }

        @Override
        public Object[] toArray() {
            loadAllSynsets();
            return super.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            loadAllSynsets();
            return super.toArray(a);
        }

        @Override
        public Synset get(int index) {
            loadAllSynsets();
            return super.get(index);
        }

        @Override
        public Synset set(int index, Synset synset) {
            checkSynsetIsNotNull(synset);
            checkDictionaryIsOurs(synset);
            loadAllSynsets();
            if (null != dictionary && dictionary.isEditable()) {
                Synset result = super.set(index, synset);
                if (null != result) {
                    removeWordsFromSynset(result, lemma);
                }
                addWord(synset, lemma);
                return result;
            } else {
                return super.set(index, synset);
            }
        }

        @Override
        public boolean add(Synset synset) {
            checkSynsetIsNotNull(synset);
            checkDictionaryIsOurs(synset);
            loadAllSynsets();
            if (null != dictionary && dictionary.isEditable()) {
                boolean result = super.add(synset);
                addWord(synset, lemma);
                return result;
            } else {
                return super.add(synset);
            }
        }

        @Override
        public void add(int index, Synset synset) {
            checkSynsetIsNotNull(synset);
            checkDictionaryIsOurs(synset);
            loadAllSynsets();
            if (null != dictionary && dictionary.isEditable()) {
                super.add(index, synset);
                addWord(synset, lemma);
            } else {
                super.add(index, synset);
            }
        }

        @Override
        public Synset remove(int index) {
            loadAllSynsets();
            if (null != dictionary && dictionary.isEditable()) {
                Dictionary d = dictionary;
                Synset result = super.remove(index);
                if (null != result) {
                    removeWordsFromSynset(result, lemma);
                    checkIfWeReEmptyAndRemoveIndexWord(d);
                }
                return result;
            } else {
                return super.remove(index);
            }
        }

        @Override
        public boolean remove(Object o) {
            loadAllSynsets();
            if (null != dictionary && dictionary.isEditable()) {
                Dictionary d = dictionary;
                boolean result = super.remove(o);
                if (result && o instanceof Synset) {
                    removeWordsFromSynset((Synset) o, lemma);
                    checkIfWeReEmptyAndRemoveIndexWord(d);
                }
                return result;
            } else {
                return super.remove(o);
            }
        }

        @Override
        public void clear() {
            loadAllSynsets();
            if (null != dictionary && dictionary.isEditable()) {
                Dictionary d = dictionary;
                List<Synset> copy = new ArrayList<>(this);
                super.clear();
                for (Synset synset : copy) {
                    removeWordsFromSynset(synset, lemma);
                }
                checkIfWeReEmptyAndRemoveIndexWord(d);
            } else {
                super.clear();
            }
        }

        @Override
        public boolean addAll(Collection<? extends Synset> c) {
            loadAllSynsets();
            if (null != dictionary && dictionary.isEditable()) {
                boolean result = false;
                for (Synset synset : c) {
                    if (add(synset)) {
                        result = true;
                    }
                }
                return result;
            } else {
                for (Synset synset : c) {
                    checkSynsetIsNotNull(synset);
                }
                return super.addAll(c);
            }
        }

        @Override
        public boolean addAll(int index, Collection<? extends Synset> c) {
            loadAllSynsets();
            if (null != dictionary && dictionary.isEditable()) {
                for (Synset synset : c) {
                    add(index, synset);
                    index++;
                }
                return true;
            } else {
                for (Synset synset : c) {
                    checkSynsetIsNotNull(synset);
                }
                return super.addAll(index, c);
            }
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            loadAllSynsets();
            if (null != dictionary && dictionary.isEditable()) {
                List<Synset> copy = new ArrayList<>(subList(fromIndex, toIndex));
                super.removeRange(fromIndex, toIndex);
                for (Synset synset : copy) {
                    removeWordsFromSynset(synset, lemma);
                }
            } else {
                super.removeRange(fromIndex, toIndex);
            }
        }

        @Override
        public Iterator<Synset> iterator() {
            loadAllSynsets();
            return super.iterator();
        }

        @Override
        public ListIterator<Synset> listIterator() {
            loadAllSynsets();
            return super.listIterator();
        }

        @Override
        public ListIterator<Synset> listIterator(int index) {
            loadAllSynsets();
            return super.listIterator(index);
        }

        @Override
        public List<Synset> subList(int fromIndex, int toIndex) {
            loadAllSynsets();
            return super.subList(fromIndex, toIndex);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            loadAllSynsets();
            return super.containsAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            loadAllSynsets();
            if (null != dictionary && dictionary.isEditable()) {
                Dictionary d = dictionary;
                List<Synset> copy = new ArrayList<>(this);
                boolean result = super.removeAll(c);
                if (result) {
                    for (Object object : c) {
                        if (object instanceof Synset) {
                            Synset synset = (Synset) object;
                            if (copy.contains(synset)) {
                                removeWordsFromSynset(synset, lemma);
                            }
                        }
                    }
                    checkIfWeReEmptyAndRemoveIndexWord(d);
                }
                return result;
            } else {
                return super.removeAll(c);
            }
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            loadAllSynsets();
            if (null != dictionary && dictionary.isEditable()) {
                Dictionary d = dictionary;
                List<Synset> copy = new ArrayList<>(this);
                boolean result = super.retainAll(c);
                if (result) {
                    for (Synset synset : copy) {
                        if (!c.contains(synset)) {
                            removeWordsFromSynset(synset, lemma);
                        }
                    }
                    checkIfWeReEmptyAndRemoveIndexWord(d);
                }
                return result;
            } else {
                return super.retainAll(c);
            }
        }

        @Override
        public Spliterator<Synset> spliterator() {
            loadAllSynsets();
            return super.spliterator();
        }

        private void checkIfWeReEmptyAndRemoveIndexWord(Dictionary d) {
            if (0 == super.size()) {
                try {
                    d.removeIndexWord(IndexWord.this);
                } catch (JWNLException e) {
                    throw new JWNLRuntimeException(e);
                }
            }
        }

        private void checkSynsetIsNotNull(Synset synset) {
            if (null == synset) {
                if (null != dictionary) {
                    throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_042"));
                } else {
                    throw new IllegalArgumentException("Synset must be not null");
                }
            }
        }

        private void checkDictionaryIsOurs(Synset synset) {
            if (dictionary != synset.getDictionary()) {
                if (null != dictionary) {
                    throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_040"));
                } else {
                    throw new IllegalArgumentException("Dictionary element must belong to this dictionary");
                }
            }
        }

        private void addWord(Synset synset, String lemma) {
            if (null != synset.getDictionary() && synset.getDictionary().isEditable()) {
                if (!synset.containsWord(lemma)) {
                    synset.getWords().add(new Word(synset.getDictionary(), synset, lemma));
                }
            }
        }

        private void removeWordsFromSynset(Synset synset, String lemma) {
            if (null != dictionary && dictionary.isEditable()) {
                for (Word word : synset.getWords()) {
                    if (word.getLemma().equalsIgnoreCase(lemma)) {
                        synset.getWords().remove(word);
                        break;
                    }
                }
            }
        }

        private void loadAllSynsets() {
            if (null != synsetOffsets) {
                synchronized (this) {
                    if (null != synsetOffsets) {
                        super.ensureCapacity(synsetOffsets.length);
                        for (long synsetOffset : synsetOffsets) {
                            Synset synset = loadSynset(synsetOffset);
                            if (null != synset) {
                                super.add(synset);
                            } else {
                                if (null != dictionary) {
                                    log.warn(dictionary.getMessages().resolveMessage("DICTIONARY_WARN_004", new Object[]{synsetOffset, getLemma()}));
                                } else {
                                    log.warn(ResourceBundleSet.insertParams("Synset {0} not found while loading index word {1}", new Object[]{synsetOffset, getLemma()}));
                                }
                            }
                        }
                        synsetOffsets = null;
                    }
                }
            }
        }

        private Synset loadSynset(long offset) {
            try {
                return null == dictionary ? null : dictionary.getSynsetAt(pos, offset);
            } catch (JWNLException e) {
                throw new JWNLRuntimeException(e);
            }
        }
    }

    protected IndexWord(Dictionary dictionary, String lemma, POS pos) throws JWNLException {
        this.dictionary = dictionary;
        if (null == lemma) {
            if (null != dictionary) {
                throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_046"));
            } else {
                throw new IllegalArgumentException("Lemma must be not null and not empty");
            }
        }
        this.lemma = lemma.toLowerCase();
        if (null == pos) {
            if (null != dictionary) {
                throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_041"));
            } else {
                throw new IllegalArgumentException("Pos must be not null");
            }
        }
        this.pos = pos;
        if (null != dictionary && dictionary.isEditable()) {
            dictionary.addElement(this);
        }
    }

    public IndexWord(Dictionary dictionary, String lemma, POS pos, Synset synset) throws JWNLException {
        this(dictionary, lemma, pos);
        if (null == synset) {
            if (null != dictionary) {
                throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_042"));
            } else {
                throw new IllegalArgumentException("Synset must be not null");
            }
        }
        if (synset.getPOS() != pos) {
            if (null != dictionary) {
                throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_062"));
            } else {
                throw new IllegalArgumentException("Synset POS must be equal to index word POS");
            }
        }
        this.synsets = new SynsetList(1);
        this.synsets.add(synset);
    }

    public IndexWord(Dictionary dictionary, String lemma, POS pos, long[] synsetOffsets) throws JWNLException {
        this(dictionary, lemma, pos);
        if (null == synsetOffsets || 0 == synsetOffsets.length) {
            if (null != dictionary) {
                throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_047"));
            } else {
                throw new IllegalArgumentException("Synset offsets must be be not null and not empty");
            }
        }
        this.synsetOffsets = synsetOffsets;
    }

    public DictionaryElementType getType() {
        return DictionaryElementType.INDEX_WORD;
    }

    /**
     * Returns the lemma of this word.
     *
     * @return lemma
     */
    public Object getKey() {
        return lemma;
    }

    /**
     * Returns the word's part-of-speech.
     *
     * @return the word's part-of-speech
     */
    public POS getPOS() {
        return pos;
    }

    // Object methods	//

    /**
     * Returns true if the lemma and the part of speech both match.
     */
    public boolean equals(Object object) {
        return (object instanceof IndexWord)
                && ((IndexWord) object).getLemma().equals(getLemma()) && ((IndexWord) object).getPOS().equals(getPOS());
    }

    public int hashCode() {
        return getLemma().hashCode() ^ getPOS().hashCode();
    }

    public String toString() {
        return ResourceBundleSet.insertParams("[IndexWord: [Lemma: {0}] {1}]", new Object[]{getLemma(), getPOS()});
    }

    /**
     * Return the word's lemma.  Its lemma is its orthographic representation, for
     * example {@code "dog"} or {@code "get up"}.
     *
     * @return the word's lemma
     */
    public String getLemma() {
        return lemma;
    }

    public long[] getSynsetOffsets() {
        if (null == synsetOffsets) {
            long[] result = new long[synsets.size()];
            for (int i = 0; i < synsets.size(); i++) {
                result[i] = synsets.get(i).getOffset();
            }
            return result;
        }
        return synsetOffsets;
    }

    /**
     * Returns the senses of this word.
     *
     * @return all the senses of this word
     */
    public List<Synset> getSenses() {
        if (null == synsets) {
            synsets = new SynsetList();
        }
        return synsets;
    }

    /**
     * Sorts senses according to their use count.
     *
     * @return number of tagged senses (senses with non-zero use count)
     */
    public int sortSenses() {
        int result = 0;

        if (1 < getSenses().size()) {
            // sort senses and find out tagged sense count
            List<Synset> ucSenses = new ArrayList<>(getSenses().size());
            List<Synset> nonUCSenses = new ArrayList<>(getSenses().size());
            for (Synset synset : getSenses()) {
                if (0 < getUseCount(synset, lemma)) {
                    ucSenses.add(synset);
                } else {
                    nonUCSenses.add(synset);
                }
            }
            ucSenses.sort(Collections.reverseOrder(Comparator.comparingInt(o -> getUseCount(o, lemma))));

            result = ucSenses.size();

            // other synsets seems to be sorted by decreasing offsets
            nonUCSenses.sort(Collections.reverseOrder(synsetOffsetComparator));

            // if ADJ, output cluster heads, then fans
            if (POS.ADJECTIVE == getPOS()) {
                int i = 0;
                while (i < nonUCSenses.size()) {
                    if (!nonUCSenses.get(i).isAdjectiveCluster()) {
                        ucSenses.add(nonUCSenses.remove(i));
                    } else {
                        i++;
                    }
                }
            }
            ucSenses.addAll(nonUCSenses);
            synsets.replaceSenses(ucSenses);
        } else if (1 == getSenses().size()) {
            if (0 < getUseCount(getSenses().get(0), lemma)) {
                result = 1;
            }
        }

        return result;
    }

    private static final Comparator<Synset> synsetOffsetComparator = (o1, o2) -> {
        long result = o1.getOffset() - o2.getOffset();
        if (0 != result) {
            // 2 huge offsets might lead to integer overflow
            return (int) (result / Math.abs(result));
        } else {
            if (POS.ADJECTIVE == o1.getPOS() && POS.ADJECTIVE == o2.getPOS()) {
                if (o1.isAdjectiveCluster() && !o2.isAdjectiveCluster()) {
                    return 1;
                } else if (o2.isAdjectiveCluster() && !o1.isAdjectiveCluster()) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }
    };

    private static int getUseCount(Synset synset, String lemma) {
        for (Word w : synset.getWords()) {
            if (w.getLemma().equalsIgnoreCase(lemma)) {
                if (0 < w.getUseCount()) {
                    return w.getUseCount();
                }
            }
        }
        return 0;
    }

    private void writeObject(java.io.ObjectOutputStream oos) throws IOException {
        boolean synsetOffsetsNull = (null == synsetOffsets);
        synsetOffsets = getSynsetOffsets();
        oos.defaultWriteObject();
        if (synsetOffsetsNull) {
            synsetOffsets = null;
        }
    }
}