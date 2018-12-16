package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;

/**
 * Factory class for creating <code>DictionaryElement</code>s (<code>Synset</code>s, <code>Exception</code>s,
 * and <code>IndexWord</code>s). Using a factory class rather than individual parsing methods in each class
 * facilitates using multiple versions of WordNet, or using a proprietary data format.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface FileDictionaryElementFactory extends DictionaryElementFactory {

    /**
     * Creates an Exc from a line in an exception file.
     *
     * @param pos  - the part of speech
     * @param line - unparsed line
     * @return exception
     * @throws JWNLException JWNLException
     */
    Exc createExc(POS pos, CharSequence line) throws JWNLException;

    /**
     * Creates a synset from a line in a data file.
     *
     * @param pos  - the part of speech
     * @param line - unparsed line
     * @return synset
     * @throws JWNLException JWNLException
     */
    Synset createSynset(POS pos, CharSequence line) throws JWNLException;

    /**
     * Creates an IndexWord from a line in an index file.
     *
     * @param pos  - the part of speech
     * @param line - unparsed line
     * @return indexword
     * @throws JWNLException JWNLException
     */
    IndexWord createIndexWord(POS pos, CharSequence line) throws JWNLException;
}