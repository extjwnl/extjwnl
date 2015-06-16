package net.sf.extjwnl.util;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(DataProviderRunner.class)
public abstract class TestPointedCharSequence {

    protected PointedCharSequence b;
    protected final static String test = "abcd";

    @Test
    public void testLength() {
        assertEquals(test.length(), b.length());
    }

    @Test
    public void testCharAt() {
        for (int i = 0; i < test.length(); i++) {
            assertEquals(test.charAt(i), b.charAt(i));
        }
    }

    @DataProvider
    public static Object[][] testSubsequence() {
        return new Object[][]{
                {"", 0, 0},
                {"a", 0, 1},
                {"ab", 0, 2},
                {"bc", 1, 3},
                {"cd", 2, 4},
                {"d", 3, 4},
                {"", 3, 3}
        };
    }

    @Test
    @UseDataProvider("testSubsequence")
    public void testSubsequence(final String s, final int x, final int y) {
        assertEquals(s, b.subSequence(x, y).toString());
    }

    @DataProvider
    public static Object[][] testCompareTo() {
        return new Object[][]{
                {"", 1},
                {"abcd", 0},
                {"abce", -1},
                {"abca", 1},
                {"a", 1},
                {"abcde", -1},
                {"adcd", -1},
                {"aacd", 1}
        };
    }

    @Test
    @UseDataProvider("testCompareTo")
    public void testCompareTo(final String s, final int x) {
        assertEquals(x, (int) Math.signum(b.compareTo(s)));
    }

    @DataProvider
    public static Object[][] testIndexOf() {
        return new Object[][]{
                {-1, "f"},
                {-1, "fffffff"},
                {-1, "abcde"},
                {-1, "bcde"},
                {0, ""},
                {0, "a"},
                {0, "ab"},
                {0, "abc"},
                {0, "abcd"},
                {1, "b"},
                {1, "bc"},
                {1, "bcd"},
                {3, "d"}
        };
    }

    @Test
    @UseDataProvider("testIndexOf")
    public void testIndexOf(final int i, final String s) {
        assertEquals(i, b.indexOf(s));
    }
}