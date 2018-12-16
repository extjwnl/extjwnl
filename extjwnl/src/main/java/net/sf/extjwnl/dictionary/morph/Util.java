package net.sf.extjwnl.dictionary.morph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Utility class.
 *
 * @author John Didion (jdidion@didion.net)
 */
public abstract class Util {

    public static String getLemma(String[] tokens, BitSet bits, String delimiter) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i != 0 && !bits.get(i - 1)) {
                buf.append(delimiter);
            }
            buf.append(tokens[i]);
        }
        return buf.toString();
    }

    public static boolean increment(BitSet bits, int size) {
        int i = size - 1;
        while (i >= 0 && bits.get(i)) {
            bits.set(i--, false);
        }
        if (i < 0) {
            return false;
        }
        bits.set(i, true);
        return true;
    }

    public static String[] split(String str) {
        char[] chars = str.toCharArray();
        List<String> tokens = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        for (char aChar : chars) {
            if ((aChar >= 'a' && aChar <= 'z') || aChar == '\'') {
                buf.append(aChar);
            } else {
                if (buf.length() > 0) {
                    tokens.add(buf.toString());
                    buf = new StringBuilder();
                }
            }
        }
        if (buf.length() > 0) {
            tokens.add(buf.toString());
        }
        return tokens.toArray(new String[0]);
    }

    public static Throwable getRootCause(Throwable e) {
        Throwable result = e.getCause();
        while (null != result && null != result.getCause()) {
            result = result.getCause();
        }
        return result;
    }
}