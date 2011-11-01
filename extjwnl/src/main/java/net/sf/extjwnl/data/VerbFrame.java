package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;

import java.io.Serializable;
import java.util.BitSet;

/**
 * A <code>VerbFrame</code> is the frame of a sentence in which it is proper to use a given verb.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class VerbFrame implements Serializable {

    static {
        JWNL.initialize();
    }

    private static final long serialVersionUID = 4L;

    private static final VerbFrame[] verbFrames;

    static {
        int framesSize = Integer.parseInt(JWNL.resolveMessage("NUMBER_OF_VERB_FRAMES"));
        verbFrames = new VerbFrame[framesSize];
        for (int i = 1; i <= framesSize; i++) {
            verbFrames[i - 1] = new VerbFrame(getKeyString(i), i);
        }
    }

    public static String getKeyString(int i) {
        StringBuilder buf = new StringBuilder();
        buf.append("VERB_FRAME_");
        int numZerosToAppend = 3 - String.valueOf(i).length();
        for (int j = 0; j < numZerosToAppend; j++) {
            buf.append(0);
        }
        buf.append(i);
        return buf.toString();
    }

    public static int getVerbFramesSize() {
        return verbFrames.length;
    }

    /**
     * Returns frame at index <var>index</var>.
     *
     * @param index index
     * @return frame at index <var>index</var>
     */
    public static String getFrame(int index) {
        return verbFrames[index - 1].getFrame();
    }

    /**
     * Returns the frames at the indexes encoded in <var>l</var>.
     * Verb Frames are encoded within <code>Word</code>s as a long. Each bit represents
     * the frame at its corresponding index. If the bit is set, that verb
     * frame is valid for the word.
     *
     * @param bits frame flags
     * @return the frames at the indexes encoded in <var>l</var>
     */
    public static String[] getFrames(BitSet bits) {
        int[] indices = getVerbFrameIndices(bits);
        String[] frames = new String[indices.length];
        for (int i = 0; i < indices.length; i++) {
            frames[i] = verbFrames[indices[i] - 1].getFrame();
        }
        return frames;
    }

    /**
     * Returns the verb frame indices for a synset. This is the collection
     * of f_num values for a synset definition. In the case of a synset, this
     * is only the values that are true for all words with the synset. In other
     * words, only the sentence frames that belong to all words.
     *
     * @param bits the bit set
     * @return an integer collection
     */
    public static int[] getVerbFrameIndices(BitSet bits) {
        int[] indices = new int[bits.cardinality()];
        int index = 0;
        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
            indices[index++] = i;
        }

        return indices;
    }

    private final transient String frame;
    private final int index;

    private VerbFrame(String frame, int index) {
        this.frame = JWNL.resolveMessage(frame);
        this.index = index;
    }

    public String getFrame() {
        return frame;
    }

    public int getIndex() {
        return index;
    }

    public String toString() {
        return JWNL.resolveMessage("DATA_TOSTRING_007", getFrame());
    }

    public int hashCode() {
        return getIndex();
    }
}