package net.sf.extjwnl.data.relationship;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestRelationshipList {

    @Test
    public void testAdd() throws JWNLException {
        RelationshipList l = new RelationshipList();
        Assert.assertNull(l.getShallowest());
        Assert.assertNull(l.getDeepest());

        Relationship r = new SymmetricRelationship(PointerType.ANTONYM,
                new PointerTargetNodeList(),
                new Synset(null, POS.NOUN), new Synset(null, POS.NOUN));

        l.add(r);
        Assert.assertEquals(r, l.getDeepest());
        Assert.assertEquals(r, l.getShallowest());
    }

    @Test
    public void testGetShallowest() throws JWNLException {
        RelationshipList l = new RelationshipList();
        Assert.assertNull(l.getShallowest());
        Assert.assertNull(l.getDeepest());

        Relationship r = new SymmetricRelationship(PointerType.ANTONYM,
                new PointerTargetNodeList(Arrays.asList(new PointerTargetNode(new Synset(null, POS.NOUN)))),
                new Synset(null, POS.NOUN), new Synset(null, POS.NOUN));

        l.add(r);
        Assert.assertEquals(r, l.getDeepest());
        Assert.assertEquals(r, l.getShallowest());

        Relationship rr = new SymmetricRelationship(PointerType.ANTONYM,
                new PointerTargetNodeList(),
                new Synset(null, POS.NOUN), new Synset(null, POS.NOUN));

        l.add(rr);
        Assert.assertEquals(r, l.getDeepest());
        Assert.assertEquals(rr, l.getShallowest());
    }

    @Test
    public void testGetDeepest() throws JWNLException {
        RelationshipList l = new RelationshipList();
        Assert.assertNull(l.getShallowest());
        Assert.assertNull(l.getDeepest());


        Relationship r = new SymmetricRelationship(PointerType.ANTONYM,
                new PointerTargetNodeList(),
                new Synset(null, POS.NOUN), new Synset(null, POS.NOUN));

        l.add(r);
        Assert.assertEquals(r, l.getDeepest());
        Assert.assertEquals(r, l.getShallowest());

        Relationship rr = new SymmetricRelationship(PointerType.ANTONYM,
                new PointerTargetNodeList(Arrays.asList(new PointerTargetNode(new Synset(null, POS.NOUN)))),
                new Synset(null, POS.NOUN), new Synset(null, POS.NOUN));

        l.add(rr);
        Assert.assertEquals(rr, l.getDeepest());
        Assert.assertEquals(r, l.getShallowest());
    }
}
