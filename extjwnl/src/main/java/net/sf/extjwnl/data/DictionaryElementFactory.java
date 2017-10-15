package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.util.factory.Owned;

import java.util.List;

/**
 * Factory class for creating <code>DictionaryElement</code>s (<code>Synset</code>s, <code>Exception</code>s,
 * and <code>IndexWord</code>s). Using a factory class rather than individual parsing methods in each class
 * facilitates using multiple versions of WordNet, or using a proprietary data format.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface DictionaryElementFactory extends Owned {

    /**
     * Creates an exception in the dictionary.
     *
     * @param pos        exception part of speech
     * @param lemma      exception lemma
     * @param exceptions list of base forms
     * @return exception object
     * @throws JWNLException JWNLException
     */
    Exc createException(POS pos, String lemma, List<String> exceptions) throws JWNLException;

    /**
     * Creates synset of the specified part of speech.
     *
     * @param pos part of speech
     * @return synset object
     * @throws JWNLException JWNLException
     */
    Synset createSynset(POS pos) throws JWNLException;


    /**
     * Creates index word.
     *
     * @param pos    part of speech
     * @param lemma  lemma
     * @param synset synset
     * @return index word object
     * @throws JWNLException JWNLException
     */
    IndexWord createIndexWord(POS pos, String lemma, Synset synset) throws JWNLException;
}
