package net.sf.extjwnl.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Tokenizes CharSequence.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class CharSequenceTokenizer implements Iterator<CharSequence> {

    // follows StringTokenizer where possible

    private final CharSequence s;
    private int currentPosition;
    private int newPosition;
    private final int maxPosition;

    public CharSequenceTokenizer(CharSequence s) {
        this.s = s;
        currentPosition = 0;
        newPosition = -1;
        maxPosition = s.length();
    }

    /**
     * Tests if there are more tokens available from this tokenizer's string.
     * If this method returns <tt>true</tt>, then a subsequent call to
     * <tt>nextToken</tt> will successfully return a token.
     *
     * @return <code>true</code> if and only if there is at least one token
     * in the string after the current position; <code>false</code>
     * otherwise.
     */
    public boolean hasMoreTokens() {
        /*
         * Temporarily store this position and use it in the following
         * nextToken() method only if the delimiters haven't been changed in
         * that nextToken() invocation.
         */
        newPosition = skipDelimiters(currentPosition);
        return (newPosition < maxPosition);
    }

    /**
     * Returns the next token from this string tokenizer.
     *
     * @return the next token from this string tokenizer.
     * @throws NoSuchElementException if there are no more tokens in this tokenizer's string.
     */
    public CharSequence nextToken() {
        int start = skip();
        return s.subSequence(start, currentPosition);
    }

    /**
     * Skips the next token.
     *
     * @throws NoSuchElementException if there are no more tokens in this tokenizer's string.
     */
    public void skipToken() {
        skip();
    }

    /**
     * Returns remainder of the string.
     *
     * @return remainder of the string
     */
    public CharSequence remainder() {
        currentPosition = skipDelimiters(currentPosition);
        return s.subSequence(currentPosition, maxPosition);
    }

    @Override
    public boolean hasNext() {
        return hasMoreTokens();
    }

    @Override
    public CharSequence next() {
        return nextToken();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Skips to the next token and returns previous position.
     *
     * @throws NoSuchElementException if there are no more tokens in this tokenizer's string.
     */
    private int skip() {
        /*
         * If next position already computed in hasMoreElements()
         * then use the computed value.
         */
        currentPosition = (newPosition >= 0) ? newPosition : skipDelimiters(currentPosition);

        /* Reset it anyway */
        newPosition = -1;

        if (currentPosition >= maxPosition) {
            throw new NoSuchElementException();
        }
        int start = currentPosition;
        currentPosition = scanToken(currentPosition);
        return start;
    }

    /**
     * Skips delimiters starting from the specified position.
     * Returns the index of the first non-delimiter character at or after startPos.
     */
    private int skipDelimiters(int startPos) {
        int position = startPos;
        while (position < maxPosition && ' ' == s.charAt(position)) {
            position++;
        }
        return position;
    }

    /**
     * Skips ahead from startPos and returns the index of the next delimiter
     * character encountered, or maxPosition if no such delimiter is found.
     */
    private int scanToken(int startPos) {
        int position = startPos;
        while (position < maxPosition && ' ' != s.charAt(position)) {
            position++;
        }
        return position;
    }
}
