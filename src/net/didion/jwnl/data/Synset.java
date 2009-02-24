/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.data;

import java.io.IOException;
import java.util.BitSet;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;

/**
 * A <code>Synset</code>, or <b>syn</b>onym <b>set</b>, represents a
 * line of a WordNet <var>pos</var><code>.data</code> file. A <code>Synset</code>
 * represents a concept, and contains a set of <code>Word</code>s, each of
 * which has a sense that names that concept (and each of which is therefore
 * synonymous with the other words in the <code>Synset</code>).
 * <p>
 * <code>Synset</code>'s are linked by {@link Pointer}s into a network of related
 * concepts; this is the <it>Net</it> in WordNet. {@link #getTargets getTargets}
 * retrieves the targets of these links, and {@link #getPointers getPointers}
 * retrieves the pointers themselves.
 */
public class Synset extends PointerTarget implements DictionaryElement {
	static final long serialVersionUID = 4038955719653496529L;

	protected POS _pos;
	protected Pointer[] _pointers;
	/** The offset of this synset in the data file. */
	protected long _offset;
	/** The words in this synset. */
	protected Word[] _words;
	/** The text (definition, usage examples) associated with the synset. */
	protected String _gloss;
	protected BitSet _verbFrameFlags;
	/** for use only with WordNet 1.6 and earlier */
	protected boolean _isAdjectiveCluster;
	
	/**
	 * The lexicographer file name id.
	 */
	protected long lexFileNum;
	
	/**
	 * The proper name for the lexicographer file (noun.plant, etc)
	 */
	protected String lexFileName;
	
	
	public Synset(POS pos, long offset, Word[] words, Pointer[] pointers, String gloss, BitSet verbFrames) {
		this(pos, offset, words, pointers, gloss, verbFrames, false);
	}

	public Synset(POS pos, long offset, Word[] words, Pointer[] pointers, String gloss,
	              BitSet verbFrames, boolean isAdjectiveCluster) {
		_pos = pos;
		_pointers = pointers;
		_offset = offset;
		_words = words;
		_gloss = gloss;
		_verbFrameFlags = verbFrames;
		_isAdjectiveCluster = isAdjectiveCluster;
	}

	public DictionaryElementType getType() {
		return DictionaryElementType.SYNSET;
	}

	// Object methods

	/** Two Synsets are equal if their POS's and offsets are equal */
	public boolean equals(Object object) {
		return (object instanceof Synset) && ((Synset) object).getPOS().equals(getPOS()) && ((Synset) object).getOffset() == getOffset();
	}

	public int hashCode() {
		return getPOS().hashCode() ^ (int) getOffset();
	}

	private transient String _cachedToString = null;

	public String toString() {
		if (_cachedToString == null) {
			StringBuffer words = new StringBuffer();
			for (int i = 0; i < getWordsSize(); ++i) {
				if (i > 0) words.append(", ");
				words.append(getWord(i).getLemma());
			}

			if (getGloss() != null)
				words.append(" -- (" + getGloss() + ")");

			_cachedToString =
			    JWNL.resolveMessage("DATA_TOSTRING_009", new Object[]{new Long(getOffset()), getPOS(), words.toString()});
		}
		return _cachedToString;
	}

	// Accessors

	public POS getPOS() {
		return _pos;
	}

	public Pointer[] getPointers() {
		return _pointers;
	}

	public String getGloss() {
		return _gloss;
	}

	public Word[] getWords() {
		return _words;
	}

	public int getWordsSize() {
		return getWords().length;
	}

	public Word getWord(int index) {
		return _words[index];
	}

	public long getOffset() {
		return _offset;
	}

	public Object getKey() {
		return new Long(getOffset());
	}

	public boolean isAdjectiveCluster() {
		return _isAdjectiveCluster;
	}

	/** Returns all Verb Frames that are valid for all the words in this synset */
	public String[] getVerbFrames() {
		return VerbFrame.getFrames(_verbFrameFlags);
	}

	public BitSet getVerbFrameFlags() {
		return _verbFrameFlags;
	}

	public int[] getVerbFrameIndicies() {
		return VerbFrame.getVerbFrameIndicies(_verbFrameFlags);
	}

	/** Returns true if <code>lemma</code> is one of the words contained in this synset.*/
	public boolean containsWord(String lemma) {
		for (int i = 0; i < getWordsSize(); i++) {
			if (getWord(i).getLemma().equals(lemma))
				return true;
		}
		return false;
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		// set POS to reference the static instance defined in the current runtime environment
		_pos = POS.getPOSForKey(_pos.getKey());
	}

	/**
	 * Gets the lexicographer file name containing this synset.
	 * @return two digit decimal integer
	 */
	public long getLexFileNum() {
		return lexFileNum;
	}

	/**
	 * Sets the lexicographer file name containing this synset.
	 * @param lexFileId - the lexicographer file name id
	 */
	public void setLexFileNum(long lexFileId) {
		this.lexFileNum = lexFileId;
		lexFileName = LexFileIdMap.getFileName(lexFileId);
	}

	/**
	 * Gets the lex file name.
	 * @return
	 */
	public String getLexFileName() {
		return lexFileName;
	}
	
	/**
	 * Gets the sense key of a lemma. This will be refactored in 2.0 with 
	 * the architecture reworking. 
	 * @param lemma lemma sense to grab
	 * @return sense key for lemma
	 */
	public String getSenseKey(String lemma) {
		int ss_type = 5;
		if (this.getPOS().equals(POS.NOUN)) {
			ss_type = 1;
		} else if (this.getPOS().equals(POS.VERB)) {
			ss_type = 2;
		} else if (this.getPOS().equals(POS.ADJECTIVE)) {
			ss_type = 3;
		} else if (this.getPOS().equals(POS.ADVERB)) {
			ss_type = 4;
		}
			
		if (isAdjectiveCluster()) {
			ss_type = 5;
		}
		int lexId = -1;
		for (int i = 0; i < this.getWords().length; i++) {
			Word w = this.getWords()[i];
			if (w.getLemma().equals(lemma)) {
				lexId = w.getLexId();
			}
		}
		
		String lexNumStr = "";
		long lexNum = getLexFileNum();
		if (lexNum < 10) {
			lexNumStr = "0" + lexNum;
		} else {
			lexNumStr = String.valueOf(lexNum);
		}
		
		String lexIdStr = "";
		if (lexId < 10) {
			lexIdStr = "0" + lexId;
		} else {
			lexIdStr = String.valueOf(lexId);
		}

		String senseKey = lemma + "%" + ss_type + ":" + lexNumStr;
		senseKey += ":" + lexIdStr + ":";
		
		String head = ":";
		if (ss_type == 5) {
			try {
				Pointer[] p = this.getPointers(PointerType.SIMILAR_TO);
				if (p.length > 0) {
					Pointer headWord = p[0];
					Word[] words = headWord.getTargetSynset().getWords();
					if (words.length > 0) {
						head = words[0].getLemma() + ":";
						lexIdStr = "";
						if (words[0].getLexId() < 10) {
							lexIdStr = "0" + words[0].getLexId();
						} else {
							lexIdStr = String.valueOf(words[0].getLexId());
						}
						head += lexIdStr;
					}
				}
			} catch (JWNLException e) { 
				e.printStackTrace(); 
			}
		}
		senseKey += head;
		
		return senseKey;
		
	}
}