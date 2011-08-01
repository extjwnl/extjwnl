package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.util.factory.Owned;

/**
 * Factory class for creating <code>DictionaryElement</code>s (<code>Synset</code>s, <code>Exception</codes,
 * and <code>IndexWord</code>s). Using a factory class rather than individual parsing methods in each class
 * facilitates using multiple versions of WordNet, or using a proprietary data format.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface FileDictionaryElementFactory extends Owned {

    /**
     * Create an Exc from a line in an exception file.
     *
     * @param pos  - the part of speech
     * @param line - unparsed line
     * @return exception
     * @throws JWNLException JWNLException
     */
    Exc createExc(POS pos, String line) throws JWNLException;

    /**
     * Creates a synset from a line in a data file.
     *
     * @param pos  - the part of speech
     * @param line - unparsed line
     * @return synset
     * @throws JWNLException JWNLException
     */
    Synset createSynset(POS pos, String line) throws JWNLException;

    /**
     * Creates an IndexWord from a line in an index file.
     *
     * @param pos  - the part of speech
     * @param line - unparsed line
     * @return indexword
     * @throws JWNLException JWNLException
     */
    IndexWord createIndexWord(POS pos, String line) throws JWNLException;

}