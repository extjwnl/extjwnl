package net.didion.jwnl.dictionary.morph;

import net.didion.jwnl.data.POS;
import net.didion.jwnl.JWNLException;

import java.util.Map;

/** yet to be implemented */
public class RemovePrepPhrasesOperation implements Operation {
	public Object create(Map params) throws JWNLException {
		return new RemovePrepPhrasesOperation();
	}

	public boolean execute(POS pos, String lemma, BaseFormSet baseForm) {
		return false;
	}
}