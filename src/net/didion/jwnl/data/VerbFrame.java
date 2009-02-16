/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.data;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.util.Resolvable;

import java.io.Serializable;
import java.util.BitSet;

/** A <code>VerbFrame</code> is the frame of a sentence in which it is proper to use a given verb. */
public final class VerbFrame implements Serializable {
	static final long serialVersionUID = 1450633678809744269L;

	private static VerbFrame[] _verbFrames;
	private static boolean _initalized = false;

	public static void initialize() {
		if (!_initalized) {
			int framesSize = Integer.parseInt(JWNL.resolveMessage("NUMBER_OF_VERB_FRAMES"));
			_verbFrames = new VerbFrame[framesSize];
			for (int i = 1; i <= framesSize; i++)
				_verbFrames[i - 1] = new VerbFrame(getKeyString(i), i);
			_initalized = true;
		}
	}

	public static final String getKeyString(int i) {
		StringBuffer buf = new StringBuffer();
		buf.append("VERB_FRAME_");
		int numZerosToAppend = 3 - String.valueOf(i).length();
		for (int j = 0; j < numZerosToAppend; j++)
			buf.append(0);
		buf.append(i);
		return buf.toString();
	}

	public static int getVerbFramesSize() {
		return _verbFrames.length;
	}

	/** Get frame at index <var>index</var>.*/
	public static String getFrame(int index) {
		return _verbFrames[index - 1].getFrame();
	}

	/**
	 * Get the frames at the indexes encoded in <var>l</var>.
	 * Verb Frames are encoded within <code>Word</code>s as a long. Each bit represents
	 * the frame at its corresponding index. If the bit is set, that verb
	 * frame is valid for the word.
	 */
	public static String[] getFrames(BitSet bits) {
		int[] indicies = getVerbFrameIndicies(bits);
		String[] frames = new String[indicies.length];
		for (int i = 0; i < indicies.length; i++)
			frames[i] = _verbFrames[indicies[i] - 1].getFrame();
		return frames;
	}

	/**
	 * Gets the verb frame indices for a synset. This is the collection
	 * of f_num values for a synset definition. In the case of a synset, this
	 * is only the values that are true for all words with the synset. In other
	 * words, only the sentence frames that belong to all words. 
	 * @param bits the bit set
	 * @return an integer collection
	 */
	public static int[] getVerbFrameIndicies(BitSet bits) {
		int[] indices = new int[bits.cardinality()];
		int index = 0;
		for (int i = bits.nextSetBit(0); i >=0; i = bits.nextSetBit(i+1)) {
			indices[index++] = i;
		}
		
		return indices;

	}

	private Resolvable _frame;
	private int _index;

	private VerbFrame(String frame, int index) {
		_frame = new Resolvable(frame);
		_index = index;
	}

	public String getFrame() {
		return _frame.toString();
	}

	public int getIndex() {
		return _index;
	}

	private String _cachedToString = null;

	public String toString() {
		if (_cachedToString == null)
			_cachedToString = JWNL.resolveMessage("DATA_TOSTRING_007", getFrame());
		return _cachedToString;
	}

	public int hashCode() {
		return getIndex();
	}
}