package net.didion.jwnl.data;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;

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
 */
public class Synset extends PointerTarget implements DictionaryElement {
    static final long serialVersionUID = 4038955719653496529L;

    protected POS _pos;
    protected Pointer[] _pointers;
    protected final static Pointer[] _emptyPointers = new Pointer[0];
    /**
     * The offset of this synset in the data file.
     */
    protected long _offset;
    /**
     * The words in this synset.
     */
    protected Word[] _words;
    protected final static Word[] _emptyWords = new Word[0];
    /**
     * The text (definition, usage examples) associated with the synset.
     */
    protected String _gloss;
    protected BitSet _verbFrameFlags;
    protected final static BitSet _emptyVerbFrameFlags = new BitSet();
    /**
     * for use only with WordNet 1.6 and earlier
     */
    protected boolean _isAdjectiveCluster;

    /**
     * The lexicographer file name id.
     */
    protected long lexFileNum;

    public Synset() {
        _pointers = _emptyPointers;
        _words = _emptyWords;
        _verbFrameFlags = _emptyVerbFrameFlags;
    }

    public Synset(POS pos, long offset, Word[] words, Pointer[] pointers, String gloss, BitSet verbFrames) {
        this(pos, offset, words, pointers, gloss, verbFrames, false);
    }

    public Synset(POS pos, long offset, Word[] words, Pointer[] pointers, String gloss,
                  BitSet verbFrames, boolean isAdjectiveCluster) {
        this();
        _pos = pos;
        if (null != pointers) {
            _pointers = pointers;
        }
        _offset = offset;
        if (null != words) {
            _words = words;
        }
        _gloss = gloss;
        if (null != verbFrames) {
            _verbFrameFlags = verbFrames;
        }
        _isAdjectiveCluster = isAdjectiveCluster;
    }

    public DictionaryElementType getType() {
        return DictionaryElementType.SYNSET;
    }

    // Object methods

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
        for (int i = 0; i < getWordsSize(); ++i) {
            if (i > 0) {
                words.append(", ");
            }
            words.append(getWord(i).getLemma());
        }

        if (getGloss() != null) {
            words.append(" -- (").append(getGloss()).append(")");
        }


