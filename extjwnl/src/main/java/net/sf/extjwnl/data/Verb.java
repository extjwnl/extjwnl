package net.sf.extjwnl.data;

import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.ResourceBundleSet;

import java.util.BitSet;

/**
 * A <code>Verb</code> is a subclass of <code>Word</code> that can have 1 or more verb frames (use cases of the verb).
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Verb extends Word {

    private static final long serialVersionUID = 4L;

    /**
     * A bit array of all the verb frames that are valid for this word.
     */
    private final BitSet verbFrameFlags;

    public Verb(Dictionary dictionary, Synset synset, int index, String lemma, BitSet verbFrameFlags) {
        super(dictionary, synset, index, lemma);
        this.verbFrameFlags = verbFrameFlags;
    }

    public BitSet getVerbFrameFlags() {
        return verbFrameFlags;
    }

    public int[] getVerbFrameIndices() {
        return dictionary.getVerbFrameIndices(verbFrameFlags);
    }

    public String[] getVerbFrames() {
        return dictionary.getFrames(getVerbFrameFlags());
    }

    private String getVerbFramesAsString() {
        String[] frames = getVerbFrames();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < frames.length; i++) {
            buf.append(frames[i]);
            if (i != frames.length - 1) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }

    public String toString() {
        return ResourceBundleSet.insertParams("[Word: {0} [Lemma: {1}] {2} [Index: {3}] VerbFrames: {4}]",
                new Object[]{getPOS(), getLemma(), getSynset(),
                getIndex(),
                getVerbFramesAsString()});
    }
}