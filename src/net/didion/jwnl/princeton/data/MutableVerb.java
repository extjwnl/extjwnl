package net.didion.jwnl.princeton.data;

import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Verb;

import java.util.BitSet;

/**
 * Wrapper for a verb that allows the VerbFrame flags to be set after the Verb
 * is created.
 */
class MutableVerb extends Verb {
	public MutableVerb(Synset synset, int index, String lemma) {
		super(synset, index, lemma, new BitSet());
	}

	public void setVerbFrameFlag(int fnum) {
		getVerbFrameFlags().set(fnum);
	}
}
