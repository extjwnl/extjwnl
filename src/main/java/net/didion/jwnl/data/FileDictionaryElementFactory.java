package net.didion.jwnl.data;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.util.factory.Owned;

/**
 * Factory class for creating <code>DictionaryElement</code>s (<code>Synset</code>s, <code>Exception</codes,
 * and <code>IndexWord</code>s). Using a factory class rather than individual parsing methods in each class
 * facilitates using multiple versions of WordNet, or using a proprietary data format.
 */
public interface FileDictionaryElementFactory extends Owned {

    /**
     * Create an Exc from a line in an exception file.
     *
     * @param pos  - the part of speech
     * @param line - unparsed line
     * @return exception
     */
    public Exc createExc(POS pos, String line) throws JWNLException;

    /**
     * Creates a synset from a line in a data file.
     *
     * @param pos  - the part of speech
     * @param line - unparsed line
     * @return synset
     */
    public Synset createSynset(POS pos, String line) throws JWNLException;

    /**
     * Creates an IndexWord from a line in an index file.
     *
     * @param pos  - the part of speech
     * @param line - unparsed line
     * @return indexword
     */
    public IndexWord createIndexWord(POS pos, String line) throws JWNLException;

}