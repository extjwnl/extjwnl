package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * Tests Exc class.
 *
 * @author Brett Walenz <bwalenz@users.sourceforge.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestExc extends BaseData {

    private Exc testObj;

    private static final POS pos = POS.NOUN;
    private static final String lemma = "alam";
    private static final String exc1 = "exc1";
    private static final String exc2 = "exc2";

    @Before
    public void setUp() throws JWNLException, IOException {
        super.setUp();
        testObj = new Exc(dictionary, pos, lemma, Arrays.asList(exc1, exc2));
    }

    @Test
    public void testGetPOS() {
        Assert.assertEquals(pos, testObj.getPOS());
        Assert.assertNotNull(testObj.toString());
    }

    @Test
    public void testGetLemma() {
        Assert.assertEquals(lemma, testObj.getLemma());
    }

    @Test
    public void testGetExceptions() {
        Assert.assertEquals(2, testObj.getExceptions().size());
        Assert.assertEquals(DictionaryElementType.EXCEPTION, testObj.getType());
        Assert.assertEquals(exc1, testObj.getExceptions().get(0));
        Assert.assertEquals(exc2, testObj.getExceptions().get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor() throws JWNLException {
        new Exc(dictionary, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2() throws JWNLException {
        new Exc(dictionary, POS.NOUN, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor3() throws JWNLException {
        new Exc(dictionary, POS.NOUN, lemma, null);
    }
}