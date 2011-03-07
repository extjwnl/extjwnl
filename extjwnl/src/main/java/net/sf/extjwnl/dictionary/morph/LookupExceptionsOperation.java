package net.sf.extjwnl.dictionary.morph;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.Exc;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Param;

import java.util.Map;

/**
 * Lookup the word in the exceptions file of the given part-of-speech.
 *
 * @author John Didion <jdidion@users.sourceforge.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public class LookupExceptionsOperation extends AbstractOperation {

    public LookupExceptionsOperation(Dictionary dictionary, Map<String, Param> params) throws JWNLException {
        super(dictionary);
    }

    public boolean execute(POS pos, String derivation, BaseFormSet form) throws JWNLException {
        Exc exc = null == dictionary ? null : dictionary.getException(pos, derivation);
        if (null != exc) {
            for (String exception : exc.getExceptions()) {
                form.add(exception);
            }
            return true;
        }
        return false;
    }
}