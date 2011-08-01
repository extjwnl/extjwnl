package net.sf.extjwnl.util;

import java.util.StringTokenizer;

/**
 * A <code>StringTokenizer</code> with extensions to retrieve the values of numeric tokens, as well as strings.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TokenizerParser extends StringTokenizer {
    public TokenizerParser(String string, String delimiters) {
        super(string, delimiters);
    }

    /**
     * Converts the next token into a byte
     *
     * @return next byte
     */
    public int nextByte() {
        return Byte.parseByte(nextToken());
    }

    /**
     * Converts the next token into a short
     *
     * @return next short
     */
    public int nextShort() {
        return Short.parseShort(nextToken());
    }

    /**
     * Converts the next token into an int
     *
     * @return next integer
     */
    public int nextInt() {
        return Integer.parseInt(nextToken());
    }

    /**
     * Converts the next token into an int with base <var>radix</var>
     *
     * @param radix the base into which to convert the next token
     * @return integer with base <var>radix</var>
     */
    public int nextInt(int radix) {
        return Integer.parseInt(nextToken(), radix);
    }

    /**
     * Converts the next token into a base 16 int.
     *
     * @return int of a base 16
     */
    public int nextHexInt() {
        return nextInt(16);
    }

    /**
     * Converts the next token into a long
     *
     * @return next long
     */
    public long nextLong() {
        return Long.parseLong(nextToken());
    }
}