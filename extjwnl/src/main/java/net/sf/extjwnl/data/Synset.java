package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.AbstractCachingDictionary;
import net.sf.extjwnl.dictionary.Dictionary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.*;

/**
 * A <code>Synset</code>, or <b>syn</b>onym <b>set</b>, represents a
 * line of a WordNet <var>pos</var><code>.data</code> file. A <code>Synset</code>
 * represents a concept, and contains a set of <code>Word</code>s, each of
 * which has a sense that names that concept (and each of which is therefore
 * synonymous with the other words in the <code>Synset</code>).
 * <p/>
 * <code>Synset</code>'s are linked by {@link Pointer}s into a network of related
 * concepts; this is the <it>Net</it> in WordNet. {@link #getTargets getTargets}
 * retrieves the targets of these links, and {@link #getPointers getPointers}
 * retrieves the pointers themselves.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Synset extends PointerTarget implements DictionaryElement {

    private static final long serialVersionUID = 4L;

    private static final Log log = LogFactory.getLog(Synset.class);

    protected final POS pos;
    protected final PointerList pointers;

    /**
     * The offset of this synset in the data file.
     */
    private long offset;

    /**
     * The words in this synset.
     */
    private final WordList words;

    /**
     * The text (definition, usage examples) associated with the synset.
     */
    private String gloss;

    /**
     * The lexicographer file name id.
     */
    private long lexFileNum;

    //for access control and updates
    private class PointerList extends ArrayList<Pointer> {

        private boolean checkingPointers = false;

        private PointerList() {
        }

        @Override
        public int size() {
            checkPointers();
            return super.size();
        }

        @Override
        public boolean isEmpty() {
            checkPointers();
            return super.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            checkPointers();
            return super.contains(o);
        }

        @Override
        public int indexOf(Object o) {
            checkPointers();
            return super.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            checkPointers();
            return super.lastIndexOf(o);
        }

        @Override
        public Object clone() {
            checkPointers();
            return super.clone();
        }

        @Override
        public Object[] toArray() {
            checkPointers();
            return super.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            checkPointers();
            return super.toArray(a);
        }

        @Override
        public Pointer get(int index) {
            checkPointers();
            return super.get(index);
        }

        @Override
        public Pointer set(int index, Pointer pointer) {
            if (null == pointer) {
                throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_043"));
            }
            Pointer result = super.set(index, pointer);
            checkPointers();
            return result;
        }

        @Override
        public boolean add(Pointer pointer) {
            if (null == pointer) {
                throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_043"));
            }
            boolean result = super.add(pointer);

            if (null != dictionary && dictionary.isEditable() && dictionary.getManageSymmetricPointers()) {
                if (null != pointer.getType().getSymmetricType()) {
                    Pointer symmetric = new Pointer(pointer.getType().getSymmetricType(), pointer.getTarget(), pointer.getSource());
                    if (!pointer.getTarget().getSynset().getPointers().contains(symmetric)) {
                        pointer.getTarget().getSynset().getPointers().add(symmetric);
                    }
                }
            }

            return result;
        }

        @Override
        public void add(int index, Pointer pointer) {
            if (null == pointer) {
                throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_043"));
            }
            super.add(index, pointer);

            if (null != dictionary && dictionary.isEditable() && dictionary.getManageSymmetricPointers()) {
                if (null != pointer.getType().getSymmetricType()) {
                    Pointer symmetric = new Pointer(pointer.getType().getSymmetricType(), pointer.getTarget(), pointer.getSource());
                    if (!pointer.getTarget().getSynset().getPointers().contains(symmetric)) {
                        pointer.getTarget().getSynset().getPointers().add(symmetric);
                    }
                }
            }
        }

        @Override
        public boolean addAll(Collection<? extends Pointer> c) {
            boolean result = false;
            ensureCapacity(size() + c.size());
            for (Pointer p : c) {
                if (add(p)) {
                    result = true;
                }
            }
            return result;
        }

        @Override
        public boolean addAll(int index, Collection<? extends Pointer> c) {
            boolean result = false;
            ensureCapacity(size() + c.size());
            for (Pointer pointer : c) {
                if (add(pointer)) {
                    result = true;
                }
            }
            return result;
        }

        @Override
        public Pointer remove(int index) {
            Pointer result = super.remove(index);
            if (null != dictionary && dictionary.isEditable() && dictionary.getManageSymmetricPointers()) {
                //delete symmetric pointer from the target
                Pointer pointer = get(index);
                if (null != pointer.getType().getSymmetricType()) {
                    for (Pointer p : pointer.getTargetSynset().getPointers()) {
                        if (offset == p.getTargetOffset() && pointer.getType().getSymmetricType().equals(p.getType())) {
                            pointer.getTargetSynset().getPointers().remove(p);
                            break;
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public boolean remove(Object o) {
            boolean result = super.remove(o);
            if (null != dictionary && dictionary.isEditable() && dictionary.getManageSymmetricPointers() && o instanceof Pointer) {
                Pointer pointer = (Pointer) o;
                //delete symmetric pointer from the target
                if (null != pointer.getType().getSymmetricType()) {
                    if (null != pointer.getTargetSynset()) {
                        for (Pointer p : pointer.getTargetSynset().getPointers()) {
                            if (offset == p.getTargetOffset() && pointer.getType().getSymmetricType().equals(p.getType())) {
                                pointer.getTargetSynset().getPointers().remove(p);
                                break;
                            }
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public void clear() {
            if (null != dictionary && dictionary.isEditable()) {
                List<Pointer> copy = new ArrayList<Pointer>(this);
                super.clear();
                for (Pointer pointer : copy) {
                    //delete symmetric pointer from the target
                    if (null != pointer.getType().getSymmetricType() && dictionary.getManageSymmetricPointers()) {
                        for (Pointer p : pointer.getTargetSynset().getPointers()) {
                            if (offset == p.getTargetOffset() && pointer.getType().getSymmetricType().equals(p.getType())) {
                                pointer.getTargetSynset().getPointers().remove(p);
                                break;
                            }
                        }
                    }
                }
            } else {
                super.clear();
            }
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            if (null != dictionary && dictionary.isEditable()) {
                List<Pointer> copy = new ArrayList<Pointer>(super.subList(fromIndex, toIndex));
                super.removeRange(fromIndex, toIndex);
                for (Pointer pointer : copy) {
                    //delete symmetric pointer from the target
                    if (null != pointer.getType().getSymmetricType() && dictionary.getManageSymmetricPointers()) {
                        for (Pointer p : pointer.getTargetSynset().getPointers()) {
                            if (offset == p.getTargetOffset() && pointer.getType().getSymmetricType().equals(p.getType())) {
                                pointer.getTargetSynset().getPointers().remove(p);
                                break;
                            }
                        }
                    }
                }
            } else {
                super.removeRange(fromIndex, toIndex);
            }
        }

        @Override
        public Iterator<Pointer> iterator() {
            checkPointers();
            return super.iterator();
        }

        @Override
        public ListIterator<Pointer> listIterator() {
            checkPointers();
            return super.listIterator();
        }

        @Override
        public ListIterator<Pointer> listIterator(int index) {
            checkPointers();
            return super.listIterator(index);
        }

        @Override
        public List<Pointer> subList(int fromIndex, int toIndex) {
            checkPointers();
            return super.subList(fromIndex, toIndex);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            checkPointers();
            return super.containsAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            if (null != dictionary && dictionary.isEditable()) {
                List<Pointer> copy = new ArrayList<Pointer>(this);
                boolean result = super.removeAll(c);
                for (Object object : c) {
                    if (object instanceof Pointer) {
                        Pointer pointer = (Pointer) object;
                        if (copy.contains(pointer)) {
                            //delete symmetric pointer from the target
                            if (null != pointer.getType().getSymmetricType() && dictionary.getManageSymmetricPointers()) {
                                for (Pointer p : pointer.getTargetSynset().getPointers()) {
                                    if (offset == p.getTargetOffset() && pointer.getType().getSymmetricType().equals(p.getType())) {
                                        pointer.getTargetSynset().getPointers().remove(p);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                return result;
            } else {
                return super.removeAll(c);
            }
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            if (null != dictionary && dictionary.isEditable()) {
                List<Pointer> copy = new ArrayList<Pointer>(this);
                boolean result = super.retainAll(c);
                for (Object object : c) {
                    if (object instanceof Pointer) {
                        Pointer pointer = (Pointer) object;
                        if (!copy.contains(pointer)) {
                            //delete symmetric pointer from the target
                            if (null != pointer.getType().getSymmetricType() && dictionary.getManageSymmetricPointers()) {
                                for (Pointer p : pointer.getTargetSynset().getPointers()) {
                                    if (offset == p.getTargetOffset() && pointer.getType().getSymmetricType().equals(p.getType())) {
                                        pointer.getTargetSynset().getPointers().remove(p);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                return result;
            } else {
                return super.retainAll(c);
            }
        }

        private void checkPointers() {
            if (null != dictionary && dictionary.getCheckAlienPointers() && !checkingPointers) {
                synchronized (this) {
                    checkingPointers = true;
                    if (null != dictionary && dictionary.isEditable()) {
                        List<Pointer> toDelete = null;
                        for (int i = 0; i < super.size(); i++) {
                            Pointer pointer = super.get(i);
                            if (dictionary != pointer.getSource().getDictionary() || null == pointer.getTarget() || dictionary != pointer.getTarget().getDictionary()) {
                                if (null == toDelete) {
                                    toDelete = new ArrayList<Pointer>();
                                }
                                toDelete.add(pointer);
                            }
                        }
                        if (null != toDelete) {
                            if (log.isWarnEnabled() && 0 < toDelete.size()) {
                                log.warn(JWNL.resolveMessage("DICTIONARY_WARN_002", Synset.this.getOffset()));
                            }
                            for (Pointer pointer : toDelete) {
                                remove(pointer);
                            }
                        }
                    }
                    checkingPointers = false;
                }
            }
        }
    }

    private class WordList extends ArrayList<Word> {

        private WordList() {
        }

        @Override
        public Word set(int index, Word word) {
            if (null == word) {
                throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_044"));
            }
            if (Synset.this.dictionary != word.getDictionary()) {
                throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_040"));
            }
            if (null != dictionary && dictionary.isEditable()) {
                Word result = super.set(index, word);
                if (null != result) {
                    removeFromIndexWords(result);
                }
                addToIndexWords(word);
                return result;
            } else {
                return super.set(index, word);
            }
        }

        @Override
        public boolean add(Word word) {
            if (null != dictionary && dictionary.isEditable()) {
                add(size(), word);
                return true;
            } else {
                return super.add(word);
            }
        }

        @Override
        public void add(int index, Word word) {
            if (null == word) {
                throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_044"));
            }
            if (Synset.this.dictionary != word.getDictionary()) {
                throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_040"));
            }
            if (null != dictionary && dictionary.isEditable()) {
                super.add(index, word);
                addToIndexWords(word);
            } else {
                super.add(index, word);
            }
        }

        @Override
        public boolean addAll(Collection<? extends Word> c) {
            if (null != dictionary && dictionary.isEditable()) {
                boolean result = false;
                ensureCapacity(size() + c.size());
                for (Word word : c) {
                    if (add(word)) {
                        result = true;
                    }
                }
                return result;
            } else {
                return super.addAll(c);
            }
        }

        @Override
        public boolean addAll(int index, Collection<? extends Word> c) {
            if (null != dictionary && dictionary.isEditable()) {
                ensureCapacity(size() + c.size());
                for (Word word : c) {
                    add(index, word);
                    index++;
                }
                return true;
            } else {
                return super.addAll(index, c);
            }
        }

        @Override
        public Word remove(int index) {
            if (null != dictionary && dictionary.isEditable()) {
                Word result = super.remove(index);
                removeFromIndexWords(super.get(index));
                return result;
            } else {
                return super.remove(index);
            }
        }

        @Override
        public boolean remove(Object o) {
            if (null != dictionary && dictionary.isEditable()) {
                boolean result = super.remove(o);
                if (o instanceof Word) {
                    removeFromIndexWords((Word) o);
                }
                return result;
            } else {
                return super.remove(o);
            }
        }

        @Override
        public void clear() {
            if (null != dictionary && dictionary.isEditable()) {
                List<Word> copy = new ArrayList<Word>(this);
                super.clear();
                for (Word word : copy) {
                    removeFromIndexWords(word);
                }
            } else {
                super.clear();
            }
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            if (null != dictionary && dictionary.isEditable()) {
                List<Word> copy = new ArrayList<Word>(subList(fromIndex, toIndex));
                super.removeRange(fromIndex, toIndex);
                for (Word word : copy) {
                    removeFromIndexWords(word);
                }
            } else {
                super.removeRange(fromIndex, toIndex);
            }
        }

        private void removeFromIndexWords(Word word) {
            if (null != dictionary && dictionary.isEditable()) {
                try {
                    //take care of IndexWords
                    IndexWord indexWord = dictionary.getIndexWord(getPOS(), word.getLemma());
                    if (null != indexWord) {
                        indexWord.getSenses().remove(Synset.this);
                    }
                } catch (JWNLException e) {
                    if (log.isErrorEnabled()) {
                        log.error(JWNL.resolveMessage("EXCEPTION_001", e.getMessage()), e);
                    }
                }
            }
        }

        private void addToIndexWords(Word word) {
            if (null != dictionary && dictionary.isEditable()) {
                try {
                    //take care of IndexWords
                    IndexWord iw = dictionary.getIndexWord(word.getPOS(), word.getLemma());
                    if (null == iw) {
                        dictionary.createIndexWord(word.getPOS(), word.getLemma(), Synset.this);
                    } else {
                        iw.getSenses().add(Synset.this);
                    }
                } catch (JWNLException e) {
                    if (log.isErrorEnabled()) {
                        if (log.isErrorEnabled()) {
                            log.error(JWNL.resolveMessage("EXCEPTION_001", e.getMessage()), e);
                        }
                    }
                }
            }
        }
    }

    public Synset(Dictionary dictionary, POS pos) throws JWNLException {
        super(dictionary);
        if (null == pos) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_041"));
        }
        this.pos = pos;
        pointers = new PointerList();
        words = new WordList();

        if (null != dictionary && dictionary.isEditable()) {
            dictionary.addSynset(this);
        }
    }

    public Synset(Dictionary dictionary, POS pos, long offset) throws JWNLException {
        super(dictionary);
        if (null == pos) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_041"));
        }
        this.pos = pos;
        pointers = new PointerList();
        words = new WordList();

        this.offset = offset;
        if (null != dictionary && dictionary.isEditable()) {
            dictionary.addSynset(this);
        }
    }

    public DictionaryElementType getType() {
        return DictionaryElementType.SYNSET;
    }

    public Object getKey() {
        return getOffset();
    }

    public POS getPOS() {
        return pos;
    }

    /**
     * Two Synsets are equal if their POS's and offsets are equal
     */
    public boolean equals(Object object) {
        return (object instanceof Synset) && ((Synset) object).getPOS().equals(getPOS()) && ((Synset) object).getOffset() == getOffset();
    }

    public int hashCode() {
        return getPOS().hashCode() ^ (int) getOffset();
    }

    public String toString() {
        StringBuilder words = new StringBuilder();
        for (int i = 0; i < this.words.size(); ++i) {
            if (i > 0) {
                words.append(", ");
            }
            words.append(this.words.get(i).getLemma());
        }

        if (getGloss() != null) {
            words.append(" -- (").append(getGloss()).append(")");
        }

        return JWNL.resolveMessage("DATA_TOSTRING_009", new Object[]{getOffset(), getPOS(), words.toString()});
    }

    @Override
    public Synset getSynset() {
        return this;
    }

    @Override
    public int getIndex() {
        return 0;
    }

    public List<Pointer> getPointers() {
        return pointers;
    }

    public String getGloss() {
        if (null == gloss) {
            return "";
        } else {
            return gloss;
        }
    }

    public void setGloss(String gloss) {
        if (null == gloss) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_048"));
        }
        this.gloss = gloss;
    }

    public List<Word> getWords() {
        return words;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) throws JWNLException {
        if (this.offset != offset) {
            if (dictionary instanceof AbstractCachingDictionary) {
                AbstractCachingDictionary acd = (AbstractCachingDictionary) dictionary;
                Synset oldSynset = acd.getSynsetAt(pos, offset);
                if (null != oldSynset) {
                    if (log.isWarnEnabled()) {
                        log.warn(JWNL.resolveMessage("DICTIONARY_WARN_003", oldSynset));
                    }
                }
                acd.clearSynset(pos, this.offset);
                this.offset = offset;
                acd.cacheSynset(this);
            } else {
                this.offset = offset;
            }
        }
    }

    public boolean isAdjectiveCluster() {
        throw new UnsupportedOperationException();
    }

    public void setIsAdjectiveCluster(boolean isAdjectiveCluster) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns all Verb Frames that are valid for all the words in this synset.
     *
     * @return all Verb Frames that are valid for all the words in this synset
     */
    public String[] getVerbFrames() {
        throw new UnsupportedOperationException();
    }

    public BitSet getVerbFrameFlags() {
        throw new UnsupportedOperationException();
    }

    public void setVerbFrameFlags(BitSet verbFrameFlags) {
        throw new UnsupportedOperationException();
    }

    public int[] getVerbFrameIndices() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true if <var>lemma</var> is one of the words contained in this synset.
     *
     * @param lemma lemma to check
     * @return true if <var>lemma</var> is one of the words contained in this synset
     */
    public boolean containsWord(String lemma) {
        if (null == lemma) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_046"));
        }
        for (Word word : words) {
            if (word.getLemma().equalsIgnoreCase(lemma)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index of the word which has the <var>lemma</var> or -1 if not found.
     *
     * @param lemma lemma to check
     * @return true if <var>lemma</var> is one of the words contained in this synset
     */
    public int indexOfWord(String lemma) {
        if (null == lemma) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_046"));
        }
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).getLemma().equalsIgnoreCase(lemma)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the lexicographer file name containing this synset.
     *
     * @return two digit decimal integer
     */
    public long getLexFileNum() {
        return lexFileNum;
    }

    /**
     * Sets the lexicographer file name containing this synset.
     *
     * @param lexFileNum - the lexicographer file name number
     */
    public void setLexFileNum(long lexFileNum) {
        this.lexFileNum = lexFileNum;
    }

    /**
     * Returns the lexicographer file name.
     *
     * @return lexicographer file name
     */
    public String getLexFileName() {
        return LexFileIdFileNameMap.getMap().get(lexFileNum);
    }

    @Override
    public void setDictionary(Dictionary dictionary) throws JWNLException {
        if (dictionary != this.dictionary) {
            if (null != this.dictionary) {
                Dictionary old = this.dictionary;
                this.dictionary = dictionary;
                old.removeElement(this);
            }
            super.setDictionary(dictionary);
            if (null != dictionary) {
                dictionary.addElement(this);
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        dictionary = Dictionary.getRestoreDictionary();
    }
}