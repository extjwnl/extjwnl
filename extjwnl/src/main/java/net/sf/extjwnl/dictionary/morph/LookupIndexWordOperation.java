package net.sf.extjwnl.dictionary.morph;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.Map;

/**
 * Lookups up index words.
 *
 * @author John Didion <jdidion@users.sourceforge.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public class LookupIndexWordOperation extends AbstractOperation {

    public LookupIndexWordOperation(Dictionary dictionary, Map params) {
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