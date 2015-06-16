package net.sf.extjwnl.util;

import java.nio.CharBuffer;

/**
 * CharSequence backed by CharBuffer.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class CharBufferCharSequence implements PointedCharSequence {

    private final CharBuffer cb;
    private final long position;

    public CharBufferCharSequence(CharBuffer cb) {
        this.cb = cb;
        this.position = -1;
    }

    public CharBufferCharSequence(CharBuffer cb, long position) {
        this.cb = cb;
        this.position = position;
    }

    @Override
    public int length() {
        return cb.length();
    }

    @Override
    public char charAt(int index) {
        return cb.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new CharBufferCharSequence(cb.subSequence(start, end), position);
    }

    @Override
    public long getLastBytePosition() {
        return position;
    }

    @Override
    public String toString() {
        return cb.toString();
    }

    @Override
    public int indexOf(String str) {
        return indexOf(cb, 0, cb.length(), str, 0, str.length(), 0);
    }

    // Lifted from String.java
    @Override
    public int compareTo(String anotherString) {
        int len1 = cb.length();
        int len2 = anotherString.length();
        int lim = Math.min(len1, len2);

        int k = 0;
        while (k < lim) {
            char c1 = cb.charAt(k);
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
    private static int indexOf(CharBuffer source, int sourceOffset, int sourceCount,
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
            if (source.charAt(i) != first) {
                while (++i <= max && source.charAt(i) != first);
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source.charAt(j) == target.charAt(k); j++, k++);

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }
}