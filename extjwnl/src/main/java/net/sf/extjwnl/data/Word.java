package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.IOException;
import java.util.*;

/**
 * A <code>Word</code> represents the lexical information related to a specific sense of an <code>IndexWord</code>.
 * <code>Word</code>'s are linked by {@link Pointer}s into a network of lexically related words.
 * {@link #getTargets getTargets} retrieves the targets of these links, and
 * {@link Word#getPointers getPointers} retrieves the pointers themselves.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Word extends PointerTarget {

    private static final long serialVersionUID = 4L;

    /**
     * The Synset to which this word belongs.
     */
    private Synset synset;

    /**
     * This word's index within the synset.
     * NB Word numbers are assigned to the word fields in a synset, from left to right, beginning with 1
     */
    private int index;

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
     * The summary of this word.
     */
    private String summary;

    /**
     * The number of times each tagged sense occurs in a semantic concordance.
     */
    private int useCount;

    private static final SenseWordCountComparator swcComp = new SenseWordCountComparator();
    private static final StringLengthComparator slComp = new StringLengthComparator();

    /**
     * Comparator to sort synsets according to word count.
     */
    private static class SenseWordCountComparator implements Comparator<Synset> {
        public int compare(Synset o1, Synset o2) {
            return o1.getWords().size() - o2.getWords().size();
        }
    }

    /**
     * Comparator to sort strings by length.
     */
    private static class StringLengthComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            return o1.length() - o2.length();
        }
    }

    /**
     * Constructs a word tied to a synset, it's position within the synset, and the lemma.
     *
     * @param dictionary owner
     * @param synset     the synset this word is contained in
     * @param index      the position of the word in the synset (usage)
     * @param lemma      the lemma of this word
     */
    public Word(Dictionary dictionary, Synset synset, int index, String lemma) {
        super(dictionary);
        if (null == synset) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_042"));
        }
        this.synset = synset;
        if (index < 1) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_045"));
        }
        this.index = index;
        if (null == lemma || "".equals(lemma)) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_046"));
        }
        if (' ' == lemma.charAt(0) || ' ' == lemma.charAt(lemma.length() - 1)) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_055"));
        }
        this.lemma = lemma.trim();
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
        return JWNL.resolveMessage("DATA_TOSTRING_005", new Object[]{getPOS(), getLemma(), getSynset(), getIndex()});
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
     * Returns the index of this word.
     * NB Word numbers are assigned to the word fields in a synset, from left to right, beginning with 1.
     *
     * @return the index of this word
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index of this word.
     * NB Word numbers are assigned to the word fields in a synset, from left to right, beginning with 1.
     *
     * @param index the index of this word
     */
    public void setIndex(int index) {
        this.index = index;
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
        List<Pointer> result = new ArrayList<Pointer>(0);
        for (Pointer pointer : getSynset().getPointers()) {
            if (this.equals(pointer.getSource())) {
                result.add(pointer);
            }
        }
        return result;
    }

    /**
     * Returns the summary calculated among all senses.
     *
     * @return summary
     * @throws JWNLException JWNLException
     */
    public String getSummary() throws JWNLException {
        if (null == summary && null != dictionary) {
            //across all POS
            //IndexWordSet iws = Dictionary.getInstance().lookupAllIndexWords(getLemma());
            //across current POS
            IndexWord iw = dictionary.getIndexWord(getSynset().getPOS(), getLemma());
            summary = getSummary(iw.getSenses());
        }
        return summary;
    }

    /**
     * Returns the summary, calculated among given set of senses. Useful during disambiguation when some senses
     * are already pruned.
     *
     * @param senses set of senses
     * @return summary
     * @throws JWNLException JWNLException
     */
    public String getSummary(List<Synset> senses) throws JWNLException {
        List<Synset> sortedSenses = new ArrayList<Synset>(senses);
        HashSet<String> summaries = new HashSet<String>(sortedSenses.size() + 2);
        //do not distinguish case, because it does not help. "ab" results in "AB", and "blood_type" is better
        String lcLemma = getLemma().toLowerCase();
        summaries.add("");
        summaries.add(lcLemma);
        //sort to put synsets with few words first
        Collections.sort(sortedSenses, swcComp);

        for (Synset ss : sortedSenses) {
            String summary = "";
            String lastSummary;
            if (POS.NOUN == getSynset().getPOS()) {
                //try INSTANCE_OF first of all, because for people and places it is better
                //to show class, rather than another name.
                List<Pointer> pointers = ss.getPointers(PointerType.INSTANCE_HYPERNYM);
                summary = getShortestLemma(getLemmasFromPointers(pointers), summaries);
            }

            if (summaries.contains(summary)) {
                //look in SYNSET, except for short abbreviations
                if ((1 < ss.getWords().size()) && (2 < getLemma().length())) {
                    summary = getShortestLemma(getLemmasFromSynset(ss), summaries);
                }
                if (summaries.contains(summary)) {
                    if (POS.NOUN == getSynset().getPOS() || POS.VERB == getSynset().getPOS()) {
                        //try HYPERNYMS
                        List<Pointer> pointers = ss.getPointers(PointerType.HYPERNYM);
                        summary = getShortestLemma(getLemmasFromPointers(pointers), summaries);
                        lastSummary = summary;
                        if ((0 == pointers.size()) || summaries.contains(summary)) {
                            //try HYPONYMS
                            pointers = ss.getPointers(PointerType.HYPONYM);
                            summary = getShortestLemma(getLemmasFromPointers(pointers), summaries);
                            lastSummary = summary;
                            if ((0 == pointers.size()) || summaries.contains(summary)) {
                                //SYNSET again
                                summary = getShortestLemma(getLemmasFromSynset(ss), new HashSet<String>(Arrays.asList(lcLemma)));
                                if (0 < summary.length()) {
                                    lastSummary = summary;
                                } else {
                                    summary = lastSummary;
                                }
                                //try gloss for verbs
                                //for nouns glosses are written in a manner that requires parsing to extract good summary
                                if (0 == summary.length()) {
                                    if (POS.VERB == getSynset().getPOS()) {
                                        String gloss = ss.getGloss();
                                        summary = gloss.substring(0, gloss.indexOf(" "));
                                    } else {
                                        summary = lastSummary;
                                    }
                                } else {
                                    summary = lastSummary;
                                }
                            } else {
                                summary = lastSummary;
                            }
                        } else {
                            summary = lastSummary;
                        }
                    } else {//NV
                        //AR
                        if (POS.ADJECTIVE == getSynset().getPOS()) {
                            //try SIMILAR_TO
                            List<Pointer> pointers = ss.getPointers(PointerType.SIMILAR_TO);
                            summary = getShortestLemma(getLemmasFromPointers(pointers), summaries);
                            lastSummary = summary;
                            if ((0 == pointers.size()) || summaries.contains(summary)) {
                                //try PERTAINYM
                                pointers = ss.getPointers(PointerType.PERTAINYM);
                                summary = getShortestLemma(getLemmasFromPointers(pointers), summaries);
                                lastSummary = summary;
                                if ((0 == pointers.size()) || summaries.contains(summary)) {
                                    //try SEE_ALSO
                                    pointers = ss.getPointers(PointerType.SEE_ALSO);
                                    summary = getShortestLemma(getLemmasFromPointers(pointers), summaries);
                                    lastSummary = summary;
                                    if ((0 == pointers.size()) || summaries.contains(summary)) {
                                        //try ANTONYM
                                        pointers = ss.getPointers(PointerType.ANTONYM);
                                        summary = "not " + getShortestLemma(getLemmasFromPointers(pointers), summaries);
                                        lastSummary = summary;
                                        if ((0 == pointers.size()) || summaries.contains(summary)) {
                                            //synset again or gloss
                                            summary = getShortestLemma(getLemmasFromSynset(ss), new HashSet<String>(Arrays.asList(lcLemma)));
                                            if (0 < summary.length()) {
                                                lastSummary = summary;
                                            } else {
                                                summary = lastSummary;
                                            }
                                            //try first word of the gloss
                                            if (0 == summary.length()) {
                                                String gloss = ss.getGloss();
                                                summary = gloss.substring(0, gloss.indexOf(" "));
                                            } else {
                                                summary = lastSummary;
                                            }
                                        }
                                    } else {
                                        summary = lastSummary;
                                    }
                                } else {
                                    summary = lastSummary;
                                }
                            } else {
                                summary = lastSummary;
                            }
                        } else if (POS.ADVERB == getSynset().getPOS()) {
                            //try PERTAINYM (former DERIVED)
                            List<Pointer> pointers = ss.getPointers(PointerType.PERTAINYM);
                            summary = getShortestLemma(getLemmasFromPointers(pointers), summaries);
                            lastSummary = summary;
                            if ((0 == pointers.size()) || summaries.contains(summary)) {
                                //try ANTONYM
                                pointers = ss.getPointers(PointerType.ANTONYM);
                                summary = "not " + getShortestLemma(getLemmasFromPointers(pointers), summaries);
                                lastSummary = summary;
                                if ((0 == pointers.size()) || summaries.contains(summary)) {
                                    //synset again
                                    summary = getShortestLemma(getLemmasFromSynset(ss), new HashSet<String>(Arrays.asList(lcLemma)));
                                    if (0 == summary.length()) {
                                        summary = lastSummary;
                                    }
                                } else {
                                    summary = lastSummary;
                                }
                            } else {
                                summary = lastSummary;
                            }
                        }
                    }
                }
            }

            //to track and avoid repetitions
            summaries.add(summary.toLowerCase());

            for (Word w : ss.getWords()) {
                if (w.getLemma().equals(getLemma())) {
                    w.setSummary(summary);
                    break;
                }
            }

        }//for senses

        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    private static ArrayList<String> getLemmasFromSynset(Synset s) {
        ArrayList<String> result = new ArrayList<String>();

        for (Word w : s.getWords()) {
            result.add(w.getLemma());
        }

        return result;
    }

    private static ArrayList<String> getLemmasFromPointers(List<Pointer> pointers) throws JWNLException {
        ArrayList<String> result = new ArrayList<String>();

        for (Pointer ptr : pointers) {
            result.addAll(getLemmasFromSynset(ptr.getTargetSynset()));
        }

        return result;
    }

    private static String getShortestLemma(ArrayList<String> candidates, Set<String> exceptions) {
        //sort by length
        //shortest may not be the best, better choose among first ones - they are better known.
        Collections.sort(candidates, slComp);

        String shortest = "";
        if (0 < candidates.size()) {
            shortest = candidates.get(0);
            while ((0 < candidates.size()) && exceptions.contains(candidates.get(0).toLowerCase())) {
                candidates.remove(0);
            }

            if (0 < candidates.size()) {
                shortest = candidates.get(0);
            }
        }

        return shortest;
    }

    /**
     * Returns the sense key of a lemma.
     *
     * @return sense key
     */
    public String getSenseKey() {
        int ss_type = getPOS().getId();
        if (POS.ADJECTIVE == getSynset().getPOS() && getSynset().isAdjectiveCluster()) {
            ss_type = POS.ADJECTIVE_SATELLITE_ID;
        }

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

        if (5 == ss_type) {
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
     */
    public String getSenseKeyWithAdjClass() {
        int ss_type = getPOS().getId();
        if (POS.ADJECTIVE == getSynset().getPOS() && getSynset().isAdjectiveCluster()) {
            ss_type = POS.ADJECTIVE_SATELLITE_ID;
        }

        StringBuilder senseKey = new StringBuilder(String.format("%s%%%d:%02d:%02d:", lemma.toLowerCase().replace(' ', '_'), ss_type, synset.getLexFileNum(), lexId));

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
                    senseKey.append(String.format("%s:%02d", lemma, word.getLexId()));
                }
            }
        } else {
            senseKey.append(":");
        }

        return senseKey.toString();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        dictionary = Dictionary.getRestoreDictionary();
    }
}