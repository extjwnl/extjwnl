package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.BitSet;

/**
 * A <code>Synset</code> for verbs.
 *
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public class VerbSynset extends Synset {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final BitSet EMPTY_BIT_SET = new BitSet();
    private static final int[] EMPTY_INT_ARRAY = new int[0];

    private BitSet verbFrameFlags;

    public VerbSynset(Dictionary dictionary, POS pos) throws JWNLException {
        super(dictionary, pos);
        verbFrameFlags = new BitSet();
    }

    public VerbSynset(Dictionary dictionary, POS pos, long offset) throws JWNLException {
        super(dictionary, pos, offset);
        verbFrameFlags = new BitSet();
    }

    /**
     * Returns all Verb Frames that are valid for all the words in this synset.
     *
     * @return all Verb Frames that are valid for all the words in this synset
     */
    public String[] getVerbFrames() {
        if (POS.VERB == pos) {
            return VerbFrame.getFrames(verbFrameFlags);
        } else {
            return EMPTY_STRING_ARRAY;
        }
    }

    public BitSet getVerbFrameFlags() {
        if (POS.VERB == pos) {
            return verbFrameFlags;
        } else {
            return EMPTY_BIT_SET;
        }
    }

    public void setVerbFrameFlags(BitSet verbFrameFlags) {
        if (POS.VERB != pos) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_049"));
        }
        if (null == verbFrameFlags) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_050"));
        }
        this.verbFrameFlags = verbFrameFlags;
    }

    public int[] getVerbFrameIndices() {
        if (POS.VERB == pos) {
            return VerbFrame.getVerbFrameIndices(verbFrameFlags);
        } else {
            return EMPTY_INT_ARRAY;
        }
    }

}
