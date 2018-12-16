package net.sf.extjwnl.data;

import net.sf.extjwnl.util.ResourceBundleSet;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * A class to simplify the access to a set of <code>IndexWord</code>s, each containing
 * one part of speech of the same word. IndexWordSets are usually created by a
 * call to {@link net.sf.extjwnl.dictionary.Dictionary#lookupAllIndexWords Dictionary.lookupAllIndexWords}.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class IndexWordSet {
    /**
     * Map of IndexWords in this set.
     */
    private final Map<POS, IndexWord> indexWords = new EnumMap<>(POS.class);
    private final String lemma;

    public IndexWordSet(String lemma) {
        this.lemma = lemma;
    }

    /**
     * Adds an IndexWord to this set.
     *
     * @param word word to add
     */
    public void add(IndexWord word) {
        indexWords.put(word.getPOS(), word);
    }

    /**
     * Removes the IndexWords associated with POS <var>p</var> from this set.
     *
     * @param p POS
     */
    public void remove(POS p) {
        indexWords.remove(p);
    }

    /**
     * Returns the number of IndexWords in this set.
     *
     * @return the number of IndexWords in this set
     */
    public int size() {
        return indexWords.size();
    }

    /**
     * Returns the IndexWord associated with POS <var>p</var>.
     *
     * @param p POS
     * @return the IndexWord associated with POS <var>p</var>.
     */
    public IndexWord getIndexWord(POS p) {
        return indexWords.get(p);
    }

    /**
     * Returns an array of the IndexWords in this set.
     *
     * @return an array of the IndexWords in this set
     */
    public IndexWord[] getIndexWordArray() {
        IndexWord[] words = new IndexWord[indexWords.size()];
        return indexWords.values().toArray(words);
    }

    /**
     * Returns a collection of the IndexWords in this set.
     *
     * @return a collection of the IndexWords in this set
     */
    public Collection<IndexWord> getIndexWordCollection() {
        return indexWords.values();
    }

    /**
     * Returns a set of all the parts-of-speech for which there is an
     * IndexWord in this set.
     *
     * @return a set of all the parts-of-speech for which there is an IndexWord in this set
     */
    public Set<POS> getValidPOSSet() {
        return indexWords.keySet();
    }

    /**
     * Return true if there is a word with part-of-speech <var>pos</var> in
     * this set.
     *
     * @param pos POS
     * @return true if there is a word with part-of-speech <var>pos</var> in this set.
     */
    public boolean isValidPOS(POS pos) {
        return indexWords.containsKey(pos);
    }

    /**
     * Finds out how many senses the word with part-of-speech <var>pos</var> has.
     *
     * @param pos POS
     * @return number of senses the word with part-of-speech <var>pos</var> has
     */
    public int getSenseCount(POS pos) {
        int result = 0;
        IndexWord indexWord = getIndexWord(pos);
        if (null != indexWord) {
            result = indexWord.getSenses().size();
        }
        return result;
    }

    public String toString() {
        String str;
        if (size() == 0) {
            str = "<empty>";
        } else {
            StringBuilder buf = new StringBuilder();
            for (POS o : getValidPOSSet()) {
                buf.append(getIndexWord(o).toString());
            }
            str = buf.toString();
        }
        return ResourceBundleSet.insertParams("[IndexWordSet: {0}]", new String[]{str});
    }

    public String getLemma() {
        return lemma;
    }

    /**
     * It is assumed that IndexWordSets will only be created by calling
     * {@link net.sf.extjwnl.dictionary.Dictionary#lookupAllIndexWords Dictionary.lookupAllIndexWords},
     * so all IndexWordSets with the same lemma should be equal.
     */
    @Override
    public boolean equals(Object object) {
        return (object instanceof IndexWordSet) &&
                getLemma().equals(((IndexWordSet) object).getLemma());
    }

    @Override
    public int hashCode() {
        return lemma.hashCode();
    }
}