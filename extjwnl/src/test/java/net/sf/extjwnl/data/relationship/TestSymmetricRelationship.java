package net.sf.extjwnl.data.relationship;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.BaseData;
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
public class TestSymmetricRelationship extends BaseData {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorPointerType() {
        new SymmetricRelationship(null, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNodes() {
        new SymmetricRelationship(PointerType.ANTONYM, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSourceSynset() {
        new SymmetricRelationship(PointerType.ANTONYM, new PointerTargetNodeList(), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorTargetSynset() throws JWNLException {
        dictionary.edit();
        new SymmetricRelationship(PointerType.ANTONYM, new PointerTargetNodeList(), dictionary.createSynset(POS.NOUN), null);
    }

    @Test
    public void testConstructorAndGetters() throws JWNLException {
        dictionary.edit();
        Synset s1 = dictionary.createSynset(POS.NOUN);
        Synset s2 = dictionary.createSynset(POS.NOUN);
        SymmetricRelationship r =
                new SymmetricRelationship(PointerType.ANTONYM, new PointerTargetNodeList(
                        Arrays.asList(
                                new PointerTargetNode(s1, PointerType.ANTONYM),
                                new PointerTargetNode(s2, PointerType.ANTONYM)
                        )
                ), s1, s2);

        Assert.assertEquals(s1, r.getSourceSynset());
        Assert.assertEquals(s2, r.getTargetSynset());
        Assert.assertNotNull(r.toString());

        Assert.assertEquals(s1, r.getSourcePointerTarget());
        Assert.assertEquals(s2, r.getTargetSynset());

        SymmetricRelationship rr =
                new SymmetricRelationship(PointerType.ANTONYM, new PointerTargetNodeList(
                        Arrays.asList(
                                new PointerTargetNode(s2, PointerType.ANTONYM),
                                new PointerTargetNode(s1, PointerType.ANTONYM)
                        )
                ), s2, s1);

        Assert.assertTrue(rr.hashCode() != r.hashCode());
        Assert.assertTrue(!rr.equals(r));
    }

    @Test
    public void testReverse() throws JWNLException, CloneNotSupportedException {
        dictionary.edit();
        Synset s1 = dictionary.createSynset(POS.NOUN);
        Synset s2 = dictionary.createSynset(POS.NOUN);
        SymmetricRelationship r =
                new SymmetricRelationship(PointerType.HYPERNYM,
                        new PointerTargetNodeList(Arrays.asList(
                                new PointerTargetNode(s1),
                                new PointerTargetNode(s2)
                        )),
                        s1, s2);
        Relationship rev = r.reverse();

        Assert.assertEquals(2, rev.getSize());
        Assert.assertEquals(2, rev.getNodeList().size());
        Assert.assertEquals(PointerType.HYPONYM, rev.getNodeList().get(0).getType());
        Assert.assertEquals(PointerType.HYPONYM, rev.getNodeList().get(1).getType());
        Assert.assertEquals(s2, rev.getNodeList().get(0).getSynset());
        Assert.assertEquals(s1, rev.getNodeList().get(1).getSynset());
    }
}
