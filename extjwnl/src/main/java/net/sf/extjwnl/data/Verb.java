package net.sf.extjwnl.data;

import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.ResourceBundleSet;

import java.util.BitSet;

/**
 * A <code>Verb</code> is a subclass of <code>Word</code> that can have 1 or more verb frames (use cases of the verb).
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Verb extends Word {

    private static final long serialVersionUID = 5L;

    final static String[] frames = {
            "Something ----s",
            "Somebody ----s",
            "It is ----ing",
            "Something is ----ing PP",
            "Something ----s something Adjective/Noun",
            "Something ----s Adjective/Noun",
            "Somebody ----s Adjective",
            "Somebody ----s something",
            "Somebody ----s somebody",
            "Something ----s somebody",
            "Something ----s something",
            "Something ----s to somebody",
            "Somebody ----s on something",
            "Somebody ----s somebody something",
            "Somebody ----s something to somebody",
            "Somebody ----s something from somebody",
            "Somebody ----s somebody with something",
            "Somebody ----s somebody of something",
            "Somebody ----s something on somebody",
            "Somebody ----s somebody PP",
            "Somebody ----s something PP",
            "Somebody ----s PP",
            "Somebody's (body part) ----s",
            "Somebody ----s somebody to INFINITIVE",
            "Somebody ----s somebody INFINITIVE",
            "Somebody ----s that CLAUSE",
            "Somebody ----s to somebody",
            "Somebody ----s to INFINITIVE",
            "Somebody ----s whether INFINITIVE",
            "Somebody ----s somebody into V-ing something",
            "Somebody ----s something with something",
            "Somebody ----s INFINITIVE",
            "Somebody ----s VERB-ing",
            "It ----s that CLAUSE",
            "Something ----s INFINITIVE"
    };

    /**
     * A bit array of all the verb frames that are valid for this word.
     */
    private final BitSet verbFrameFlags;

    public Verb(Dictionary dictionary, Synset synset, String lemma, BitSet verbFrameFlags) {
        super(dictionary, synset, lemma);
        this.verbFrameFlags = verbFrameFlags;
    }

    public BitSet getVerbFrameFlags() {
        return verbFrameFlags;
    }

    public int[] getVerbFrameIndices() {
        return Dictionary.getVerbFrameIndices(verbFrameFlags);
    }

    public String[] getVerbFrames() {
        if (null == dictionary) {
            return Dictionary.getFrames(getVerbFrameFlags(), frames);
        }
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