package net.sf.extjwnl.util;

import java.nio.charset.StandardCharsets;

/**
 * CharSequence backed by byte array.
 * Does not convert encoding. Does not check bounds.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class ByteArrayCharSequence implements PointedCharSequence {

    private final byte[] bytes;
    private final int start; // inclusive
    private final int end; // exclusive
    private final long position; // exclusive

    /**
     * Creates a char sequence backed by <var>bytes</var>,
     * starting from <var>start</var> (inclusive) and ending at <var>end</var> (exclusive).
     *
     * @param bytes backing byte array
     * @param start start index (inclusive)
     * @param end   end index (exclusive)
     */
    public ByteArrayCharSequence(byte[] bytes, int start, int end) {
        this.bytes = bytes;
        this.start = start;
        this.end = end;
        this.position = -1;
    }

    /**
     * Creates a char sequence backed by <var>bytes</var>,
     * starting from <var>start</var> (inclusive) and ending at <var>end</var> (exclusive).
     *
     * @param bytes    backing byte array
     * @param start    start index (inclusive)
     * @param end      end index (exclusive)
     * @param position last byte position (exclusive)
     */
    public ByteArrayCharSequence(byte[] bytes, int start, int end, long position) {
        this.bytes = bytes;
        this.start = start;
        this.end = end;
        this.position = position;
    }

    @Override
    public long getLastBytePosition() {
        return position;
    }

    @Override
    public int length() {
        return end - start;
    }

    @Override
    public char charAt(int index) {
        return (char) bytes[start + index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new ByteArrayCharSequence(bytes, this.start + start, this.start + end);
    }

    @Override
    public String toString() {
        return new String(bytes, start, end - start, StandardCharsets.US_ASCII);
    }

    @Override
    public int indexOf(String str) {
        return indexOf(bytes, start, end - start, str, 0, str.length(), 0);
    }

    // Lifted from String.java
    @Override
    public int compareTo(String anotherString) {
        int len1 = length();
        int len2 = anotherString.length();
        int lim = Math.min(len1, len2);

        int k = 0;
        while (k < lim) {
            char c1 = (char) bytes[start + k];
            char c2 = anotherString.charAt(k);
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }

    // Lifted from String.java
    /**
     * The source is the character array being searched,
     * and the target is the string being searched for.
     *
     * @param   source       the characters being searched.
     * @param   sourceOffset offset of the source string.
     * @param   sourceCount  count of the source string.
     * @param   target       the characters being searched for.
     * @param   targetOffset offset of the target string.
     * @param   targetCount  count of the target string.
     * @param   fromIndex    the index to begin searching from.
     */
    private static int indexOf(byte[] source, int sourceOffset, int sourceCount,
                       String target, int targetOffset, int targetCount,
                       int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        char first = target.charAt(targetOffset);
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source[i] != first) {
                while (++i <= max && source[i] != first);
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source[j] == target.charAt(k); j++, k++);

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }
}