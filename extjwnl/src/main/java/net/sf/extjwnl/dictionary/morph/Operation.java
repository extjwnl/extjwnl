package net.sf.extjwnl.dictionary.morph;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.util.factory.Owned;

/**
 * Base class for morphological operations.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface Operation extends Owned {

    /**
     * Executes the operation.
     *
     * @param pos       part of speech
     * @param lemma     lemma
     * @param baseForms BaseFormSet to which all discovered base forms should
     *                  be added.
     * @return true if at least one base form was discovered by the operation and
     *         added to <var>baseForms</var>.
     * @throws JWNLException JWNLException
     */
    boolean execute(POS pos, String lemma, BaseFormSet baseForms) throws JWNLException;
}