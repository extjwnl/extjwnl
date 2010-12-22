package net.didion.jwnl.data;

import junit.framework.TestCase;

import java.util.BitSet;

/**
 * Tests Synset functionality by creating a mock synset.
 *
 * @author bwalenz
 */
public class TestSynset extends TestCase {

    /**
     * Our test object.
     */
    Synset testObj;

    /**
     * A test gloss definition.
     */
    String gloss = "testGloss";

    /**
     * A notional offset.
     */
    long offset = 4125;

    /**
     * Creates a new Synset.
     */
    public void setUp() {
        testObj = new Synset(POS.NOUN, offset, new Word[2], new Pointer[2], gloss,
                new BitSet(), false);
    }

    /**
     * Tests getting the part of speech.
     */
    public void testGetPOS() {
        assertTrue(testObj.getPOS().equals(POS.NOUN));
    }

    /**
     * Tests getting the gloss.
     */
    public void testGetGloss() {
        assertTrue(testObj.getGloss().equals(gloss));
    }

    /**
     * Tests getting the verb frame flags.
     */
    public void testGetVerbFrameFlags() {
        assertTrue(testObj.getVerbFrameFlags().isEmpty());
    }

    /**
     * Tests getting the offset.
     */
    public void testGetOffset() {
        assertTrue(testObj.getOffset() == offset);
    }

    /**
     * Tests getting the words size.
     */
    public void testGetWordsSize() {
        assertTrue(testObj.getWordsSize() == 2);
    }

    /**
     * Tests getting the pointers size.
     */
    public void testGetPointersSize() {
        assertTrue(testObj.getPointers().length == 2);
    }

}
