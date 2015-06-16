package net.sf.extjwnl.util;

import org.junit.After;
import org.junit.Before;

import java.nio.charset.StandardCharsets;

public class TestByteArrayCharSequence extends TestPointedCharSequence {

    @Before
    public void setUp() {
        byte[] bytes = test.getBytes(StandardCharsets.US_ASCII);
        b = new ByteArrayCharSequence(bytes, 0, bytes.length);
    }

    @After
    public void tearDown() {
        b = null;
    }
}