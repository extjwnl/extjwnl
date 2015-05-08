package net.sf.extjwnl.data.list;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestPointerTargetTree {

    @Test
    public void testEquals() throws JWNLException {
        Synset s1 = new Synset(null, POS.NOUN, 1);
        Synset s2 = new Synset(null, POS.NOUN, 2);
        PointerTargetTree p1 = new PointerTargetTree(new PointerTargetTreeNode(s1));
        PointerTargetTree p2 = new PointerTargetTree(new PointerTargetTreeNode(s2));
        PointerTargetTree p3 = new PointerTargetTree(new PointerTargetTreeNode(s1));
        Assert.assertFalse(p1.equals(p2));
        Assert.assertTrue(p1.equals(p3));
    }

    @Test
    public void testFirstMatch() throws JWNLException {
        Synset s1 = new Synset(null, POS.NOUN, 1);
        Synset s2 = new Synset(null, POS.NOUN, 2);
        PointerTargetTreeNode n1 = new PointerTargetTreeNode(s1);
        PointerTargetTree p1 = new PointerTargetTree(n1);
        PointerTargetTreeNode n2 = new PointerTargetTreeNode(s2);

        Assert.assertEquals(n1, p1.getFirstMatch(new PointerTargetTreeNodeList.FindNodeOperation(n1)));
        Assert.assertNull(p1.getFirstMatch(new PointerTargetTreeNodeList.FindNodeOperation(n2)));

        Assert.assertEquals(n1, p1.getFirstMatch(new PointerTargetTreeNodeList.FindTargetOperation(s1)));
        Assert.assertNull(p1.getFirstMatch(new PointerTargetTreeNodeList.FindTargetOperation(s2)));

        Assert.assertEquals(n1, p1.findFirst(s1));
        Assert.assertEquals(n1, p1.findFirst(n1));
    }

    @Test
    public void testAllMatches() throws JWNLException {
        Synset s1 = new Synset(null, POS.NOUN, 1);
        Synset s2 = new Synset(null, POS.NOUN, 2);
        PointerTargetTreeNode n1 = new PointerTargetTreeNode(s1);
        PointerTargetTree p1 = new PointerTargetTree(n1);
        PointerTargetTreeNode n2 = new PointerTargetTreeNode(s2);

        Assert.assertEquals(n1, p1.getAllMatches(new PointerTargetTreeNodeList.FindNodeOperation(n1)).get(0));
        Assert.assertEquals(0, p1.getAllMatches(new PointerTargetTreeNodeList.FindNodeOperation(n2)).size());

        Assert.assertEquals(n1, p1.findAll(n1).get(0));
        Assert.assertEquals(0, p1.findAll(s2).size());
    }
}
