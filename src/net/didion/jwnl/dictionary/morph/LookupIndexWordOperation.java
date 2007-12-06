package net.didion.jwnl.dictionary.morph;

import net.didion.jwnl.data.POS;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;

import java.util.Map;

public class LookupIndexWordOperation implements Operation {
	public Object create(Map params) throws JWNLException {
		return new LookupIndexWordOperation();
	}

	public boolean execute(POS pos, String lemma, BaseFormSet baseForms) throws JWNLException {
		if (Dictionary.getInstance().getIndexWord(pos, lemma) != null) {
			baseForms.add(lemma);
			return true;
		}
		return false;
	}
}