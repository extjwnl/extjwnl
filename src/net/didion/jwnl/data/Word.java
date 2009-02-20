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
	
	/** The lexicographer id that identifies this lemma. */
	protected int lexId;
	


    public int getLexId() {
		return lexId;
	}


	public void setLexId(int lexId) {
		this.lexId = lexId;
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

}