package net.didion.jwnl.dictionary.morph;

import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;

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