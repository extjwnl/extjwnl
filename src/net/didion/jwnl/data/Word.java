/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.data;

import java.util.ArrayList;
import java.util.List;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.dictionary.Dictionary;

/**
 * A <code>Word</code> represents the lexical information related to a specific sense of an <code>IndexWord</code>.
 * <code>Word</code>'s are linked by {@link Pointer}s into a network of lexically related words.
 * {@link #getTargets getTargets} retrieves the targets of these links, and
 * {@link Word#getPointers getPointers} retrieves the pointers themselves.
 */
public class Word extends PointerTarget {
	/**
	 * The serialization id. 
	 */
	private static final long serialVersionUID = 8591237840924027785L;
	
	/** The Synset to which this word belongs. */
	private Synset _synset;
	
	/** This word's index within the synset. */
	private int _index;
	
	/** The string representation of the word. */
    
	private String _lemma;
	
	/**
	 * The sense key for this word within the synset. 
	 */
	private String senseKey;
	
	/**
	 * The usage tag of this specific word in the synset.
	 */
	private int usageTag = 0;
	
	/**
	 * This is false until getSenseKey() is called. 
	 */
	private boolean keysLoaded = false;
	
	/**
	 * This is false until getUsageCount() is called. 
	 */
	private boolean usageLoaded = false;
	
	/**
	 * Proxy constructor for an unitialized word. 
	 * @param lemma 
	 * @param senseKey
	 * @param usageCnt
	 */
	public Word(String lemma, String senseKey, int usageCnt) {
		_lemma = lemma;
		setSenseKey(senseKey);
		setUsageCount(usageCnt); 
	}

    /**
     * Constructs a word tied to a synset, it's position within the synset, and the lemma.
     * @param synset - the synset this word is contained in
     * @param index - the position of the word in the synset (usage)
     * @param lemma - the lemma of this word
     */
	public Word(Synset synset, int index, String lemma) {
		_synset = synset;
		_index = index;
		_lemma = lemma;
	}
	
	/**
	 * Creates a word with the synset, index, lemma, and senseKey. 
	 * @param synset the synset
	 * @param index the index
	 * @param lemma the lemma
	 * @param senseKey
	 * @param usageCnt 
	 */
	public Word(Synset synset, int index, String lemma, String senseKey, int usageCnt) {
		_synset = synset;
		_index = index;
		_lemma = lemma;
		setSenseKey(senseKey);
		setUsageCount(usageCnt);
	}

	// Object methods

	/** Two words are equal if their parent Synsets are equal and they have the same index */
	public boolean equals(Object object) {
		return (object instanceof Word)
				&& ((Word) object).getSynset().equals(getSynset())
				&& ((Word) object).getIndex() == getIndex();
	}

	public int hashCode() {
		return getSynset().hashCode() ^ getIndex();
	}

	private transient String _cachedToString = null;

	public String toString() {
		if (_cachedToString == null) {
			Object[] params = new Object[]{getPOS(), getLemma(), getSynset(), new Integer(getIndex())};
			_cachedToString = JWNL.resolveMessage("DATA_TOSTRING_005", params);
		}
		return _cachedToString;
	}

	// Accessors

    /**
     * Gets the synset associated with this word. 
     * @return synset 
     */
	public Synset getSynset() {
		return _synset;
	}

    /**
     * Gets the part of speech of this word. 
     * @return part of speech
     */
	public POS getPOS() {
		return _synset.getPOS();
	}

    /**
     * Gets the index of this word. 
     * @return index
     */
	public int getIndex() {
		return _index;
	}

    /**
     * Gets the lemma of this word. 
     * @return lemma 
     */
	public String getLemma() {
		return _lemma;
	}
	
	/**
	 * Sets the sense key to this word. 
	 * @param sk the sense key
	 */
	public void setSenseKey(String sk) {
		keysLoaded = true;
		senseKey = sk;
	}
	
	/**
	 * Gets the sense key for this Word. For more information, see SenseIdX(5WN) in WordNet documentation.
	 * @return WordNet sense key
	 */
	public String getSenseKey() {
		if (!keysLoaded) {
			senseKey = Dictionary.getInstance().getSenseKey(getSynset().getOffset(), getLemma());
			keysLoaded = true;
		}
		return senseKey;
	}

	/** returns all the pointers of the synset that contains this word whose source is this word */
	public Pointer[] getPointers() {
		Pointer[] source = getSynset().getPointers();
		List list = new ArrayList(source.length);
		for (int i = 0; i < source.length; ++i) {
			Pointer pointer = source[i];
			if (this.equals(pointer.getSource()))
				list.add(pointer);
		}
		return (Pointer[]) list.toArray(new Pointer[list.size()]);
	}

	/**
	 * Gets the usage if this word has been tagged within WordNet. 
	 * @return - the usage number. 
	 */
	public int getUsageCount() {
		if (!usageLoaded) {
			usageTag = Dictionary.getInstance().getUsageCount(getSynset().getOffset(), getLemma());
			usageLoaded = true;
		}
		return usageTag;
	}

	/**
	 * Sets the usage tag for this word. 
	 * @param usageTag - usage number in tagged texts.
	 */
	public void setUsageCount(int usageTag) {
		this.usageTag = usageTag;
		usageLoaded = true;
	}
}