package net.sf.extjwnl.dictionary.morph;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 * yet to be implemented
 */
public class RemovePrepPhrasesOperation extends AbstractOperation {

    public RemovePrepPhrasesOperation(Dictionary dictionary) {
        super(dictionary);
    }

    public boolean execute(POS pos, String lemma, BaseFormSet baseForm) {
        return false;
    }
}