package net.didion.jwnl.dictionary.morph;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;

import java.util.Map;

/**
 * Lookups up index words.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class LookupIndexWordOperation extends AbstractOperation {

    public LookupIndexWordOperation(Dictionary dictionary, Map params) throws JWNLException {
        super(dictionary);
    }

    public boolean execute(POS pos, String lemma, BaseFormSet baseForms) throws JWNLException {
        if (dictionary.getIndexWord(pos, lemma) != null) {
            baseForms.add(lemma);
            return true;
        }
        return false;
    }
}