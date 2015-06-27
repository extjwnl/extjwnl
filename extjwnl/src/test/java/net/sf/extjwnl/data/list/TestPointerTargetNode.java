package net.sf.extjwnl.data.list;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestPointerTargetNode {

    @Test
    public void testIsLexical() throws JWNLException {
        Synset s1 = new Synset(null, POS.NOUN);
        PointerTargetNode p1 = new PointerTargetNode(s1, PointerType.ANTONYM);
        Synset s2 = new Synset(null, POS.NOUN);
        Word w2 = new Word(null, s2, "test");
        PointerTargetNode p2 = new PointerTargetNode(w2, PointerType.ANTONYM);

        Assert.assertFalse(p1.isLexical());
        Assert.assertTrue(p2.isLexical());

        Assert.assertEquals(s1, p1.getSynset());
        Assert.assertEquals(s2, p2.getSynset());

        Assert.assertEquals(s1, p1.getPointerTarget());
        Assert.assertEquals(w2, p2.getPointerTarget());

        Assert.assertNull(p1.getWord());
        Assert.assertEquals(w2, p2.getWord());

        Assert.assertFalse(p1.equals(p2));
        Assert.assertFalse(p1.hashCode() == p2.hashCode());

        Assert.assertNotNull(p1.toString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDeepClone() throws JWNLException {
        Synset s1 = new Synset(null, POS.NOUN);
        PointerTargetNode p1 = new PointerTargetNode(s1);
        p1.deepClone();
    }
}
