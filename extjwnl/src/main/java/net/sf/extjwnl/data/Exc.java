package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.List;

/**
 * Represents an entry in an exception file. Contains all of the exceptions
 * for the given lemma.
 * <p/>
 * Exception lists are alphabetized lists of inflected forms of words and
 * their base forms. The first field of each line is an inflected form,
 * followed by a space separated list of one or more base forms of the word.
 * There is one exception list file for each syntactic category. From wndb.5WN
 * in WordNet base documentation.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Exc extends BaseDictionaryElement {

    private static final long serialVersionUID = 4L;

    private POS pos;

    /**
     * The excepted word
     */
    private String lemma;

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
        super(dictionary);
        if (null == pos) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_041"));
        }
        if (null == lemma || "".equals(lemma)) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_046"));
        }
        if (null == exceptions || 0 == exceptions.size()) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_039"));
        }
        this.pos = pos;
        this.lemma = lemma;
        this.exceptions = exceptions;
        if (null != dictionary && dictionary.isEditable()) {
            dictionary.addException(this);
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
     * Returns the lemma (derivation) of the exception word.
     *
     * @return the lemma (derivation) of the exception word
     */
    public String getLemma() {
        return lemma;
    }

    /**
     * Returns list of exceptions (base forms).
     *
     * @return list of exceptions (base forms)
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

        return JWNL.resolveMessage("DATA_TOSTRING_001", new Object[]{getLemma(), str.toString()});
    }
}