        return JWNL.resolveMessage("DATA_TOSTRING_009", new Object[]{getOffset(), getPOS(), words.toString()});
    }

    // Accessors

    public POS getPOS() {
        return _pos;
    }

    @Override
    public Synset getSynset() {
        return this;
    }

    @Override
    public int getIndex() {
        return 0;
    }

    public void setPOS(POS pos) {
        _pos = pos;
    }

    public Pointer[] getPointers() {
        return _pointers;
    }

    public boolean addPointer(Pointer p) {
        if (-1 == getPointerIndex(p)) {
            Pointer[] newPointers = new Pointer[_pointers.length + 1];
            System.arraycopy(_pointers, 0, newPointers, 0, _pointers.length);
            newPointers[newPointers.length - 1] = p;
            _pointers = newPointers;
            return true;
        } else {
            return false;
        }
    }

    public int getPointerIndex(Pointer p) {
        int result = -1;
        for (int i = 0; i < _pointers.length; i++) {
            if (p.equals(_pointers[i])) {
                return i;
            }
        }
        return result;
    }

    public int addPointers(Collection<Pointer> pointers) {
        int equal = 0;
        for (Pointer p : pointers) {
            if (-1 < getPointerIndex(p)) {
                equal++;
            }
        }
        Pointer[] newPointers = new Pointer[_pointers.length + pointers.size() - equal];
        System.arraycopy(_pointers, 0, newPointers, 0, _pointers.length);
        int idx = _pointers.length;
        for (Pointer p : pointers) {
            if (-1 == getPointerIndex(p)) {
                newPointers[idx] = p;
                idx++;
            }
        }
        _pointers = newPointers;
        return pointers.size() - equal;
    }

    public String getGloss() {
        return _gloss;
    }

    public void setGloss(String gloss) {
        _gloss = gloss;
    }

    public Word[] getWords() {
        return _words;
    }

    public int getWordsSize() {
        return getWords().length;
    }

    public Word getWord(int index) {
        return _words[index];
    }

    public boolean addWord(Word w) {
        if (-1 == getWordIndex(w)) {
            Word[] newWords = new Word[_words.length + 1];
            System.arraycopy(_words, 0, newWords, 0, _words.length);
            newWords[newWords.length - 1] = w;
            _words = newWords;
            return true;
        } else {
            return false;
        }
    }

    public boolean addWords(Collection<Word> words) {
        int equal = 0;
        for (Word w : words) {
            if (-1 < getWordIndex(w)) {
                equal++;
            }
        }
        Word[] newWords = new Word[_words.length + words.size() - equal];
        System.arraycopy(_words, 0, newWords, 0, _words.length);
        int idx = _words.length;
        for (Word w : words) {
            if (-1 == getWordIndex(w)) {
                newWords[idx] = w;
                idx++;
            }
        }
        _words = newWords;
        return equal < words.size();
    }

    public int getWordIndex(Word w) {
        int result = -1;
        for (int i = 0; i < _words.length; i++) {
            if (w.equals(_words[i])) {
                return i;
            }
        }
        return result;
    }

    public long getOffset() {
        return _offset;
    }

    public void setOffset(long offset) {
        _offset = offset;
    }

    public Object getKey() {
        return getOffset();
    }

    public boolean isAdjectiveCluster() {
        return _isAdjectiveCluster;
    }

    public void setIsAdjectiveCluster(boolean isAdjectiveCluster) {
        _isAdjectiveCluster = isAdjectiveCluster;
    }

    /**
     * Returns all Verb Frames that are valid for all the words in this synset
     */
    public String[] getVerbFrames() {
        return VerbFrame.getFrames(_verbFrameFlags);
    }

    public BitSet getVerbFrameFlags() {
        return _verbFrameFlags;
    }

    public void setVerbFrameFlags(BitSet verbFrameFlags) {
        _verbFrameFlags = verbFrameFlags;
    }

    public int[] getVerbFrameIndices() {
        return VerbFrame.getVerbFrameIndices(_verbFrameFlags);
    }

    /**
     * Returns true if <code>lemma</code> is one of the words contained in this synset.
     */
    public boolean containsWord(String lemma) {
        for (int i = 0; i < getWordsSize(); i++) {
            if (getWord(i).getLemma().equals(lemma)) {
                return true;
            }
        }
        return false;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // set POS to reference the static instance defined in the current runtime environment
        _pos = POS.getPOSForKey(_pos.getKey());
    }

    /**
     * Gets the lexicographer file name containing this synset.
     *
     * @return two digit decimal integer
     */
    public long getLexFileNum() {
        return lexFileNum;
    }

    /**
     * Sets the lexicographer file name containing this synset.
     *
     * @param lexFileId - the lexicographer file name id
     */
    public void setLexFileNum(long lexFileId) {
        this.lexFileNum = lexFileId;
    }

    /**
     * Gets the lex file name.
     *
     * @return lex file name
     */
    public String getLexFileName() {
        return LexFileIdMap.getFileName(lexFileNum);
    }

    /**
     * Gets the sense key of a lemma. This will be refactored in 2.0 with
     * the architecture reworking.
     *
     * @param lemma lemma sense to grab
     * @return sense key for lemma
     */
    public String getSenseKey(String lemma) {
        int ss_type = 5;
        if (this.getPOS().equals(POS.NOUN)) {
            ss_type = 1;
        } else if (this.getPOS().equals(POS.VERB)) {
            ss_type = 2;
        } else if (this.getPOS().equals(POS.ADJECTIVE)) {
            ss_type = 3;
        } else if (this.getPOS().equals(POS.ADVERB)) {
            ss_type = 4;
        }

        if (isAdjectiveCluster()) {
            ss_type = 5;
        }

        //find lex id
        long lexId = -1;
        for (int i = 0; i < this.getWords().length; i++) {
            Word w = this.getWords()[i];
            if (w.getLemma().equalsIgnoreCase(lemma)) {
                lexId = w.getLexId();
            }
        }

        StringBuilder senseKey = new StringBuilder(String.format("%s%%%d:%02d:%02d:", lemma.toLowerCase(), ss_type, getLexFileNum(), lexId));

        if (ss_type == 5) {
            try {
                Pointer[] p = this.getPointers(PointerType.SIMILAR_TO);
                if (p.length > 0) {
                    Pointer headWord = p[0];
                    Word[] words = headWord.getTargetSynset().getWords();
                    if (words.length > 0) {
                        senseKey.append(String.format("%s:%02d", words[0].getLemma().toLowerCase(), words[0].getLexId()));
                    }
                }
            } catch (JWNLException e) {
                e.printStackTrace();
            }
        } else {
            senseKey.append(":");
        }

        return senseKey.toString();

    }
}