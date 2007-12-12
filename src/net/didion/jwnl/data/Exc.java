/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.data;

import net.didion.jwnl.JWNL;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Represents an entry in an exception file. Contains all of the exceptions
 * for the given lemma.
 */
public final class Exc implements DictionaryElement {
    
    /**
     * Unique identifier. 
     */
	static final long serialVersionUID = -5792651340274489357L;

    /**
     * The part of speech. 
     */
	private POS _pos;
    
	/** The excepted word */
	private String _lemma;
	
    
    /** All the exceptions for <code>lemma</code>. */
	private List _exceptions;
    
    /**
     * The exception string. 
     */
    private String _exceptionString = null;
    
    /**
     * The cached to string value. 
     */
    private transient String _cachedToString = null;

	/**
     * Creates a new exception entry. 
     * @param pos - the part of speech
     * @param lemma - the word's lemma form
     * @param exceptions - the given exceptions
	 */
    public Exc(POS pos, String lemma, List exceptions) {
		_pos = pos;
		_lemma = lemma;
		_exceptions = Collections.unmodifiableList(exceptions);
	}

    /**
     * Gets the type of this exception entry. 
     */
	public DictionaryElementType getType() {
		return DictionaryElementType.EXCEPTION;
	}

    /**
     * Gets the part of speech. 
     * @return
     */
	public POS getPOS() {
		return _pos;
	}

    /**
     * Gets the lemma of the exception word.
     * @return lemma
     */
	public String getLemma() {
		return _lemma;
	}

	/** Get the exception at index <code>index</code>. */
	public String getException(int index) {
		return (String)getExceptions().get(index);
	}

    /**
     * Gets the number of exceptions.
     * @return int
     */
	public int getExceptionsSize() {
		return getExceptions().size();
	}

	/** Get the collection of Exc objects in array form. */
	public String[] getExceptionArray() {
		return (String[])getExceptions().toArray(new String[_exceptions.size()]);
	}

	/** Get the List of exceptions. */
	public List getExceptions() {
		return _exceptions;
	}

    /**
     * Gets the lemma. 
     */
	public Object getKey() {
		return getLemma();
	}

    /**
     * Returns true if lemma and exceptions are equal. 
     */
	public boolean equals(Object obj) {
		return (obj instanceof Exc) &&
			getLemma().equals(((Exc)obj).getLemma()) &&
			getExceptions().equals(((Exc)obj).getExceptions());
	}


    /**
     * {@inheritDoc}
     */
	public String toString() {
		if (_cachedToString == null) {
			_cachedToString =
				JWNL.resolveMessage("DATA_TOSTRING_001", new Object[] { getLemma(), getExceptionsAsString() });
		}
		return _cachedToString;
	}

    /**
     * {@inheritDoc}
     */
	public int hashCode() {
		int hash = getLemma().hashCode();
		for (int i = 0; i < getExceptionsSize(); i++) {
			hash ^= getException(i).hashCode();
		}
		return hash;
	}

    /**
     * Gets the exceptions as a string bundle. 
     * @return
     */
	private String getExceptionsAsString() {
		if (_exceptionString == null) {
			String str = "";
			for (int i = 0;  i < getExceptionsSize(); i++) {
				str += getException(i);
				if (i != getExceptionsSize() - 1) {
					str += ", ";
				}
			}
			_exceptionString = str;
		}
		return _exceptionString;
	}

    
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		// set POS to reference the static instance defined in the current runtime environment
		_pos = POS.getPOSForKey(_pos.getKey());
	}
}






