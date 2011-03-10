package net.sf.extjwnl.princeton.data;

import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Verb;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.BitSet;

/**
 * Wrapper for a verb that allows the VerbFrame flags to be set after the Verb is created.
 *
 * @author John Didion <jdidion@didion.net>
 */
class MutableVerb extends Verb {
    public MutableVerb(Dictionary dictionary, Synset synset, int index, String lemma) {
        super(dictionary, synset, index, lemma, new BitSet());
    }

    public void setVerbFrameFlag(int fnum) {
        getVerbFrameFlags().set(fnum);
    }
}
