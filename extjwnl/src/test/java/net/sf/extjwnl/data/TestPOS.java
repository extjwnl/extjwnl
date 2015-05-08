package net.sf.extjwnl.data;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestPOS {

    @Test
    public void testGetPOSForLabel() {
        Assert.assertEquals(POS.NOUN, POS.getPOSForLabel("noun"));
        Assert.assertEquals(POS.ADJECTIVE, POS.getPOSForLabel("adjective"));
        Assert.assertEquals(POS.VERB, POS.getPOSForLabel("verb"));
        Assert.assertEquals(POS.ADVERB, POS.getPOSForLabel("adverb"));
    }

    @Test
    public void testGetPOSForId() {
        Assert.assertEquals(POS.NOUN, POS.getPOSForId(1));
        Assert.assertEquals(POS.VERB, POS.getPOSForId(2));
        Assert.assertEquals(POS.ADJECTIVE, POS.getPOSForId(3));
        Assert.assertEquals(POS.ADVERB, POS.getPOSForId(4));
        Assert.assertEquals(POS.ADJECTIVE, POS.getPOSForId(5));
        Assert.assertNull(POS.getPOSForId(6));
        Assert.assertNull(POS.getPOSForId(0));
    }

    @Test
    public void testGetPOSForKey() {
        Assert.assertEquals(POS.NOUN, POS.getPOSForKey("n"));
        Assert.assertEquals(POS.ADJECTIVE, POS.getPOSForKey("a"));
        Assert.assertEquals(POS.VERB, POS.getPOSForKey("v"));
        Assert.assertEquals(POS.ADVERB, POS.getPOSForKey("r"));
        Assert.assertEquals(POS.ADJECTIVE, POS.getPOSForKey("s"));
        Assert.assertNull(POS.getPOSForKey("b"));
    }
}