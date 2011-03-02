package net.didion.jwnl.dictionary.morph;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.Exc;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.util.factory.Param;

import java.util.Map;

/**
 * Lookup the word in the exceptions file of the given part-of-speech.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class LookupExceptionsOperation extends AbstractOperation {

    public LookupExceptionsOperation(Dictionary dictionary, Map<String, Param> params) throws JWNLException {
        super(dictionary);
    }

    public boolean execute(POS pos, String derivation, BaseFormSet form) throws JWNLException {
        Exc exc = dictionary.getException(pos, derivation);
        if (null != exc) {
            for (String exception : exc.getExceptions()) {
                form.add(exception);
            }
            return true;
        }
        return false;
    }
}