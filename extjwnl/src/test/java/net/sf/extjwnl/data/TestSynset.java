package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests Synset functionality by creating a mock synset.
 *
 * @author Brett Walenz <bwalenz@users.sourceforge.net>
 * @author Aliaksandr Autayeu <aliaksandr@autayeu.com>
 */
public class TestSynset extends BaseDataTest {

    private Synset testObj;
    private final static String gloss = "testGloss";
    private final static long offset = 4125;

    @Before
    public void setUp() throws JWNLException, IOException {
        super.setUp();
        testObj = new Synset(dictionary, POS.NOUN, offset);
        testObj.setGloss(gloss);
    }

    @Test
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

    @Test(expected =  UnsupportedOperationException.class)
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