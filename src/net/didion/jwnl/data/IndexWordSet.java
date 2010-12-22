package net.didion.jwnl.data;

import net.didion.jwnl.JWNL;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * A class to simplify the access to a set of <code>IndexWord</code>s, each containing
 * one part of speech of the same word. IndexWordSets are usually created by a
 * call to {@link net.didion.jwnl.dictionary.Dictionary#lookupAllIndexWords Dictionary.lookupAllIndexWords}.
 */
public class IndexWordSet {
    /**
     * Map of IndexWords in this set.
     */
    private Map<POS, IndexWord> _indexWords = new Hashtable<POS, IndexWord>(4, (float) 1.0);
    private String _lemma;

    public IndexWordSet(String lemma) {
        _lemma = lemma;
    }

    /**
     * Adds an IndexWord to this set.
     *
     * @param word word to add
     */
    public void add(IndexWord word) {
        _indexWords.put(word.getPOS(), word);
    }

    /**
     * Removes the IndexWords associated with POS <code>p</code> from this set.
     *
     * @param p POS
     */
    public void remove(POS p) {
        _indexWords.remove(p);
    }

    /**
     * Gets the number of IndexWords in this set.
     *
     * @return the number of IndexWords in this set
     */
    public int size() {
        return _indexWords.size();
    }

    /**
     * Gets the IndexWord associated with POS <code>p</code>.
     *
     * @param p POS
     * @return the IndexWord associated with POS <code>p</code>.
     */
    public IndexWord getIndexWord(POS p) {
        return _indexWords.get(p);
    }

    /**
     * Gets an array of the IndexWords in this set.
     *
     * @return an array of the IndexWords in this set
     */
    public IndexWord[] getIndexWordArray() {
        IndexWord[] words = new IndexWord[_indexWords.size()];
        return _indexWords.values().toArray(words);
    }

    /**
     * Gets a collection of the IndexWords in this set.
     *
     * @return a collection of the IndexWords in this set
     */
    public Collection getIndexWordCollection() {
        return _indexWords.values();
    }

    /**
     * Gets a set of all the parts-of-speech for which there is an
     * IndexWord in this set.
     *
     * @return a set of all the parts-of-speech for which there is an IndexWord in this set
     */
    public Set getValidPOSSet() {
        return _indexWords.keySet();
    }

    /**
     * Return true if there is a word with part-of-speech <code>pos</code> in
     * this set.
     *
     * @param pos POS
     * @return true if there is a word with part-of-speech <code>pos</code> in this set.
     */
    public boolean isValidPOS(POS pos) {
        return _indexWords.containsKey(pos);
    }

    /**
     * Finds out how many senses the word with part-of-speech <code>pos</code> has.
     *
     * @param pos POS
     * @return number of senses the word with part-of-speech <code>pos</code> has
     */
    public int getSenseCount(POS pos) {
        return getIndexWord(pos).getSenseCount();
    }

    public String toString() {
        String str;
        if (size() == 0) {
            str = JWNL.resolveMessage("DATA_TOSTRING_003");
        } else {
            StringBuffer buf = new StringBuffer();
            for (Object o : getValidPOSSet()) {
                buf.append(getIndexWord((POS) o).toString());
            }
            str = buf.toString();
        }
        return JWNL.resolveMessage("DATA_TOSTRING_004", str);
    }

    public String getLemma() {
        return _lemma;
    }

    /**
     * It is assumed that IndexWordSets will only be created by calling
     * {@link net.didion.jwnl.dictionary.Dictionary#lookupAllIndexWords Dictionary.lookupAllIndexWords},
     * so all IndexWordSets with the same lemma should be equal.
     */
    public boolean equals(Object object) {
        return (object instanceof IndexWordSet) &&
                getLemma().equals(((IndexWordSet) object).getLemma());
    }
}