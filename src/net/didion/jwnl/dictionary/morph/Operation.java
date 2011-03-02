package net.didion.jwnl.dictionary.morph;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.util.factory.Owned;

/**
 * Base class for operations.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
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