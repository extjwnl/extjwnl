package net.didion.jwnl.data;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;

import java.util.*;

/**
 * A <code>Word</code> represents the lexical information related to a specific sense of an <code>IndexWord</code>.
 * <code>Word</code>'s are linked by {@link Pointer}s into a network of lexically related words.
 * {@link #getTargets getTargets} retrieves the targets of these links, and
 * {@link Word#getPointers getPointers} retrieves the pointers themselves.
 */
public class Word extends PointerTarget {
    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 8591237840924027785L;

    /**
     * The Synset to which this word belongs.
     */
    private Synset _synset;

    /**
     * This word's index within the synset.
     * NB Word numbers are assigned to the word fields in a synset, from left to right, beginning with 1
     */
    private int _index;

    /**
     * The string representation of the word.
     */

    private String _lemma;

    /**
     * The lexicographer id that identifies this lemma.
     */
    protected long lexId;

    /**
     * The summary of this word.
     */
    protected String _summary;


    public long getLexId() {
        return lexId;
    }


    public void setLexId(long lexId) {
        this.lexId = lexId;
    }


    /**
     * Constructs a word tied to a synset, it's position within the synset, and the lemma.
     *
     * @param synset - the synset this word is contained in
     * @param index  - the position of the word in the synset (usage)
     * @param lemma  - the lemma of this word
     */
    public Word(Synset synset, int index, String lemma) {
        _synset = synset;
        _index = index;
        _lemma = lemma;
    }


    // Object methods

    /**
     * Two words are equal if their parent Synsets are equal and they have the same index
     */
    public boolean equals(Object object) {
        return (object instanceof Word)
                && ((Word) object).getSynset().equals(getSynset())
                && ((Word) object).getIndex() == getIndex();
    }

    public int hashCode() {
        return getSynset().hashCode() ^ getIndex();
    }

    public String toString() {
        Object[] params = new Object[]{getPOS(), getLemma(), getSynset(), getIndex()};
        return JWNL.resolveMessage("DATA_TOSTRING_005", params);
    }

    // Accessors

    /**
     * Gets the synset associated with this word.
     *
     * @return synset
     */
    public Synset getSynset() {
        return _synset;
    }

    /**
     * Gets the part of speech of this word.
     *
     * @return part of speech
     */
    public POS getPOS() {
        return _synset.getPOS();
    }

    /**
     * Gets the index of this word.
     * NB Word numbers are assigned to the word fields in a synset, from left to right, beginning with 1
     *
     * @return index
     */
    public int getIndex() {
        return _index;
    }

    /**
     * Gets the lemma of this word.
     *
     * @return lemma
     */
    public String getLemma() {
        return _lemma;
    }

    /**
     * Returns all the pointers of the synset that contains this word whose source is this word.
     */
    public Pointer[] getPointers() {
        Pointer[] source = getSynset().getPointers();
        List<Pointer> list = new ArrayList<Pointer>(source.length);
        for (Pointer pointer : source) {
            if (this.equals(pointer.getSource())) {
                list.add(pointer);
            }
        }
        return list.toArray(new Pointer[list.size()]);
    }

    /**
     * Comparator to sort synsets according to word count.
     */
    private static class SenseWordCountComparator implements Comparator<Synset> {
        public int compare(Synset o1, Synset o2) {
            return o1.getWordsSize() - o2.getWordsSize();
        }
    }

    private static final SenseWordCountComparator _swcComp = new SenseWordCountComparator();

