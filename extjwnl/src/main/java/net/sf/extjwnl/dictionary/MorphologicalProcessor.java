package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;

import java.util.List;

/**
 * A <code>MorphologicalProcessor</code> tries to turn the inflected form of a word or phrase into
 * the form that can be found in WordNet. For example, if one calls
 * lookupBaseForm(POS.VERB, "running"), the index word for "run" should be returned.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface MorphologicalProcessor {
    /**
     * Try to turn <var>derivation</var> into a word that is found in the index file for <var>pos</var>.
     * If there is more than one possible base form, then the first call to this method should
     * return the first base form found. The return value for subsequent calls is undefined (it could
     * be the same base form, or the next base form - it is up to the implementer to decide, but the
     * decision should be noted.
     *
     * @param pos        part of speech
     * @param derivation derivation
     * @return index word of the base form of the derivation
     * @throws JWNLException JWNLException
     */
    IndexWord lookupBaseForm(POS pos, String derivation) throws JWNLException;

    /**
     * Return all the base forms of <var>derivation</var>.
     *
     * @param pos        part of speech
     * @param derivation derivation
     * @return all the base forms of <var>derivation</var>
     * @throws JWNLException JWNLException
     */
    List<String> lookupAllBaseForms(POS pos, String derivation) throws JWNLException;
}