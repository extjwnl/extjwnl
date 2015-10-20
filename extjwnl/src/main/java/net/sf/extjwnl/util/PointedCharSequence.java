package net.sf.extjwnl.util;

/**
 * PointedCharSequence is a char sequence with the position of its last byte in the original file.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface PointedCharSequence extends CharSequence {

    /**
     * Position of the last byte of this sequence in the file (exclusive).
     *
     * @return position of the last byte of this sequence in the file (exclusive)
     */
    long getLastBytePosition();

    /**
     * Returns the index within this sequence of the first occurrence of the specified substring.
     *
     * <p>The returned index is the smallest value <i>k</i> for which:
     * <blockquote><pre>
     * this.startsWith(str, <i>k</i>)
     * </pre></blockquote>
     * If no such value of <i>k</i> exists, then {@code -1} is returned.
     *
     * @param str the substring to search for.
     * @return the index of the first occurrence of the specified substring,
     * or {@code -1} if there is no such occurrence.
     */
    int indexOf(String str);

    /**
     * Compares the sequence and the string lexicographically.
     * The comparison is based on the Unicode value of each character in
     * the strings. The character sequence represented by this
     * object is compared lexicographically to the
     * character sequence represented by the argument string. The result is
     * a negative integer if this object
     * lexicographically precedes the argument string. The result is a
     * positive integer if this object lexicographically
     * follows the argument string. The result is zero if the strings
     * are equal; {@code compareTo} returns {@code 0} exactly when
     * the equals(Object) method would return {@code true}.
     * <p>
     * This is the definition of lexicographic ordering. If two strings are
     * different, then either they have different characters at some index
     * that is a valid index for both strings, or their lengths are different,
     * or both. If they have different characters at one or more index
     * positions, let <i>k</i> be the smallest such index; then the string
     * whose character at position <i>k</i> has the smaller value, as
     * determined by using the &lt; operator, lexicographically precedes the
     * other string. In this case, {@code compareTo} returns the
     * difference of the two character values at position {@code k} in
     * the two string -- that is, the value:
     * <blockquote><pre>
     * this.charAt(k)-anotherString.charAt(k)
     * </pre></blockquote>
     * If there is no index position at which they differ, then the shorter
     * string lexicographically precedes the longer string. In this case,
     * {@code compareTo} returns the difference of the lengths of the
     * strings -- that is, the value:
     * <blockquote><pre>
     * this.length()-anotherString.length()
     * </pre></blockquote>
     *
     * @param   anotherString   the {@code String} to be compared.
     * @return  the value {@code 0} if the argument string is equal to
     *          this string; a value less than {@code 0} if this string
     *          is lexicographically less than the string argument; and a
     *          value greater than {@code 0} if this string is
     *          lexicographically greater than the string argument.
     */
    int compareTo(String anotherString);
}