    /**
     * Comparator to sort strings by length.
     */
    private static class StringLengthComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            return o1.length() - o2.length();
        }
    }

    private static final StringLengthComparator _slComp = new StringLengthComparator();

    /**
     * Returns the summary calculated among all senses.
     *
     * @return summary
     * @throws JWNLException JWNLException
     */
    public String getSummary() throws JWNLException {
        if (null == _summary) {
            //across all POS
            //IndexWordSet iws = Dictionary.getInstance().lookupAllIndexWords(getLemma());
            //across current POS
            IndexWord iw = Dictionary.getInstance().getIndexWord(getSynset().getPOS(), getLemma());
            _summary = getSummary(Arrays.asList(iw.getSenses()));
        }
        return _summary;
    }

    private static ArrayList<String> getLemmasFromSynset(Synset s) throws JWNLException {
        ArrayList<String> result = new ArrayList<String>();

        for (Word w : s.getWords()) {
            result.add(w.getLemma().replaceAll("_", " "));
        }

        return result;
    }


    private static ArrayList<String> getLemmasFromPointers(Pointer[] pointers) throws JWNLException {
        ArrayList<String> result = new ArrayList<String>();

        for (Pointer ptr : pointers) {
            result.addAll(getLemmasFromSynset(ptr.getTargetSynset()));
        }

        return result;
    }

    private static String getShortestLemma(ArrayList<String> candidates, Set<String> exceptions) throws JWNLException {
        //sort by length
        //shortest may not be the best, better choose among first ones - they are better known.
        Collections.sort(candidates, _slComp);

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
        Collections.sort(sortedSenses, _swcComp);

        for (Synset ss : sortedSenses) {
            String summary = "";
            String lastSummary;
            if (POS.NOUN.equals(getSynset().getPOS())) {
                //try INSTANCE_OF first of all, because for people and places it is better
                //to show class, rather than another name.
                Pointer[] pointers = ss.getPointers(PointerType.INSTANCE_HYPERNYM);
                summary = getShortestLemma(getLemmasFromPointers(pointers), summaries);
            }

            if (summaries.contains(summary)) {
                //look in SYNSET, except for short abbreviations
                if ((1 < ss.getWordsSize()) && (2 < getLemma().length())) {
                    summary = getShortestLemma(getLemmasFromSynset(ss), summaries);
                }
                if (summaries.contains(summary)) {
                    if (POS.NOUN.equals(getSynset().getPOS()) || POS.VERB.equals(getSynset().getPOS())) {
                        //try HYPERNYMS
                        Pointer[] pointers = ss.getPointers(PointerType.HYPERNYM);
                        summary = getShortestLemma(getLemmasFromPointers(pointers), summaries);
                        lastSummary = summary;
                        if ((0 == pointers.length) || summaries.contains(summary)) {
                            //try HYPONYMS
                            pointers = ss.getPointers(PointerType.HYPONYM);
                            summary = getShortestLemma(getLemmasFromPointers(pointers), summaries);
                            lastSummary = summary;
                            if ((0 == pointers.length) || summaries.contains(summary)) {
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
                                    if (POS.VERB.equals(getSynset().getPOS())) {
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
                        if (POS.ADJECTIVE.equals(getSynset().getPOS())) {
                            //try SIMILAR_TO
                            Pointer[] pointers = ss.getPointers(PointerType.SIMILAR_TO);
                            summary = getShortestLemma(getLemmasFromPointers(pointers), summaries);
                            lastSummary = summary;
                            if ((0 == pointers.length) || summaries.contains(summary)) {
                                //try PERTAINYM
                                pointers = ss.getPointers(PointerType.PERTAINYM);
                                summary = getShortestLemma(getLemmasFromPointers(pointers), summaries);
                                lastSummary = summary;
                                if ((0 == pointers.length) || summaries.contains(summary)) {
                                    //try SEE_ALSO
                                    pointers = ss.getPointers(PointerType.SEE_ALSO);
                                    summary = getShortestLemma(getLemmasFromPointers(pointers), summaries);
                                    lastSummary = summary;
                                    if ((0 == pointers.length) || summaries.contains(summary)) {
                                        //try ANTONYM
                                        pointers = ss.getPointers(PointerType.ANTONYM);
                                        summary = "not " + getShortestLemma(getLemmasFromPointers(pointers), summaries);
                                        lastSummary = summary;
                                        if ((0 == pointers.length) || summaries.contains(summary)) {
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
                        } else if (POS.ADVERB.equals(getSynset().getPOS())) {
                            //try DERIVED
                            Pointer[] pointers = ss.getPointers(PointerType.DERIVED);
                            summary = getShortestLemma(getLemmasFromPointers(pointers), summaries);
                            lastSummary = summary;
                            if ((0 == pointers.length) || summaries.contains(summary)) {
                                //try ANTONYM
                                pointers = ss.getPointers(PointerType.ANTONYM);
                                summary = "not " + getShortestLemma(getLemmasFromPointers(pointers), summaries);
                                lastSummary = summary;
                                if ((0 == pointers.length) || summaries.contains(summary)) {
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

        return _summary;
    }

    public void setSummary(String _summary) {
        this._summary = _summary;
    }
}