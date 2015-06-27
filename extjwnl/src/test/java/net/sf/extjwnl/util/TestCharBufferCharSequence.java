package net.sf.extjwnl.util;

import org.junit.After;
import org.junit.Before;

import java.nio.CharBuffer;

public class TestCharBufferCharSequence extends TestPointedCharSequence {

    @Before
    public void setUp() {
        char[] chars = test.toCharArray();
        b = new CharBufferCharSequence(CharBuffer.wrap(chars));
    }

    @After
    public void tearDown() {
        b = null;
    }

}