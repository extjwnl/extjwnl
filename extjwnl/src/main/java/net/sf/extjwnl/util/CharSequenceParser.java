package net.sf.extjwnl.util;

import java.util.NoSuchElementException;

/**
 * Parses CharSequence.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class CharSequenceParser extends CharSequenceTokenizer {

    public CharSequenceParser(CharSequence s) {
        super(s);
    }

    /**
     * Returns the next token from this string tokenizer.
     *
     * @return     the next token from this string tokenizer.
     * @exception NoSuchElementException  if there are no more tokens in this tokenizer's string.
     */
    @Override
    public String nextToken() {
        return super.nextToken().toString();
    }

    @Override
    public String remainder() {
        return super.remainder().toString();
    }

    /**
     * Converts the next token into a long.
     *
     * @return next long
     */
    public long nextLong() {
        return parseLong(super.nextToken(), 10);
    }

    /**
     * Converts the next token into an int.
     *
     * @return next int
     */
    public int nextInt() {
        return parseInt(super.nextToken(), 10);
    }

    /**
     * Converts the next hex-encoded token into an int.
     *
     * @return next hex-encoded int
     */
    public int nextHexInt() {
        return parseInt(super.nextToken(), 16);
    }

    /**
     * Converts the next token into a char.
     *
     * @return next char
     */
    public char nextChar() {
        return super.nextToken().charAt(0);
    }

    /**
     * Parses the string argument as a signed {@code long} in the
     * radix specified by the second argument. The characters in the
     * string must all be digits of the specified radix (as determined
     * by whether {@link java.lang.Character#digit(char, int)} returns
     * a nonnegative value), except that the first character may be an
     * ASCII minus sign {@code '-'} ({@code '\u005Cu002D'}) to
     * indicate a negative value or an ASCII plus sign {@code '+'}
     * ({@code '\u005Cu002B'}) to indicate a positive value. The
     * resulting {@code long} value is returned.
     * <p/>
     * <p>Note that neither the character {@code L}
     * ({@code '\u005Cu004C'}) nor {@code l}
     * ({@code '\u005Cu006C'}) is permitted to appear at the end
     * of the string as a type indicator, as would be permitted in
     * Java programming language source code - except that either
     * {@code L} or {@code l} may appear as a digit for a
     * radix greater than or equal to 22.
     * <p/>
     * <p>An exception of type {@code NumberFormatException} is
     * thrown if any of the following situations occurs:
     * <ul>
     * <p/>
     * <li>The first argument is {@code null} or is a string of
     * length zero.
     * <p/>
     * <li>The {@code radix} is either smaller than {@link
     * java.lang.Character#MIN_RADIX} or larger than {@link
     * java.lang.Character#MAX_RADIX}.
     * <p/>
     * <li>Any character of the string is not a digit of the specified
     * radix, except that the first character may be a minus sign
     * {@code '-'} ({@code '\u005Cu002d'}) or plus sign {@code
     * '+'} ({@code '\u005Cu002B'}) provided that the string is
     * longer than length 1.
     * <p/>
     * <li>The value represented by the string is not a value of type {@code long}.
     * </ul>
     * <p/>
     * <p>Examples:
     * <blockquote><pre>
     * parseLong("0", 10) returns 0L
     * parseLong("473", 10) returns 473L
     * parseLong("+42", 10) returns 42L
     * parseLong("-0", 10) returns 0L
     * parseLong("-FF", 16) returns -255L
     * parseLong("1100110", 2) returns 102L
     * parseLong("99", 8) throws a NumberFormatException
     * parseLong("Hazelnut", 10) throws a NumberFormatException
     * parseLong("Hazelnut", 36) returns 1356099454469L
     * </pre></blockquote>
     *
     * @param s     the {@code CharSequence} containing the {@code long} representation to be parsed.
     * @param radix the radix to be used while parsing {@code s}.
     * @return the {@code long} represented by the string argument in the specified radix.
     * @throws NumberFormatException if the string does not contain a parsable {@code long}.
     */
    private static long parseLong(CharSequence s, int radix) throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
        }
        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
        }

        long result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        long limit = -Long.MAX_VALUE;
        long multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstChar != '+')
                    throw NumberFormatExceptionForInputString(s);

                if (len == 1) // Cannot have lone "+" or "-"
                    throw NumberFormatExceptionForInputString(s);
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++), radix);
                if (digit < 0) {
                    throw NumberFormatExceptionForInputString(s);
                }
                if (result < multmin) {
                    throw NumberFormatExceptionForInputString(s);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw NumberFormatExceptionForInputString(s);
                }
                result -= digit;
            }
        } else {
            throw NumberFormatExceptionForInputString(s);
        }
        return negative ? result : -result;
    }

    /**
     * Parses the string argument as a signed integer in the radix
     * specified by the second argument. The characters in the string
     * must all be digits of the specified radix (as determined by
     * whether {@link java.lang.Character#digit(char, int)} returns a
     * nonnegative value), except that the first character may be an
     * ASCII minus sign {@code '-'} ({@code '\u005Cu002D'}) to
     * indicate a negative value or an ASCII plus sign {@code '+'}
     * ({@code '\u005Cu002B'}) to indicate a positive value. The
     * resulting integer value is returned.
     *
     * <p>An exception of type {@code NumberFormatException} is
     * thrown if any of the following situations occurs:
     * <ul>
     * <li>The first argument is {@code null} or is a string of
     * length zero.
     *
     * <li>The radix is either smaller than
     * {@link java.lang.Character#MIN_RADIX} or
     * larger than {@link java.lang.Character#MAX_RADIX}.
     *
     * <li>Any character of the string is not a digit of the specified
     * radix, except that the first character may be a minus sign
     * {@code '-'} ({@code '\u005Cu002D'}) or plus sign
     * {@code '+'} ({@code '\u005Cu002B'}) provided that the
     * string is longer than length 1.
     *
     * <li>The value represented by the string is not a value of type {@code int}.
     * </ul>
     *
     * <p>Examples:
     * <blockquote><pre>
     * parseInt("0", 10) returns 0
     * parseInt("473", 10) returns 473
     * parseInt("+42", 10) returns 42
     * parseInt("-0", 10) returns 0
     * parseInt("-FF", 16) returns -255
     * parseInt("1100110", 2) returns 102
     * parseInt("2147483647", 10) returns 2147483647
     * parseInt("-2147483648", 10) returns -2147483648
     * parseInt("2147483648", 10) throws a NumberFormatException
     * parseInt("99", 8) throws a NumberFormatException
     * parseInt("Kona", 10) throws a NumberFormatException
     * parseInt("Kona", 27) returns 411787
     * </pre></blockquote>
     *
     * @param      s   the {@code String} containing the integer
     *                  representation to be parsed
     * @param      radix   the radix to be used while parsing {@code s}.
     * @return     the integer represented by the string argument in the
     *             specified radix.
     * @exception  NumberFormatException if the {@code String}
     *             does not contain a parsable {@code int}.
     */
    private static int parseInt(CharSequence s, int radix) throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix +
                    " less than Character.MIN_RADIX");
        }

        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix +
                    " greater than Character.MAX_RADIX");
        }

        int result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+')
                    throw NumberFormatExceptionForInputString(s);

                if (len == 1) // Cannot have lone "+" or "-"
                    throw NumberFormatExceptionForInputString(s);
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++),radix);
                if (digit < 0) {
                    throw NumberFormatExceptionForInputString(s);
                }
                if (result < multmin) {
                    throw NumberFormatExceptionForInputString(s);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw NumberFormatExceptionForInputString(s);
                }
                result -= digit;
            }
        } else {
            throw NumberFormatExceptionForInputString(s);
        }
        return negative ? result : -result;
    }

    private static NumberFormatException NumberFormatExceptionForInputString(CharSequence s) {
        return new NumberFormatException("For input string: \"" + s.toString() + "\"");
    }
}
