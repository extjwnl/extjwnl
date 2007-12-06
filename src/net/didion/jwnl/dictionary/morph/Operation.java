package net.didion.jwnl.dictionary.morph;

import net.didion.jwnl.data.POS;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.util.factory.Createable;

public interface Operation extends Createable {
	/**
	 * Execute the operation.
	 * @param pos
	 * @param lemma
	 * @param baseForms BaseFormSet to which all discovered base forms should
	 *        be added.
	 * @return true if at least one base form was discovered by the operation and
	 *         added to <var>baseForms</var>.
	 * @throws JWNLException
	 */
	boolean execute(POS pos, String lemma, BaseFormSet baseForms) throws JWNLException;
}