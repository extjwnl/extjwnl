package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.BitSet;

/**
 * A <code>Synset</code> for verbs.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class VerbSynset extends Synset {

    private static final long serialVersionUID = 4L;

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
        return VerbFrame.getFrames(verbFrameFlags);
    }

    public BitSet getVerbFrameFlags() {
        return verbFrameFlags;
    }

    public void setVerbFrameFlags(BitSet verbFrameFlags) {
        if (null == verbFrameFlags) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_050"));
        }
        this.verbFrameFlags = verbFrameFlags;
    }

    public int[] getVerbFrameIndices() {
        return VerbFrame.getVerbFrameIndices(verbFrameFlags);
    }
}