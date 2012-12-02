package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestIndexWordSet extends BaseData {

    @Test
    public void testConstructor() {
        IndexWordSet iws = new IndexWordSet("test");
        Assert.assertEquals("test", iws.getLemma());
    }

    @Test
    public void testAdd() throws JWNLException {
        IndexWordSet iws = new IndexWordSet("test");
        final IndexWord test = new IndexWord(dictionary, "test", POS.NOUN);
        iws.add(test);
        Assert.assertEquals(1, iws.size());
        Assert.assertEquals(test, iws.getIndexWord(POS.NOUN));
    }

    @Test
    public void testRemove() throws JWNLException {
        IndexWordSet iws = new IndexWordSet("test");
        final IndexWord test = new IndexWord(dictionary, "test", POS.NOUN);
        iws.add(test);
        Assert.assertEquals(1, iws.size());
        Assert.assertEquals(test, iws.getIndexWord(POS.NOUN));
        iws.remove(POS.NOUN);
        Assert.assertEquals(0, iws.size());
    }

    @Test
    public void testGets() throws JWNLException {
        IndexWordSet iws = new IndexWordSet("test");
        final IndexWord test = new IndexWord(dictionary, "test", POS.NOUN);
        iws.add(test);
        Assert.assertEquals(1, iws.getIndexWordArray().length);
        Assert.assertEquals(1, iws.getIndexWordCollection().size());
        Assert.assertEquals(1, iws.getValidPOSSet().size());
        Assert.assertTrue(iws.isValidPOS(POS.NOUN));
        Assert.assertFalse(iws.isValidPOS(POS.VERB));
        Assert.assertNotNull(iws.toString());
    }
}
