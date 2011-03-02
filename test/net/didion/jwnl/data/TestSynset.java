package net.didion.jwnl.data;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Tests Synset functionality by creating a mock synset.
 *
 * @author bwalenz
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class TestSynset extends BaseDictionaryTest {

    private Synset testObj;
    private final static String gloss = "testGloss";
    private final static long offset = 4125;

    @Before
    public void setUp() throws JWNLException, IOException {
        Dictionary dictionary = Dictionary.getInstance(getProperties());
        testObj = new Synset(dictionary, POS.NOUN, offset);
        testObj.setGloss(gloss);
    }

    @Test(expected = JWNLException.class)
    public void testConstructor() throws JWNLException {
        testObj = new Synset(null, POS.NOUN, offset);
    }

    @Test
    public void testGetPOS() {
        Assert.assertEquals(POS.NOUN, testObj.getPOS());
    }

    @Test
    public void testGetGloss() {
        Assert.assertEquals(gloss, testObj.getGloss());
    }

    @Test
    public void testGetVerbFrameFlags() {
        Assert.assertTrue(testObj.getVerbFrameFlags().isEmpty());
    }

    @Test
    public void testGetOffset() {
        Assert.assertEquals(offset, testObj.getOffset());
    }

    @Test
    public void testGetWordsSize() {
        Assert.assertEquals(0, testObj.getWords().size());
    }

    @Test
    public void testGetPointersSize() throws JWNLException {
        Assert.assertEquals(0, testObj.getPointers().size());
    }
}