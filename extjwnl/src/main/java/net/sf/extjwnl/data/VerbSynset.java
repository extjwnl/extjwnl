package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.BitSet;

/**
 * A <code>Synset</code> for verbs.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class VerbSynset extends Synset {

    private static final long serialVersionUID = 5L;

    private BitSet verbFrameFlags;

    public VerbSynset(Dictionary dictionary) throws JWNLException {
        super(dictionary, POS.VERB);
        verbFrameFlags = new BitSet();
    }

    public VerbSynset(Dictionary dictionary, long offset) throws JWNLException {
        super(dictionary, POS.VERB, offset);
        verbFrameFlags = new BitSet();
    }

    /**
     * Returns all Verb Frames that are valid for all the words in this synset.
     *
     * @return all Verb Frames that are valid for all the words in this synset
     */
    public String[] getVerbFrames() {
        if (null == dictionary) {
            return Dictionary.getFrames(verbFrameFlags, Verb.frames);
        }
        return dictionary.getFrames(verbFrameFlags);
    }

    public BitSet getVerbFrameFlags() {
        return verbFrameFlags;
    }

    public void setVerbFrameFlags(BitSet verbFrameFlags) {
        if (null == verbFrameFlags) {
            if (null != dictionary) {
                throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_050"));
            } else {
                throw new IllegalArgumentException("Verb frame flags must be not null");
            }
        }
        this.verbFrameFlags = verbFrameFlags;
    }

    public int[] getVerbFrameIndices() {
        return Dictionary.getVerbFrameIndices(verbFrameFlags);
    }
}