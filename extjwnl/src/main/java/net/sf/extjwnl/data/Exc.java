package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.ResourceBundleSet;

import java.util.List;

/**
 * Represents an entry in an exception file. Contains all of the exceptions
 * for the given lemma.
 * <p>
 * Exception lists are alphabetized lists of inflected forms of words and
 * their base forms. The first field of each line is an inflected form,
 * followed by a space separated list of one or more base forms of the word.
 * There is one exception list file for each syntactic category. From wndb.5WN
 * in WordNet base documentation.
 * </p>
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Exc extends BaseDictionaryElement {

	private static final long serialVersionUID = 5L;

	private POS pos;

	/**
	 * The lemma (derivation) of the exception word, e.g. aardwolves
	 */
	private String lemma;

	/**
	 * The normalized form, e.g. aardwolf
	 */
	private List<String> exceptions;

	/**
	 * Creates a new exception entry.
	 *
	 * @param dictionary the owner of the exception
	 * @param pos        the exception part of speech
	 * @param lemma      the word's lemma form
	 * @param exceptions the given exceptions
	 * @throws JWNLException JWNLException
	 */
	public Exc(Dictionary dictionary, POS pos, String lemma, List<String> exceptions) throws JWNLException {
		this.dictionary = dictionary;
		if (null == pos) {
			if (null != dictionary) {
				throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_041"));
			}
			else {
				throw new IllegalArgumentException("Pos must be not null");
			}
		}
		this.pos = pos;
		if (null == lemma || "".equals(lemma)) {
			if (null != dictionary) {
				throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_046"));
			}
			else {
				throw new IllegalArgumentException("Lemma must be not null and not empty");
			}
		}
		this.lemma = lemma;
		if (null == exceptions || 0 == exceptions.size()) {
			if (null != dictionary) {
				throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_039"));
			}
			else {
				throw new IllegalArgumentException("Exceptions must be not null and not empty");
			}
		}
		this.exceptions = exceptions;
		if (null != dictionary && dictionary.isEditable()) {
			dictionary.addElement(this);
		}
	}

	public DictionaryElementType getType() {
		return DictionaryElementType.EXCEPTION;
	}

	public POS getPOS() {
		return pos;
	}

	public Object getKey() {
		return getLemma();
	}

	/**
	 * Returns the lemma (derivation) of the exception word, e.g. aardwolves
	 *
	 * @return the lemma (derivation) of the exception word, e.g. aardwolves
	 */
	public String getLemma() {
		return lemma;
	}

	/**
	 * Returns list of exceptions (base forms), e.g. aardwolf
	 *
	 * @return list of exceptions (base forms), e.g. aardwolf
	 */
	public List<String> getExceptions() {
		return exceptions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Exc)) {
			return false;
		}

		Exc exc = (Exc) o;

		if (!exceptions.equals(exc.exceptions)) {
			return false;
		}
		if (!lemma.equals(exc.lemma)) {
			return false;
		}
		//noinspection RedundantIfStatement
		if (!pos.equals(exc.pos)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = pos.hashCode();
		result = 31 * result + lemma.hashCode();
		result = 31 * result + exceptions.hashCode();
		return result;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < exceptions.size(); i++) {
			str.append(exceptions.get(i));
			if (i != exceptions.size() - 1) {
				str.append(", ");
			}
		}

		return ResourceBundleSet.insertParams("Exc: [Lemma: {0}] Exceptions: {1}]", new Object[]{getLemma(), str.toString()});
	}
}