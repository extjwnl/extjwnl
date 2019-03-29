package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.ResourceBundleSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A <code>Word</code> represents the lexical information related to a specific sense of an <code>IndexWord</code>.
 * <code>Word</code>'s are linked by {@link Pointer}s into a network of lexically related words.
 * {@link #getTargets getTargets} retrieves the targets of these links, and
 * {@link Word#getPointers getPointers} retrieves the pointers themselves.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Word extends PointerTarget {

    private static final long serialVersionUID = 5L;

    /**
     * The Synset to which this word belongs.
     */
    private Synset synset;

    /**
     * The string representation of the word.
     */
    private String lemma;

    /**
     * The integer that, when appended onto lemma, uniquely identifies a sense within a lexicographer file.
     * lex_id numbers usually start with 00, and are incremented as additional senses of the word are added
     * to the same file, although there is no requirement that the numbers be consecutive or begin with 00.
     * Note that a value of 00 is the default.
     */
    private int lexId = -1;//flag as not set

    /**
     * The number of times each tagged sense occurs in a semantic concordance.
     */
    private int useCount;

    /**
     * Constructs a word tied to a synset, it's position within the synset, and the lemma.
     *
     * @param dictionary owner
     * @param synset     the synset this word is contained in
     * @param lemma      the lemma of this word
     */
    public Word(Dictionary dictionary, Synset synset, String lemma) {
        super(dictionary);
        if (null == synset) {
            if (null != dictionary) {
                throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_042"));
            } else {
                throw new IllegalArgumentException("Synset must be not null");
            }
        }
        if (synset.getDictionary() != dictionary) {
            if (null != dictionary) {
                throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_040"));
            } else {
                throw new IllegalArgumentException("Dictionary element must belong to this dictionary");
            }
        }
        this.synset = synset;
        if (null == lemma || "".equals(lemma)) {
            if (null != dictionary) {
                throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_046"));
            } else {
                throw new IllegalArgumentException("Lemma must be not null and not empty");
            }
        }
        if (' ' == lemma.charAt(0) || ' ' == lemma.charAt(lemma.length() - 1)) {
            if (null != dictionary) {
                throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_055"));
            } else {
                throw new IllegalArgumentException("Lemma should not be surrounded by spaces");
            }
        }
        this.lemma = lemma;
    }

    /**
     * Two words are equal if their parent Synsets are equal and they have the same lemma
     */
    public boolean equals(Object object) {
        return (object instanceof Word)
                && ((Word) object).getSynset().equals(getSynset())
                && ((Word) object).getLemma().equals(getLemma());
    }

    public int hashCode() {
        return getSynset().hashCode() ^ getLemma().hashCode();
    }

    public String toString() {
        return ResourceBundleSet.insertParams("[Word: {0} [Lemma: {1}] {2} [Index: {3}]]", new Object[]{getPOS(), getLemma(), getSynset(), getIndex()});
    }

    /**
     * Returns the lexicographer id that identifies this lemma
     *
     * @return the lexicographer id that identifies this lemma
     */
    public int getLexId() {
        return lexId;
    }

    /**
     * Sets the lexicographer id that identifies this lemma.
     *
     * @param lexId the lexicographer id that identifies this lemma
     */
    public void setLexId(int lexId) {
        this.lexId = lexId;
    }

    /**
     * Returns the synset associated with this word.
     *
     * @return the synset associated with this word
     */
    public Synset getSynset() {
        return synset;
    }

    /**
     * Returns the part of speech of this word.
     *
     * @return the part of speech
     */
    public POS getPOS() {
        return synset.getPOS();
    }

    /**
     * Returns the number of this word (1-based) or 0 if not found.
     * NB Word numbers are assigned to the word fields in a synset, from left to right, beginning with 1.
     *
     * @return the number of this word
     */
    @Override
    public int getIndex() {
        int result = 0;
        if (null != synset) {
            result = synset.getWords().indexOf(this) + 1;
        }
        return result;
    }

    /**
     * Returns the lemma of this word.
     *
     * @return the lemma of this word
     */
    public String getLemma() {
        return lemma;
    }

    /**
     * Returns the number of times each tagged sense occurs in a semantic concordance.
     *
     * @return the number of times each tagged sense occurs in a semantic concordance
     */
    public int getUseCount() {
        return useCount;
    }

    /**
     * Sets the number of times each tagged sense occurs in a semantic concordance.
     *
     * @param useCount number of times each tagged sense occurs in a semantic concordance
     */
    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    /**
     * Returns all the pointers of the synset that contains this word whose source is this word.
     */
    public List<Pointer> getPointers() {
        List<Pointer> result = new ArrayList<>(0);
        for (Pointer pointer : getSynset().getPointers()) {
            if (this.equals(pointer.getSource())) {
                result.add(pointer);
            }
        }
        return result;
    }

    /**
     * Returns the sense key of a lemma.
     *
     * @return sense key
	 * @throws JWNLException JWNLException
     */
    public String getSenseKey() throws JWNLException {
        int ss_type = getSynsetType();

        final StringBuilder senseKey = startBuildingSenseKey(ss_type);

        if (POS.ADJECTIVE_SATELLITE_ID == ss_type) {
            List<Pointer> p = synset.getPointers(PointerType.SIMILAR_TO);
            if (0 < p.size()) {
                Pointer headWord = p.get(0);
                List<Word> words = headWord.getTargetSynset().getWords();
                if (0 < words.size()) {
                    Word word = words.get(0);
                    senseKey.append(word.getLemma().toLowerCase().replace(' ', '_')).append(":");
                    if (word.getLexId() < 10) {
                        senseKey.append("0");
                    }
                    senseKey.append(word.getLexId());
                }
            }
        } else {
            senseKey.append(":");
        }

        return senseKey.toString();
    }

    /**
     * Returns the sense key of a lemma, taking into account adjective class (position).
     *
     * @return sense key
	 * @throws JWNLException JWNLException
     */
    public String getSenseKeyWithAdjClass() throws JWNLException {
        int ss_type = getSynsetType();

        final StringBuilder senseKey = startBuildingSenseKey(ss_type);

        if (POS.ADJECTIVE_SATELLITE_ID == ss_type) {
            List<Pointer> p = synset.getPointers(PointerType.SIMILAR_TO);
            if (0 < p.size()) {
                Pointer headWord = p.get(0);
                List<Word> words = headWord.getTargetSynset().getWords();
                if (0 < words.size()) {
                    Word word = words.get(0);
                    String lemma = word.getLemma().toLowerCase().replace(' ', '_');
                    if (word instanceof Adjective) {
                        Adjective a = (Adjective) word;
                        if (AdjectivePosition.NONE != a.getAdjectivePosition()) {
                            lemma = lemma + "(" + a.getAdjectivePosition().getKey() + ")";
                        }
                    }
                    senseKey.append(lemma).append(":");
                    if (word.getLexId() < 10) {
                        senseKey.append("0");
                    }
                    senseKey.append(word.getLexId());
                }
            }
        } else {
            senseKey.append(":");
        }

        return senseKey.toString();
    }

    /**
     * Returns sense number of this word (1-based) or 0 if not found.
     * sense_number is a decimal integer indicating the sense number of the word,
     * within the part of speech encoded in sense_key, in the WordNet database.
     *
     * @return sense number or 0 if not found
     * @throws JWNLException JWNLException
     */
    public int getSenseNumber() throws JWNLException {
        int result = 0;
        if (null != dictionary) {
            IndexWord indexWord = dictionary.getIndexWord(getPOS(), lemma);
            if (null != indexWord) {
                result = indexWord.getSenses().indexOf(synset) + 1;
            }
        }
        return result;
    }

    protected int getSynsetType() {
        int ss_type = getPOS().getId();
        if (POS.ADJECTIVE == getSynset().getPOS() && getSynset().isAdjectiveCluster()) {
            ss_type = POS.ADJECTIVE_SATELLITE_ID;
        }
        return ss_type;
    }

    protected StringBuilder startBuildingSenseKey(final int ss_type) {
        StringBuilder senseKey = new StringBuilder(lemma.toLowerCase().replace(' ', '_'));
        senseKey.append("%").append(ss_type).append(":");
        if (synset.getLexFileNum() < 10) {
            senseKey.append("0");
        }
        senseKey.append(synset.getLexFileNum()).append(":");
        if (lexId < 10) {
            senseKey.append("0");
        }
        senseKey.append(lexId).append(":");
        return senseKey;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        dictionary = Dictionary.getRestoreDictionary();
    }
}
